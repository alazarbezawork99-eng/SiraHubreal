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
}
