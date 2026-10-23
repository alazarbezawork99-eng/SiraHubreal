package com.example.model

data class AuthUser(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val accountType: AccountType = AccountType.JOB_SEEKER,
    val companyName: String = "",
    val companyIndustry: String = "",
    val companyLocation: String = "Addis Ababa, Ethiopia",
    val headline: String = "",
    val isLoggedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val registeredDate: String = ""
) {
    val isGuest: Boolean
        get() = !isLoggedIn

    val displayName: String
        get() = if (accountType == AccountType.COMPANY_EMPLOYER && companyName.isNotBlank()) companyName else fullName

    val initials: String
        get() {
            val name = displayName.trim()
            if (name.isBlank()) return "U"
            val parts = name.split(" ").filter { it.isNotBlank() }
            return if (parts.size >= 2) {
                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            } else {
                name.take(2).uppercase()
            }
        }
}

data class AppSettings(
    val selectedLanguage: String = "English", // "English", "አማርኛ (Amharic)", "Afaan Oromoo"
    val notificationsEnabled: Boolean = true,
    val jobAlertsEnabled: Boolean = true,
    val bidAlertsEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = true,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val currency: String = "ETB"
)

enum class AuthMode {
    REGISTER,
    LOGIN
}

