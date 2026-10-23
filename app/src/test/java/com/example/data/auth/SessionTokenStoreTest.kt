package com.example.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Guards the contract that broke job posting: the auth layer writes the JWT into the store that
 * the HTTP client and repositories read from. If these ever diverge again, every authenticated
 * PostgREST write silently goes out as an anonymous request and RLS rejects it.
 */
class SessionTokenStoreTest {

    @Test
    fun `token set by auth layer is visible to readers`() {
        val store = SessionTokenStore()

        store.setToken("jwt-abc")

        assertEquals("jwt-abc", store.currentToken())
        assertEquals("jwt-abc", store.accessToken.value)
    }

    @Test
    fun `clearing the session removes the token`() {
        val store = SessionTokenStore()
        store.setToken("jwt-abc")

        store.clear()

        assertNull(store.currentToken())
        assertNull(store.accessToken.value)
    }

    @Test
    fun `blank tokens are treated as signed out`() {
        val store = SessionTokenStore()

        store.setToken("")
        assertNull(store.currentToken())

        store.setToken("   ")
        assertNull(store.currentToken())

        store.setToken(null)
        assertNull(store.currentToken())
    }

    @Test
    fun `companion instance is the one handed to hilt`() {
        // SupabaseAuthRepository is not a Hilt component yet, so it writes to the companion
        // instance. CoroutinesModule provides that same object to every repository.
        val shared = SessionTokenStore.instance

        shared.setToken("jwt-shared")
        try {
            assertEquals("jwt-shared", SessionTokenStore.instance.currentToken())
        } finally {
            shared.clear()
        }
    }
}
