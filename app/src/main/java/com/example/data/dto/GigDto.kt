package com.example.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

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
     * The embedded `gig_contacts` join, kept as raw JSON on purpose.
     *
     * `gig_contacts.gig_id` is UNIQUE, so PostgREST treats this as a to-one relationship and
     * returns a single object (`"gig_contacts": {...}`). If that constraint were ever dropped it
     * would return an array instead. Decoding into a concrete type would break the moment the
     * cardinality changed, so the raw form is kept and normalised by [contactOrNull].
     *
     * Null is the normal case for anonymous browsing: RLS simply omits the row.
     */
    @SerialName("gig_contacts") val contacts: JsonElement? = null
) {
    /** The embedded contact row, whichever shape PostgREST chose to send. */
    fun contactOrNull(): GigContactDto? = when (val element = contacts) {
        null, is JsonNull -> null
        is JsonObject -> GigContactDto.from(element)
        is JsonArray -> element.firstOrNull()?.let { item ->
            (item as? JsonObject)?.let(GigContactDto::from)
        }
        else -> null
    }
}

/** Wire representation of a row in `public.gig_contacts`. */
@Serializable
data class GigContactDto(
    @SerialName("gig_id") val gigId: String? = null,
    @SerialName("client_phone") val clientPhone: String? = null,
    @SerialName("client_telegram") val clientTelegram: String? = null
) {
    companion object {
        /**
         * Reads a contact out of an embedded object.
         *
         * The embedded form only contains the columns named in the `select`, so `gig_id` is
         * usually absent; every field is therefore read defensively.
         */
        fun from(json: JsonObject): GigContactDto = GigContactDto(
            gigId = json.stringOrNull("gig_id"),
            clientPhone = json.stringOrNull("client_phone"),
            clientTelegram = json.stringOrNull("client_telegram")
        )

        private fun JsonObject.stringOrNull(key: String): String? =
            (this[key] as? JsonElement)
                ?.takeIf { it !is JsonNull }
                ?.jsonPrimitive
                ?.content
                ?.takeIf { it.isNotBlank() }
    }
}

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
