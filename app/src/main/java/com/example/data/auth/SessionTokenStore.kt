package com.example.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the Supabase access token (JWT) of the current session in memory.
 *
 * This replaces the old `SupabaseClientConfig.sessionTokenProvider` mutable global. The HTTP client
 * reads from this store to attach `Authorization: Bearer <jwt>` to every PostgREST request, so Row
 * Level Security resolves `auth.uid()` to the real account.
 *
 * [SupabaseAuthRepository] is the only writer; every data repository is a reader.
 *
 * Constructed once in [companion] and provided to Hilt from `CoroutinesModule`, so the writer
 * (auth) and the readers (HTTP interceptor, repositories) share one instance.
 */
class SessionTokenStore {

    private val _accessToken = MutableStateFlow<String?>(null)

    /** Null when signed out. */
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    /** Synchronous read for the OkHttp interceptor, which runs on a background thread. */
    fun currentToken(): String? = _accessToken.value

    fun setToken(token: String?) {
        _accessToken.value = token?.takeIf { it.isNotBlank() }
    }

    fun clear() {
        _accessToken.value = null
    }

    companion object {
        /**
         * The single instance Hilt hands to every data repository.
         *
         * [SupabaseAuthRepository] is not yet a Hilt component, but it is the only thing that ever
         * writes a token. It reads this reference so the writer and the readers share one store.
         * When the auth layer is migrated to Hilt this companion goes away.
         */
        @Volatile
        var instance: SessionTokenStore = SessionTokenStore()
            internal set
    }
}
