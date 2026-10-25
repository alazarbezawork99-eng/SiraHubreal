package com.example.data.auth

import android.util.Base64
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

/**
 * Resolves the signed-in user's id from the Supabase JWT.
 *
 * The `sub` claim is decoded from the token rather than stored separately, so there is exactly one
 * source of identity and it can never drift from the session. An unparseable or absent token yields
 * null, which callers treat as "signed out" - never as an error worth surfacing.
 */
@Singleton
class CurrentUserProvider @Inject constructor(
    private val tokenStore: SessionTokenStore
) {

    fun userId(): String? {
        val token = tokenStore.currentToken() ?: return null
        return runCatching {
            val parts = token.split('.')
            if (parts.size < 2) return null

            val decoded = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            JSONObject(String(decoded, Charsets.UTF_8))
                .optString("sub")
                .takeIf { it.isNotBlank() }
        }.onFailure {
            Log.w(TAG, "Could not decode the JWT subject", it)
        }.getOrNull()
    }

    companion object {
        private const val TAG = "CurrentUserProvider"
    }
}
