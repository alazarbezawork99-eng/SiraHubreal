package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Patterns
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.supabase.SupabaseClientConfig
import com.example.model.AccountType
import com.example.model.AuthUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SupabaseAuthRepository(private val context: Context) : AuthRepository {

    private val clientConfig = SupabaseClientConfig.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Shared with the data repositories. This is the only place a session token is written.
     *
     * TODO(step N): replace with constructor injection once this class is a Hilt component.
     * Until then it shares the process-wide singleton so both sides see the same token.
     */
    private val sessionTokenStore: SessionTokenStore = SessionTokenStore.instance

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "secure_auth_session_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to regular prefs", e)
            context.getSharedPreferences("secure_auth_session_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override val accessToken: String?
        get() = encryptedPrefs.getString("access_token", null)

    init {
        // Let every PostgREST call made through SupabaseClientConfig carry the signed-in user's
        // JWT, so Row Level Security policies resolve auth.uid() to the real account.
        SupabaseClientConfig.sessionTokenProvider = { accessToken }
        scope.launch {
            checkInitialSession()
        }
    }

    private suspend fun checkInitialSession() {
        if (!clientConfig.isConnectedAndConfigured) {
            _currentUser.value = null
            _authState.value = AuthState.SignedOut
            return
        }

        try {
            val accessToken = encryptedPrefs.getString("access_token", null)
            val refreshToken = encryptedPrefs.getString("refresh_token", null)

            if (!accessToken.isNullOrBlank()) {
                val user = fetchUserInfo(accessToken)
                if (user != null) {
                    // Restoring an existing session: publish the token so data repositories can
                    // authenticate immediately, without waiting for the first refresh.
                    sessionTokenStore.setToken(accessToken)
                    val authUser = fetchOrCreateProfile(user.getString("id"), user.optString("email", ""), true, accessToken)
                    _currentUser.value = authUser
                    _authState.value = AuthState.SignedIn(authUser)
                    return
                } else if (!refreshToken.isNullOrBlank()) {
                    if (refreshSessionInternal(refreshToken)) {
                        return
                    }
                }
            }

            clearSession()
            _currentUser.value = null
            _authState.value = AuthState.SignedOut
        } catch (e: Exception) {
            Log.e(TAG, "Error checking initial session", e)
            clearSession()
            _currentUser.value = null
            _authState.value = AuthState.SignedOut
        }
    }

    private fun saveSession(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
        // Mirror the JWT into the store the data repositories read from. Without this every
        // PostgREST call would go out with the anon key and RLS would reject the user's writes.
        sessionTokenStore.setToken(accessToken)
    }

    private fun clearSession() {
        encryptedPrefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
        sessionTokenStore.clear()
    }

    private fun fetchUserInfo(accessToken: String): JSONObject? {
        try {
            val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/user"
            val request = Request.Builder()
                .url(url)
                .header("apikey", clientConfig.activeAnonKey)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            clientConfig.httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                return JSONObject(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
            return null
        }
    }

    private fun refreshSessionInternal(refreshToken: String): Boolean {
        try {
            val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/token?grant_type=refresh_token"
            val jsonBody = JSONObject().apply {
                put("refresh_token", refreshToken)
            }.toString()

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .header("apikey", clientConfig.activeAnonKey)
                .header("Authorization", "Bearer ${clientConfig.activeAnonKey}")
                .post(requestBody)
                .build()

            clientConfig.httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    clearSession()
                    _currentUser.value = null
                    _authState.value = AuthState.SignedOut
                    return false
                }
                val body = response.body?.string() ?: return false
                val json = JSONObject(body)
                val newAccessToken = json.optString("access_token", "")
                val newRefreshToken = json.optString("refresh_token", refreshToken)

                if (newAccessToken.isNotBlank()) {
                    saveSession(newAccessToken, newRefreshToken)
                    val user = fetchUserInfo(newAccessToken)
                    if (user != null) {
                        val authUser = fetchOrCreateProfile(user.getString("id"), user.optString("email", ""), true, newAccessToken)
                        _currentUser.value = authUser
                        _authState.value = AuthState.SignedIn(authUser)
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing session", e)
        }
        clearSession()
        _currentUser.value = null
        _authState.value = AuthState.SignedOut
        return false
    }

    private fun fetchOrCreateProfile(
        userId: String,
        email: String,
        isEmailVerified: Boolean,
        accessToken: String? = null,
        fallbackName: String? = null,
        fallbackPhone: String? = null,
        fallbackCity: String? = null,
        fallbackAccountType: String? = null
    ): AuthUser {
        var accountType = accountTypeFromDbValue(fallbackAccountType)
        var fullName = fallbackName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
        var phone = fallbackPhone.orEmpty()
        var city = fallbackCity.orEmpty()
        var profileExists = false

        try {
            val endpoint = clientConfig.getTableEndpoint("profiles", "id=eq.$userId")
            val builder = Request.Builder()
                .url(endpoint)
                .header("apikey", clientConfig.activeAnonKey)
                .get()

            if (!accessToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $accessToken")
            } else {
                builder.header("Authorization", "Bearer ${clientConfig.activeAnonKey}")
            }

            clientConfig.httpClient.newCall(builder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        profileExists = true
                        val p = array.getJSONObject(0)
                        fullName = p.optString("full_name", fullName).ifBlank { fullName }
                        phone = p.optString("phone", phone)
                        city = p.optString("city", city)
                        accountType = accountTypeFromDbValue(p.optString("account_type", ""))
                    }
                }
            }

            // Safety net: the profiles row is normally created by the on_auth_user_created
            // trigger. If that trigger is missing/disabled on the project, create the row here
            // with the user's own JWT so accounts never end up without a profile.
            if (!profileExists && !accessToken.isNullOrBlank()) {
                Log.w(TAG, "No profile row for user $userId - creating it client side.")
                createProfileRow(userId, email, accessToken, fullName, phone, city, accountType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile for user $userId", e)
        }

        return AuthUser(
            id = userId,
            email = email,
            fullName = fullName,
            phone = phone,
            accountType = accountType,
            companyLocation = city,
            isLoggedIn = true,
            isEmailVerified = isEmailVerified,
            registeredDate = "Recently"
        )
    }

    private fun createProfileRow(
        userId: String,
        email: String,
        accessToken: String,
        fullName: String,
        phone: String,
        city: String,
        accountType: AccountType
    ) {
        try {
            // profiles.full_name enforces length(trim(full_name)) between 2 and 120.
            val safeName = fullName.trim().takeIf { it.length >= 2 }
                ?: email.substringBefore("@").trim().takeIf { it.length >= 2 }
                ?: "SiraHub User"

            val json = JSONObject().apply {
                put("id", userId)
                put("account_type", accountTypeToDbValue(accountType))
                put("full_name", safeName.take(120))
                if (phone.isNotBlank()) put("phone", phone)
                if (city.isNotBlank()) put("city", city)
            }

            val request = Request.Builder()
                .url(clientConfig.getTableEndpoint("profiles"))
                .header("apikey", clientConfig.activeAnonKey)
                .header("Authorization", "Bearer $accessToken")
                .header("Prefer", "resolution=ignore-duplicates,return=minimal")
                .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            clientConfig.httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "Failed to create profile row for $userId: ${response.code} $errorBody")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating profile row for user $userId", e)
        }
    }

    private fun accountTypeFromDbValue(value: String?): AccountType =
        when (value?.lowercase()) {
            "company" -> AccountType.COMPANY_EMPLOYER
            "gig_provider" -> AccountType.GIG_CLIENT
            else -> AccountType.JOB_SEEKER
        }

    private fun accountTypeToDbValue(type: AccountType): String = when (type) {
        AccountType.COMPANY_EMPLOYER -> "company"
        AccountType.GIG_CLIENT -> "gig_provider"
        AccountType.JOB_SEEKER -> "job_seeker"
    }

    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        accountType: AccountType
    ): Result<SignUpOutcome> {
        return withContext(Dispatchers.IO) {
            try {
                if (!validateEmail(email)) {
                    return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
                }
                if (!validatePassword(password)) {
                    return@withContext Result.failure(IllegalArgumentException("Password must be at least 8 characters long."))
                }
                val formattedPhone = validateAndFormatEthiopianPhone(phone)
                if (phone.isNotBlank() && formattedPhone == null) {
                    return@withContext Result.failure(IllegalArgumentException("Please enter a valid Ethiopian phone number (+2519... or 09...)."))
                }

                val metaAccountType = when (accountType) {
                    AccountType.COMPANY_EMPLOYER -> "company"
                    AccountType.GIG_CLIENT -> "gig_provider"
                    AccountType.JOB_SEEKER -> "job_seeker"
                }

                val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/signup"
                val jsonBody = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password)
                    put("data", JSONObject().apply {
                        put("full_name", fullName.trim())
                        put("phone", formattedPhone ?: "")
                        put("account_type", metaAccountType)
                    })
                }.toString()

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .header("apikey", clientConfig.activeAnonKey)
                    .header("Authorization", "Bearer ${clientConfig.activeAnonKey}")
                    .post(requestBody)
                    .build()

                clientConfig.httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        Log.e(TAG, "SignUp failed with code ${response.code}: $body")
                        return@withContext Result.failure(Exception(extractAuthError(body)))
                    }

                    val json = runCatching { JSONObject(body) }.getOrNull()

                    // When "Confirm email" is enabled, Supabase only creates a pending auth user and
                    // returns no session. The row in auth.users already exists at this point (and with
                    // it the profiles row from the handle_new_user trigger), but the user is not
                    // signed in yet.
                    val sessionToken = json?.optString("access_token", "").orEmpty()
                    val userObj = json?.optJSONObject("user")

                    if (sessionToken.isNotBlank() && userObj != null) {
                        val userId = userObj.getString("id")
                        val userEmail = userObj.optString("email", email.trim())
                        val isVerified = userObj.optString("email_confirmed_at", "").let {
                            it.isNotBlank() && it != "null"
                        }

                        saveSession(sessionToken, json.optString("refresh_token", ""))

                        val authUser = fetchOrCreateProfile(
                            userId = userId,
                            email = userEmail,
                            isEmailVerified = isVerified,
                            accessToken = sessionToken,
                            fallbackName = fullName.trim(),
                            fallbackPhone = formattedPhone ?: "",
                            fallbackAccountType = metaAccountType
                        )
                        _currentUser.value = authUser
                        _authState.value = AuthState.SignedIn(authUser)
                        return@withContext Result.success(SignUpOutcome.SignedIn(authUser))
                    }

                    clearSession()
                    _currentUser.value = null
                    _authState.value = AuthState.SignedOut
                    Result.success(SignUpOutcome.ConfirmationRequired(email.trim()))
                }
            } catch (e: Exception) {
                Log.e(TAG, "SignUp exception", e)
                Result.failure(Exception(mapSupabaseError(e)))
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!validateEmail(email)) {
                    return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
                }
                if (password.length < 8) {
                    return@withContext Result.failure(IllegalArgumentException("Password must be at least 8 characters long."))
                }

                val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/token?grant_type=password"
                val jsonBody = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password)
                }.toString()

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .header("apikey", clientConfig.activeAnonKey)
                    .header("Authorization", "Bearer ${clientConfig.activeAnonKey}")
                    .post(requestBody)
                    .build()

                clientConfig.httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception(extractAuthError(body)))
                    }
                    val json = JSONObject(body)
                    val accessToken = json.optString("access_token", "")
                    val refreshToken = json.optString("refresh_token", "")
                    val userObj = json.optJSONObject("user")

                    if (accessToken.isBlank() || userObj == null) {
                        return@withContext Result.failure(Exception("Sign in failed. Please try again."))
                    }

                    val emailConfirmedAt = userObj.optString("email_confirmed_at", "")
                    if (emailConfirmedAt.isBlank() || emailConfirmedAt == "null") {
                        return@withContext Result.failure(Exception("Please verify your email address before signing in. Check your inbox for the verification link."))
                    }

                    saveSession(accessToken, refreshToken)
                    val userId = userObj.getString("id")
                    val userEmail = userObj.optString("email", email)
                    val authUser = fetchOrCreateProfile(userId, userEmail, true, accessToken)
                    _currentUser.value = authUser
                    _authState.value = AuthState.SignedIn(authUser)

                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(Exception(mapSupabaseError(e)))
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = encryptedPrefs.getString("access_token", null)
                if (!accessToken.isNullOrBlank()) {
                    val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/logout"
                    val request = Request.Builder()
                        .url(url)
                        .header("apikey", clientConfig.activeAnonKey)
                        .header("Authorization", "Bearer $accessToken")
                        .post("{}".toRequestBody("application/json; charset=utf-8".toMediaType()))
                        .build()
                    clientConfig.httpClient.newCall(request).execute().close()
                }
                clearSession()
                _currentUser.value = null
                _authState.value = AuthState.SignedOut
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign out", e)
                clearSession()
                _currentUser.value = null
                _authState.value = AuthState.SignedOut
                Result.success(Unit)
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!validateEmail(email)) {
                    return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
                }

                val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/recover"
                val jsonBody = JSONObject().apply {
                    put("email", email.trim())
                }.toString()

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .header("apikey", clientConfig.activeAnonKey)
                    .header("Authorization", "Bearer ${clientConfig.activeAnonKey}")
                    .post(requestBody)
                    .build()

                clientConfig.httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        return@withContext Result.failure(Exception(mapSupabaseError(Exception(body))))
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception(mapSupabaseError(e)))
            }
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!validatePassword(newPassword)) {
                    return@withContext Result.failure(IllegalArgumentException("Password must be at least 8 characters long."))
                }

                val accessToken = encryptedPrefs.getString("access_token", null)
                    ?: return@withContext Result.failure(IllegalAccessException("User not signed in."))

                val url = "${clientConfig.activeSupabaseUrl.trimEnd('/')}/auth/v1/user"
                val jsonBody = JSONObject().apply {
                    put("password", newPassword)
                }.toString()

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .header("apikey", clientConfig.activeAnonKey)
                    .header("Authorization", "Bearer $accessToken")
                    .put(requestBody)
                    .build()

                clientConfig.httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        return@withContext Result.failure(Exception(mapSupabaseError(Exception(body))))
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception(mapSupabaseError(e)))
            }
        }
    }

    override suspend fun refreshSession(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = encryptedPrefs.getString("refresh_token", null)
                if (!refreshToken.isNullOrBlank()) {
                    if (refreshSessionInternal(refreshToken)) {
                        return@withContext Result.success(Unit)
                    }
                }
                Result.failure(Exception("Session expired. Please sign in again."))
            } catch (e: Exception) {
                Result.failure(Exception(mapSupabaseError(e)))
            }
        }
    }

    companion object {
        private const val TAG = "SupabaseAuthRepository"

        /**
         * Pulls the human readable text out of a GoTrue error payload instead of feeding the raw
         * JSON body to [mapSupabaseError], which made failures such as "User already registered"
         * or "Database error saving new user" surface as opaque text.
         */
        fun extractAuthError(body: String): String {
            val raw = body.ifBlank { "Request failed. Please try again." }
            val message = try {
                val json = JSONObject(raw)
                json.optString("msg").ifBlank {
                    json.optString("error_description").ifBlank {
                        json.optString("message").ifBlank {
                            json.optString("error").ifBlank { raw }
                        }
                    }
                }
            } catch (e: Exception) {
                raw
            }
            return mapSupabaseError(Exception(message))
        }

        fun validateEmail(email: String): Boolean {
            return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        }

        fun validatePassword(password: String): Boolean {
            return password.length >= 8
        }

        fun validateAndFormatEthiopianPhone(phone: String): String? {
            val cleaned = phone.trim().replace("\\s".toRegex(), "")
            val regex = Regex("^(\\+251|0)([97]\\d{8})$")
            val match = regex.matchEntire(cleaned)
            return if (match != null) {
                "+251${match.groupValues[2]}"
            } else {
                null
            }
        }

        fun mapSupabaseError(e: Throwable): String {
            val msg = e.localizedMessage?.lowercase() ?: ""
            return when {
                msg.contains("invalid login") || msg.contains("invalid credentials") || msg.contains("invalid grant") || msg.contains("invalid_grant") ->
                    "Invalid email or password. Please check your credentials."
                msg.contains("email not confirmed") || msg.contains("not verified") ->
                    "Please verify your email address before signing in. Check your inbox for the verification link."
                msg.contains("already registered") || msg.contains("already exists") || msg.contains("user already registered") || msg.contains("already_exists") ->
                    "This email address is already registered. Please sign in instead."
                msg.contains("network") || msg.contains("connect") || e is IOException ->
                    "No internet connection. Please check your network and try again."
                msg.contains("rate limit") || msg.contains("too many requests") || msg.contains("attempts") ->
                    "Too many attempts. Please wait a few minutes before trying again."
                else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
            }
        }
    }
}
