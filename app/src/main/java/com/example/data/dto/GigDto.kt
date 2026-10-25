package com.example.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire representation of a row in `public.gigs`.
 *
 * Field names and nullability mirror `supabase/migrations/0001_schema.sql`. Client contact details
 * are NOT part of this row: they live in `public.gig_contacts`, which is protected by its own RLS
 * policy and only readable by the gig owner or the winning bidder. They are modelled separately in
 * [GigContactDto].
 */
@Serializable
data class GigDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    @SerialName("client_name") val clientName: String,
    @SerialName("client_type") val clientType: String? = null,
    val location: String = "Addis Ababa",
    val category: String,
    @SerialName("budget_etb") val budgetEtb: Long,
    @SerialName("pricing_type") val pricingType: String = "fixed",
    @SerialName("duration_text") val durationText: String,
    val description: String,
    val deliverables: List<String> = emptyList(),
    @SerialName("required_skills") val requiredSkills: List<String> = emptyList(),
    @SerialName("is_urgent") val isUrgent: Boolean = false,
    val status: String = "open",
    @SerialName("bids_count") val bidsCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    /**
     * Present only when the row was fetched with an embedded `gig_contacts` join and the caller is
     * permitted to read it. Null is the normal case for anonymous browsing.
     */
    @SerialName("gig_contacts") val contacts: List<GigContactDto>? = null
)

/** Wire representation of a row in `public.gig_contacts`. */
@Serializable
data class GigContactDto(
    @SerialName("gig_id") val gigId: String,
    @SerialName("client_phone") val clientPhone: String? = null,
    @SerialName("client_telegram") val clientTelegram: String? = null
)

/** Body sent when creating a gig. Only columns the client is allowed to set. */
@Serializable
data class NewGigDto(
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    @SerialName("client_name") val clientName: String,
    @SerialName("client_type") val clientType: String? = null,
    val location: String,
    val category: String,
    @SerialName("budget_etb") val budgetEtb: Long,
    @SerialName("pricing_type") val pricingType: String,
    @SerialName("duration_text") val durationText: String,
    val description: String,
    val deliverables: List<String>,
    @SerialName("required_skills") val requiredSkills: List<String>,
    @SerialName("is_urgent") val isUrgent: Boolean = false
)

/** Body sent when creating the contact row that accompanies a new gig. */
@Serializable
data class NewGigContactDto(
    @SerialName("gig_id") val gigId: String,
    @SerialName("client_phone") val clientPhone: String? = null,
    @SerialName("client_telegram") val clientTelegram: String? = null
)
