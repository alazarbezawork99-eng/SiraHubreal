package com.example.model

enum class AccountType(val label: String, val subtitle: String, val amharicBadge: String) {
    JOB_SEEKER("Job Seeker", "Find jobs, bid on gigs & build digital CV", "ስራ ፈላጊ"),
    COMPANY_EMPLOYER("Company", "Post jobs, review applicant CVs & hire talent", "ድርጅት"),
    GIG_CLIENT("Gig Provider", "Post freelance gigs & tasks, review bids & hire", "ቀጣሪ")
}

data class CompanyProfile(
    val id: String = "",
    val name: String = "",
    val industry: String = "",
    val location: String = "",
    val website: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val description: String = "",
    val isVerified: Boolean = false,
    val employeesCount: String = "",
    val foundedYear: String = ""
)

data class CompanyApplicantReview(
    val application: JobApplication,
    val applicantProfile: UserProfile?
)
