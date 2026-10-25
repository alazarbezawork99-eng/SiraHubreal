package com.example.data.repository

import android.util.Log
import com.example.R
import com.example.data.auth.CurrentUserProvider
import com.example.data.core.DataResult
import com.example.data.dto.GigDto
import com.example.data.dto.NewGigContactDto
import com.example.data.dto.NewGigDto
import com.example.data.local.SiraHubDatabase
import com.example.data.local.dao.GigDao
import com.example.data.mapper.GigMapper
import com.example.data.supabase.SupabaseHttpClient
import com.example.di.IoDispatcher
import com.example.model.GigPost
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Supabase is the source of truth; Room is the offline read cache.
 *
 * Read path:  Room Flow (gigs + contacts) -> mapper -> GigPost.
 * Write path: Supabase -> (on success) refresh -> Room. The cache is only ever written from a
 * server response, so it cannot drift from what the server actually holds.
 */
@Singleton
class GigsRepositoryImpl @Inject constructor(
    private val dao: GigDao,
    private val database: SiraHubDatabase,
    private val http: SupabaseHttpClient,
    private val currentUser: CurrentUserProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GigsRepository {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    override fun observeGigs(): Flow<List<GigPost>> = combine(
        dao.observeAll(),
        dao.observeAllContacts()
    ) { gigs, contacts ->
        val userId = currentUser.userId()
        val contactsById = contacts.associateBy { it.gigId }
        gigs.map { GigMapper.entityToDomain(it, contactsById[it.id], userId) }
    }

    override fun observeGig(gigId: String): Flow<GigPost?> = combine(
        dao.observeById(gigId),
        dao.observeAllContacts()
    ) { entity, contacts ->
        entity?.let {
            GigMapper.entityToDomain(
                it,
                contacts.firstOrNull { c -> c.gigId == gigId },
                currentUser.userId()
            )
        }
    }

    override fun observeMyGigs(userId: String): Flow<List<GigPost>> = combine(
        dao.observeByOwner(userId),
        dao.observeAllContacts()
    ) { gigs, contacts ->
        val contactsById = contacts.associateBy { it.gigId }
        gigs.map { GigMapper.entityToDomain(it, contactsById[it.id], userId) }
    }

    override suspend fun refresh(): DataResult<Int> = withContext(ioDispatcher) {
        if (!http.isConfigured) {
            return@withContext DataResult.ValidationError(messageRes = R.string.error_not_configured)
        }

        // `gig_contacts` is embedded so the owner (and the winning bidder) receive contact details
        // in the same round trip. For anyone else RLS returns an empty array, which the mapper
        // treats as "not permitted to see" rather than an error.
        val result = http.get(
            table = "gigs",
            query = mapOf(
                "select" to "*,gig_contacts(client_phone,client_telegram)",
                "order" to "created_at.desc"
            )
        )

        when (result) {
            is DataResult.Success -> {
                val dtos = decodeGigs(result.data)
                    ?: return@withContext DataResult.ServerError(
                        IllegalStateException("Could not parse gigs payload")
                    )

                val entities = dtos.map(GigMapper::dtoToEntity)
                val contacts = dtos.mapNotNull(GigMapper::dtoToContactEntity)

                database.replaceGigs(entities, contacts)
                Log.i(TAG, "Cached ${entities.size} gigs (${contacts.size} contact rows) from Supabase.")
                DataResult.Success(entities.size)
            }

            is DataResult.NetworkError -> {
                Log.w(TAG, "Gig refresh failed: no network.", result.cause)
                result
            }

            is DataResult.AuthError -> {
                Log.w(TAG, "Gig refresh rejected: session invalid.", result.cause)
                result
            }

            is DataResult.ValidationError -> {
                Log.w(TAG, "Gig refresh rejected by server: ${result.messageRes}", result.cause)
                result
            }

            is DataResult.ServerError -> {
                Log.e(TAG, "Gig refresh failed on the server.", result.cause)
                result
            }
        }
    }

    override suspend fun createGig(gig: GigPost): DataResult<GigPost> = withContext(ioDispatcher) {
        if (!http.isConfigured) {
            return@withContext DataResult.ValidationError(messageRes = R.string.error_not_configured)
        }
        val userId = currentUser.userId()
            ?: return@withContext DataResult.AuthError(IllegalStateException("Not signed in"))

        val payload = json.encodeToString(
            NewGigDto.serializer(),
            GigMapper.domainToNewDto(gig, ownerId = userId)
        )

        when (val created = http.post(table = "gigs", body = payload)) {
            is DataResult.Success -> {
                val dto = decodeGigs(created.data)?.firstOrNull()
                if (dto == null) {
                    return@withContext DataResult.ServerError(
                        IllegalStateException("Server did not return the created gig")
                    )
                }

                // The contact row references the gig, so it can only be written afterwards. A
                // failure here is reported but does not roll back the published gig.
                val contactFailure = writeContactRow(dto.id, gig)
                if (contactFailure != null) {
                    return@withContext contactFailure
                }

                refresh()
                DataResult.Success(
                    GigMapper.entityToDomain(
                        GigMapper.dtoToEntity(dto),
                        GigMapper.dtoToContactEntity(dto),
                        userId
                    )
                )
            }

            is DataResult.NetworkError -> created
            is DataResult.AuthError -> created
            is DataResult.ValidationError -> created
            is DataResult.ServerError -> created
        }
    }

    override suspend fun deleteGig(gigId: String): DataResult<Unit> = withContext(ioDispatcher) {
        if (!http.isConfigured) {
            return@withContext DataResult.ValidationError(messageRes = R.string.error_not_configured)
        }

        when (val result = http.delete(table = "gigs", query = mapOf("id" to "eq.$gigId"))) {
            is DataResult.Success -> {
                // Only drop the local rows once the server has confirmed the delete.
                dao.deleteById(gigId)
                DataResult.Success(Unit)
            }

            is DataResult.NetworkError -> result
            is DataResult.AuthError -> result
            is DataResult.ValidationError -> result
            is DataResult.ServerError -> result
        }
    }

    /**
     * Writes the `gig_contacts` row for a freshly created gig.
     *
     * Returns null on success, or the failure to surface. The gig itself is already published at
     * this point, so the message tells the user their contact details specifically were not saved.
     */
    private fun writeContactRow(gigId: String, gig: GigPost): DataResult<Nothing>? {
        val contactDto = GigMapper.domainToNewContactDto(gigId, gig) ?: return null

        val payload = json.encodeToString(NewGigContactDto.serializer(), contactDto)
        val result = http.post(
            table = "gig_contacts",
            body = payload,
            prefer = "return=minimal"
        )

        return when (result) {
            is DataResult.Success -> null
            is DataResult.NetworkError -> result
            is DataResult.AuthError -> result
            is DataResult.ValidationError -> {
                Log.w(TAG, "Gig $gigId published but its contact row was rejected.", result.cause)
                result
            }
            is DataResult.ServerError -> {
                Log.e(TAG, "Gig $gigId published but its contact row failed.", result.cause)
                result
            }
        }
    }

    /** Decodes a PostgREST payload. Returns null when the body is not a JSON array of gigs. */
    private fun decodeGigs(body: String): List<GigDto>? =
        runCatching {
            json.decodeFromString(ListSerializer(GigDto.serializer()), body)
        }.onFailure {
            Log.e(TAG, "Failed to decode gigs payload", it)
        }.getOrNull()

    companion object {
        private const val TAG = "GigsRepository"
    }
}
