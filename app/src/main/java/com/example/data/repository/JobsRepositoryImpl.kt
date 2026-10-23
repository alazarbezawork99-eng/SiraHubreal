package com.example.data.repository

import android.util.Base64
import android.util.Log
import com.example.R
import com.example.data.auth.SessionTokenStore
import com.example.data.core.DataResult
import com.example.data.local.SiraHubDatabase
import com.example.data.local.dao.JobDao
import com.example.data.mapper.JobMapper
import com.example.data.supabase.SupabaseHttpClient
import com.example.di.IoDispatcher
import com.example.model.JobPost
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import com.example.data.dto.JobDto
import com.example.data.dto.NewJobDto
import org.json.JSONArray
import org.json.JSONObject

/**
 * Supabase is the source of truth; Room is the offline read cache.
 *
 * Read path:  Room Flow -> mapper -> JobPost. Screens never wait on the network.
 * Write path: Supabase -> (on success) refresh -> Room. The cache is only ever written from a
 * server response, so it cannot drift from what the server actually holds.
 */
@Singleton
class JobsRepositoryImpl @Inject constructor(
    private val dao: JobDao,
    private val database: SiraHubDatabase,
    private val http: SupabaseHttpClient,
    private val tokenStore: SessionTokenStore,
    /**
     * Blocking OkHttp calls must never run on the caller's dispatcher: `viewModelScope` is
     * Main-immediate, so without this the very first request throws NetworkOnMainThreadException.
     * The repository owns the dispatcher so no call site has to remember.
     */
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : JobsRepository {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    override fun observeJobs(): Flow<List<JobPost>> =
        combine(dao.observeAll(), tokenStore.accessToken) { entities, _ ->
            val userId = currentUserId()
            entities.map { JobMapper.entityToDomain(it, userId) }
        }

    override fun observeJob(jobId: String): Flow<JobPost?> =
        combine(dao.observeById(jobId), tokenStore.accessToken) { entity, _ ->
            entity?.let { JobMapper.entityToDomain(it, currentUserId()) }
        }

    override fun observeMyJobs(userId: String): Flow<List<JobPost>> =
        dao.observeByPoster(userId).map { entities ->
            entities.map { JobMapper.entityToDomain(it, userId) }
        }

    override suspend fun refresh(): DataResult<Int> = withContext(ioDispatcher) {
        if (!http.isConfigured) {
            return@withContext DataResult.ValidationError(messageRes = R.string.error_not_configured)
        }

        // Only open jobs are public. RLS also returns the caller's own non-open jobs, so no
        // client-side status filter is needed - the server already scopes the result.
        val result = http.get(
            table = "jobs",
            query = mapOf(
                "select" to "*",
                "order" to "created_at.desc"
            )
        )

        when (result) {
            is DataResult.Success -> {
                val dtos = decodeJobs(result.data)
                    ?: return@withContext DataResult.ServerError(
                        IllegalStateException("Could not parse jobs payload")
                    )
                val entities = dtos.map(JobMapper::dtoToEntity)
                database.replaceJobs(entities)
                Log.i(TAG, "Cached ${entities.size} jobs from Supabase.")
                DataResult.Success(entities.size)
            }

            is DataResult.NetworkError -> {
                Log.w(TAG, "Job refresh failed: no network.", result.cause)
                result
            }

            is DataResult.AuthError -> {
                Log.w(TAG, "Job refresh rejected: session invalid.", result.cause)
                result
            }

            is DataResult.ValidationError -> {
                Log.w(TAG, "Job refresh rejected by server: ${result.messageRes}", result.cause)
                result
            }

            is DataResult.ServerError -> {
                Log.e(TAG, "Job refresh failed on the server.", result.cause)
                result
            }
        }
    }

    override suspend fun createJob(job: JobPost): DataResult<JobPost> = withContext(ioDispatcher) {
        if (!http.isConfigured) {
            return@withContext DataResult.ValidationError(messageRes = R.string.error_not_configured)
        }
        val userId = currentUserId()
            ?: return@withContext DataResult.AuthError(IllegalStateException("Not signed in"))

        // The `enforce_job_rules` trigger only allows status='open' when is_company_verified()
        // finds a row in public.companies for this owner. Sign-up creates the profile but not the
        // company row, so make sure it exists before attempting the insert.
        val companyId = ensureCompanyRow(userId, job.company)

        val body = JobMapper.domainToNewDto(job, postedBy = userId, companyId = companyId)
        val payload = json.encodeToString(NewJobDto.serializer(), body)

        when (val result = http.post(table = "jobs", body = payload)) {
            is DataResult.Success -> {
                val created = decodeJobs(result.data)?.firstOrNull()
                if (created == null) {
                    DataResult.ServerError(
                        IllegalStateException("Server did not return the created job")
                    )
                } else {
                    // Pull the authoritative list so applicants_count, created_at and the
                    // generated id all come from the server rather than being guessed locally.
                    refresh()
                    DataResult.Success(JobMapper.entityToDomain(JobMapper.dtoToEntity(created), userId))
                }
            }

            is DataResult.NetworkError -> result
            is DataResult.AuthError -> result
            is DataResult.ValidationError -> result
            is DataResult.ServerError -> result
        }
    }

    override suspend fun deleteJob(jobId: String): DataResult<Unit> = withContext(ioDispatcher) {
        if (!http.isConfigured) {
            return@withContext DataResult.ValidationError(messageRes = R.string.error_not_configured)
        }

        when (val result = http.delete(table = "jobs", query = mapOf("id" to "eq.$jobId"))) {
            is DataResult.Success -> {
                // Only drop the local row once the server has confirmed the delete, so a failed
                // request never makes a job silently disappear from the user's own list.
                dao.deleteById(jobId)
                DataResult.Success(Unit)
            }

            is DataResult.NetworkError -> result
            is DataResult.AuthError -> result
            is DataResult.ValidationError -> result
            is DataResult.ServerError -> result
        }
    }

    /**
     * Returns the id of the signed-in user's company row, creating it if it does not exist yet.
     *
     * Sign-up only writes `public.profiles` (via the `handle_new_user` trigger), so a Company
     * account has no `public.companies` row until it is created here. Without it,
     * `is_company_verified()` finds nothing and every job insert is rejected.
     *
     * The row is created with `verification_status = 'unverified'`, which is what the schema
     * defaults to and what the guard trigger expects. Verification itself is a back-office step;
     * this method only makes sure the company exists.
     *
     * Returns null when the row could not be read or created, in which case the caller proceeds
     * without a company id and surfaces whatever error the server returns.
     */
    private fun ensureCompanyRow(userId: String, companyName: String): String? {
        val existing = http.get(
            table = "companies",
            query = mapOf("select" to "id", "owner_id" to "eq.$userId", "limit" to "1")
        )

        if (existing is DataResult.Success) {
            val id = runCatching {
                JSONArray(existing.data).optJSONObject(0)?.optString("id")
            }.getOrNull()
            if (!id.isNullOrBlank()) return id
        }

        // `companies.name` requires at least 2 characters.
        val safeName = companyName.trim().takeIf { it.length >= 2 } ?: "Company"
        val payload = JSONObject().apply {
            put("owner_id", userId)
            put("name", safeName.take(150))
            put("verification_status", "unverified")
        }.toString()

        val created = http.post(
            table = "companies",
            body = payload,
            prefer = "return=representation"
        )

        return when (created) {
            is DataResult.Success -> runCatching {
                JSONArray(created.data).optJSONObject(0)?.optString("id")
            }.getOrNull()?.takeIf { it.isNotBlank() }

            else -> {
                Log.w(TAG, "Could not create companies row for $userId")
                null
            }
        }
    }

    /**
     * The signed-in user's id, read from the JWT `sub` claim.
     *
     * We decode the token rather than storing the id separately so there is exactly one source of
     * identity. An unparseable token yields null, which simply means "nothing is marked as mine".
     */
    private fun currentUserId(): String? {
        val token = tokenStore.currentToken() ?: return null
        return runCatching {
            val payload = token.split('.')
            if (payload.size < 2) return null
            val decoded = Base64.decode(
                payload[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            JSONObject(String(decoded, Charsets.UTF_8)).optString("sub").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    /** Decodes a PostgREST payload. Returns null when the body is not a JSON array of jobs. */
    private fun decodeJobs(body: String): List<JobDto>? =
        runCatching {
            json.decodeFromString(ListSerializer(JobDto.serializer()), body)
        }.onFailure {
            Log.e(TAG, "Failed to decode jobs payload", it)
        }.getOrNull()

    companion object {
        private const val TAG = "JobsRepository"
    }
}
