package com.example.model

enum class GigPricingType(val label: String) {
    FIXED("Fixed Budget"),
    HOURLY("Hourly Rate"),
    DAILY("Daily Rate")
}

enum class GigStatus(val label: String) {
    OPEN("Accepting Bids"),
    UNDER_REVIEW("Reviewing Proposals"),
    ASSIGNED("In Progress"),
    COMPLETED("Completed")
}

data class GigPost(
    val id: String,
    val title: String,
    val clientName: String,
    val clientType: String, // "Individual", "Local Startup", "Shop Owner", "NGO Agent", etc.
    val location: String, // e.g. "Addis Ababa (Bole/Atlas)", "Addis Ababa (Piassa)", "Remote Ethiopia"
    val category: String, // "Translation & Writing", "Graphic Design", "Web & Mobile Dev", "Tech & IT Support", "Photography & Video", "Errands & Logistics", "Tutoring", "Quick Fix / Trades"
    val budgetETB: Long,
    val pricingType: GigPricingType,
    val durationText: String, // "2 Days", "1 Week", "Urgent - Today", "Weekend Project"
    val description: String,
    val deliverables: List<String>,
    val requiredSkills: List<String>,
    val bidsCount: Int,
    val postedTimeAgo: String,
    val status: GigStatus = GigStatus.OPEN,
    val clientPhone: String? = null,
    val clientTelegram: String? = null,
    val isPostedByUser: Boolean = false,
    val isUrgent: Boolean = false
) {
    val formattedBudget: String
        get() = "${String.format("%,d", budgetETB)} ETB ${pricingType.label}"
}
