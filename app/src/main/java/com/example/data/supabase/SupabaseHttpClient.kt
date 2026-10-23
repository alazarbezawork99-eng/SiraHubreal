package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.auth.SessionTokenStore
import com.example.data.core.DataResult
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

/**
 * The single injectable gateway to the Supabase project.
 *
 * Replaces the old `SupabaseClientConfig` / `SupabaseManager` singletons. It owns:
 *  - the base URL and anon key (from BuildConfig - never a service_role key),
 *  - the OkHttp client that attaches `apikey` and the signed-in user's JWT,
 *  - the mapping from HTTP status codes to [DataResult] failures.
 *
 * Repositories call [get] / [post] / [patch] / [delete] and receive either the raw successful
 * [Response] body or a typed failure. No repository builds a [Request] by hand.
 */
@Singleton
class SupabaseHttpClient @Inject constructor(
    private val tokenStore: SessionTokenStore
) {

    val baseUrl: String = BuildConfig.SUPABASE_URL.trim()
        .removeSuffix("/rest/v1/")
        .removeSuffix("/rest/v1")
        .trimEnd('/')

    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && anonKey.isNotBlank()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(SupabaseAuthInterceptor(anonKey) { tokenStore.currentToken() })
            .build()
    }

    /** Builds a PostgREST URL such as `https://project.supabase.co/rest/v1/jobs?select=*`. */
    fun tableUrl(table: String, query: Map<String, String> = emptyMap()): HttpUrl {
        val builder = ("$baseUrl/rest/v1/${table.trimStart('/')}").toHttpUrl().newBuilder()
        query.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        return builder.build()
    }

    /** Builds an Auth (GoTrue) URL such as `https://project.supabase.co/auth/v1/user`. */
    fun authUrl(path: String, query: Map<String, String> = emptyMap()): HttpUrl {
        val builder = ("$baseUrl/auth/v1/${path.trimStart('/')}").toHttpUrl().newBuilder()
        query.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        return builder.build()
    }

    fun newRequest(url: HttpUrl): Request.Builder = Request.Builder().url(url)

    fun jsonBody(json: String): RequestBody =
        json.toRequestBody("application/json; charset=utf-8".toMediaType())

    // ---------------------------------------------------------------------------------------
    // Table (PostgREST) verbs. Each returns the response body on 2xx, or a typed failure.
    // ---------------------------------------------------------------------------------------

    fun get(table: String, query: Map<String, String> = emptyMap()): DataResult<String> =
        execute(tableUrl(table, query)) { builder -> builder.get() }

    fun post(
        table: String,
        body: String,
        query: Map<String, String> = emptyMap(),
        prefer: String? = null
    ): DataResult<String> = execute(tableUrl(table, query)) { builder ->
        builder.header("Prefer", prefer ?: "return=representation").post(jsonBody(body))
    }

    fun patch(
        table: String,
        body: String,
        query: Map<String, String>,
        prefer: String? = null
    ): DataResult<String> = execute(tableUrl(table, query)) { builder ->
        builder.header("Prefer", prefer ?: "return=representation").patch(jsonBody(body))
    }

    fun delete(table: String, query: Map<String, String>): DataResult<String> =
        execute(tableUrl(table, query)) { builder -> builder.delete() }

    /**
     * Runs a request and converts the outcome into a [DataResult].
     *
     * The response body is read inside the `use` block so the connection is always released.
     * Every non-2xx status is classified rather than collapsed into an empty list.
     */
    private fun execute(url: HttpUrl, build: (Request.Builder) -> Request.Builder): DataResult<String> {
        if (!isConfigured) {
            Log.e(TAG, "Supabase is not configured: missing URL or anon key in BuildConfig.")
            return DataResult.ValidationError(
                messageRes = R.string.error_not_configured
            )
        }

        return try {
            val request = build(newRequest(url)).build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    DataResult.Success(body)
                } else {
                    Log.w(TAG, "HTTP ${response.code} for ${url.encodedPath}: $body")
                    classifyHttpFailure(response.code, body)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network failure for ${url.encodedPath}", e)
            DataResult.NetworkError(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected failure for ${url.encodedPath}", e)
            DataResult.ServerError(e)
        }
    }

    private fun classifyHttpFailure(code: Int, body: String): DataResult<Nothing> = when (code) {
        400, 409, 422 -> DataResult.ValidationError(messageRes = postgrestMessageRes(body))
        401, 403 -> DataResult.AuthError()
        in 500..599 -> DataResult.ServerError()
        else -> DataResult.ServerError()
    }

    /**
     * Turns a PostgREST error payload into the most specific message we can show the user.
     *
     * Postgres reports constraint and trigger violations in a `code` field, and our own triggers
     * raise descriptive text in `message`. Matching on that text is what lets us say "your company
     * is not verified" instead of the useless "some details are not valid".
     */
    private fun postgrestMessageRes(body: String): Int {
        val json = runCatching { JSONObject(body) }.getOrNull()
        val code = json?.optString("code").orEmpty()
        val message = json?.optString("message").orEmpty().lowercase()

        return when {
            // 42501 = insufficient_privilege, raised by a failing RLS policy.
            code == "42501" -> R.string.error_auth_expired

            // Raised by enforce_job_rules() when the company is unverified.
            message.contains("verified company") -> R.string.error_company_not_verified

            // Unique (job_id, applicant_id) / (gig_id, bidder_id) violations.
            code == "23505" && message.contains("application") -> R.string.error_duplicate_application
            code == "23505" && message.contains("bid") -> R.string.error_duplicate_bid

            // Raised by the insert policies when the parent row is no longer open.
            message.contains("job") && message.contains("open") -> R.string.error_job_closed
            message.contains("gig") && message.contains("open") -> R.string.error_gig_closed

            else -> R.string.error_validation_failed
        }
    }

    private class SupabaseAuthInterceptor(
        private val anonKey: String,
        private val tokenProvider: () -> String?
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            if (anonKey.isNotBlank()) {
                builder.header("apikey", anonKey)
                // The user's JWT when signed in, otherwise the anon key for public reads.
                val bearer = tokenProvider()?.takeIf { it.isNotBlank() } ?: anonKey
                builder.header("Authorization", "Bearer $bearer")
            }

            return chain.proceed(builder.build())
        }
    }

    companion object {
        private const val TAG = "SupabaseHttpClient"
    }
}
