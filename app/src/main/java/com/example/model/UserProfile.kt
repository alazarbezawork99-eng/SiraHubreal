package com.example.model

data class EducationItem(
    val id: String,
    val degree: String,
    val fieldOfStudy: String,
    val institution: String, // e.g. "Addis Ababa University", "ASTU", "Hawassa University", "Jimma University"
    val startYear: String,
    val endYear: String
)

data class ExperienceItem(
    val id: String,
    val roleTitle: String,
    val companyName: String,
    val location: String, // e.g. "Addis Ababa, Ethiopia", "Remote"
    val startDate: String,
    val endDate: String,
    val description: String
)

data class PortfolioLink(
    val id: String,
    val title: String,
    val url: String
)

data class UserProfile(
    val id: String = "user_default",
    val fullName: String = "",
    val titleHeadline: String = "",
    val bioSummary: String = "",
    val email: String = "",
    val phone: String = "",
    val telegramHandle: String = "",
    val cityLocation: String = "Addis Ababa, Ethiopia",
    val skills: List<String> = emptyList(),
    val languages: List<String> = listOf("Amharic", "English"),
    val educationList: List<EducationItem> = emptyList(),
    val experienceList: List<ExperienceItem> = emptyList(),
    val portfolioLinks: List<PortfolioLink> = emptyList(),
    val expectedSalaryMinETB: Long = 0,
    val availabilityStatus: String = "Actively seeking opportunities",
    val resumeUploadedDate: String = "Not uploaded yet"
) {
    val completionPercentage: Int
        get() {
            var score = 0
            if (fullName.isNotBlank()) score += 15
            if (email.isNotBlank()) score += 10
            if (phone.isNotBlank()) score += 10
            if (titleHeadline.isNotBlank()) score += 15
            if (bioSummary.isNotBlank()) score += 15
            if (skills.isNotEmpty()) score += 15
            if (experienceList.isNotEmpty()) score += 10
            if (educationList.isNotEmpty()) score += 10
            return score.coerceAtMost(100)
        }

    companion object {
        val DEMO_ABEBE = UserProfile(
            id = "user_abebe_1",
            fullName = "Abebe Kebede",
            titleHeadline = "Senior Mobile & Backend Engineer",
            bioSummary = "Passionate Ethiopian software developer with 4+ years building high-impact Android applications and fintech integrations (Telebirr, CBE Birr). Eager to contribute to scaling tech and innovative business solutions.",
            email = "abebe.kebede.dev@gmail.com",
            phone = "+251 91 123 4567",
            telegramHandle = "@abebek_dev",
            cityLocation = "Addis Ababa (Bole), Ethiopia",
            skills = listOf("Kotlin", "Jetpack Compose", "Android SDK", "Telebirr API", "Spring Boot", "Git", "Amharic & English Fluency"),
            languages = listOf("Amharic (Native)", "English (Fluent)", "Afaan Oromoo (Conversational)"),
            educationList = listOf(
                EducationItem(
                    id = "edu_1",
                    degree = "B.Sc. in Computer Science",
                    fieldOfStudy = "Software Engineering",
                    institution = "Addis Ababa University (AAU - 4 Kilo)",
                    startYear = "2018",
                    endYear = "2022"
                )
            ),
            experienceList = listOf(
                ExperienceItem(
                    id = "exp_1",
                    roleTitle = "Android Software Engineer",
                    companyName = "EthioFintech Solutions",
                    location = "Addis Ababa (Kazanchis)",
                    startDate = "2022",
                    endDate = "Present",
                    description = "Developed retail merchant mobile application integrated with Telebirr and CBE payment gateways serving over 20,000 daily transactions."
                ),
                ExperienceItem(
                    id = "exp_2",
                    roleTitle = "Junior Mobile Developer",
                    companyName = "Habesha Digital Lab",
                    location = "Addis Ababa",
                    startDate = "2021",
                    endDate = "2022",
                    description = "Maintained client apps and built responsive UI layouts with Jetpack Compose and Kotlin coroutines."
                )
            ),
            portfolioLinks = listOf(
                PortfolioLink("port_1", "GitHub Profile", "https://github.com/abebe-ethio-dev"),
                PortfolioLink("port_2", "LinkedIn", "https://linkedin.com/in/abebe-kebede-eth")
            ),
            expectedSalaryMinETB = 45000,
            availabilityStatus = "Available for Full-time & Short Gigs",
            resumeUploadedDate = "Updated Recently"
        )
    }
}
