package com.example.data.auth

import com.example.model.AccountType
import com.example.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Result of a successful sign-up request.
 *
 * Supabase returns a live session immediately when "Confirm email" is disabled in the project's
 * Auth settings, and only a *pending* user when it is enabled. The UI must distinguish the two,
 * because in the second case the user is not actually signed in yet.
 */
sealed interface SignUpOutcome {
    data class SignedIn(val user: AuthUser) : SignUpOutcome
    data class ConfirmationRequired(val email: String) : SignUpOutcome
}

interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentUser: StateFlow<AuthUser?>

    /** Raw Supabase access token (JWT) of the active session, or null when signed out. */
    val accessToken: String?

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        accountType: AccountType
    ): Result<SignUpOutcome>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun refreshSession(): Result<Unit>
}
