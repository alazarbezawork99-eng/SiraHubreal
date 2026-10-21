package com.example.model

enum class ApplicationStatus(val label: String) {
    APPLIED("Application Sent"),
    UNDER_REVIEW("CV Under Review"),
    SHORTLISTED("Shortlisted"),
    INTERVIEW("Interview Invited"),
    OFFER("Job Offer Made"),
    REJECTED("Not Selected")
}

data class JobApplication(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val location: String,
    val applicantName: String,
    val applicantEmail: String,
    val applicantPhone: String,
    val applicantHeadline: String = "Professional Candidate",
    val applicantSkills: List<String> = listOf("Kotlin", "Android", "Amharic & English"),
    val applicantEducation: String = "Addis Ababa University (AAU)",
    val applicantExperience: String = "3+ years relevant industry experience",
    val coverNote: String,
    val expectedSalaryETB: Long,
    val appliedDate: String,
    val status: ApplicationStatus = ApplicationStatus.APPLIED
)

enum class BidStatus(val label: String) {
    SUBMITTED("Bid Active"),
    CLIENT_REVIEWING("Client Reviewing"),
    SHORTLISTED("Proposal Shortlisted"),
    ACCEPTED("Bid Accepted!"),
    COMPLETED("Task Completed"),
    DECLINED("Bid Passed")
}

data class GigBid(
    val id: String,
    val gigId: String,
    val gigTitle: String,
    val clientName: String,
    val bidderName: String,
    val bidderPhone: String,
    val bidAmountETB: Long,
    val deliveryDays: String, // e.g. "3 Days", "1 Week"
    val proposalPitch: String,
    val submittedDate: String,
    val status: BidStatus = BidStatus.SUBMITTED
) {
    val formattedBid: String
        get() = "${String.format("%,d", bidAmountETB)} ETB"
}
