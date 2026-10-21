package com.example.model

enum class AccountType(val label: String, val subtitle: String, val amharicBadge: String) {
    JOB_SEEKER("Job Seeker", "Find jobs, bid on gigs & build digital CV", "ስራ ፈላጊ"),
    COMPANY_EMPLOYER("Company", "Post jobs, review applicant CVs & hire talent", "ድርጅት"),
    GIG_CLIENT("Gig Provider", "Post freelance gigs & tasks, review bids & hire", "ቀጣሪ")
}

data class CompanyProfile(
    val id: String = "comp_default_1",
    val name: String = "EthioFintech & Tech Labs",
    val industry: String = "Technology & Financial Services",
    val location: String = "Addis Ababa (Bole Atlas), Ethiopia",
    val website: String = "https://ethiofintech.et",
    val contactEmail: String = "recruitment@ethiofintech.et",
    val contactPhone: String = "+251 91 144 5566",
    val description: String = "Leading Ethiopian digital finance and software consultancy specializing in enterprise payment infrastructure, mobile banking, and Telebirr integrations.",
    val isVerified: Boolean = true,
    val employeesCount: String = "50-100 Employees",
    val foundedYear: String = "2020"
)

data class CompanyApplicantReview(
    val application: JobApplication,
    val applicantProfile: UserProfile?
)
