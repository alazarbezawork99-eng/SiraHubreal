package com.example.model

data class AuthUser(
    val id: String = "user_default_1",
    val email: String = "abebe.kebede.dev@gmail.com",
    val fullName: String = "Abebe Kebede",
    val phone: String = "+251 91 123 4567",
    val accountType: AccountType = AccountType.JOB_SEEKER,
    val companyName: String = "",
    val companyIndustry: String = "",
    val companyLocation: String = "Addis Ababa, Ethiopia",
    val headline: String = "Senior Mobile & Backend Engineer",
    val isLoggedIn: Boolean = false,
    val registeredDate: String = "October 2024"
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
    val autoSyncSupabase: Boolean = true,
    val darkThemeEnabled: Boolean = true,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val currency: String = "ETB"
)

enum class AuthMode {
    REGISTER,
    LOGIN
}

