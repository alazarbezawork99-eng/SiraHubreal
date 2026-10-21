package com.example.model

enum class EmploymentType(val label: String) {
    FULL_TIME("Full-time"),
    PART_TIME("Part-time"),
    CONTRACT("Contract"),
    INTERNSHIP("Internship")
}

enum class WorkplaceType(val label: String) {
    ON_SITE("On-site"),
    HYBRID("Hybrid"),
    REMOTE("Remote")
}

enum class ExperienceLevel(val label: String) {
    ENTRY("Entry Level"),
    MID("Mid Level (2-4 yrs)"),
    SENIOR("Senior Level (5+ yrs)"),
    LEAD("Lead / Manager")
}

data class JobPost(
    val id: String,
    val title: String,
    val company: String,
    val location: String, // e.g. "Addis Ababa (Bole)", "Hawassa Industrial Park", "Remote (Ethiopia)"
    val category: String, // "Tech & Software", "Banking & Finance", "NGO & Development", etc.
    val employmentType: EmploymentType,
    val workplaceType: WorkplaceType,
    val experienceLevel: ExperienceLevel,
    val salaryMinETB: Long,
    val salaryMaxETB: Long,
    val salaryPeriod: String = "month", // "month" or "project"
    val description: String,
    val responsibilities: List<String>,
    val requirements: List<String>,
    val benefits: List<String>,
    val postedTimeAgo: String,
    val deadline: String,
    val applicantsCount: Int,
    val isFeatured: Boolean = false,
    val isUrgent: Boolean = false,
    val isVerifiedCompany: Boolean = true,
    val contactEmail: String,
    val contactPhone: String? = null,
    val isPostedByUser: Boolean = false
) {
    val formattedSalary: String
        get() = if (salaryMinETB > 0 && salaryMaxETB > 0) {
            "${String.format("%,d", salaryMinETB)} - ${String.format("%,d", salaryMaxETB)} ETB / $salaryPeriod"
        } else if (salaryMinETB > 0) {
            "From ${String.format("%,d", salaryMinETB)} ETB / $salaryPeriod"
        } else {
            "Negotiable (ETB)"
        }
}
