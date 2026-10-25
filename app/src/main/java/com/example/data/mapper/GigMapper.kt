package com.example.data.mapper

import com.example.data.dto.GigDto
import com.example.data.dto.NewGigContactDto
import com.example.data.dto.NewGigDto
import com.example.data.local.entity.GigContactEntity
import com.example.data.local.entity.GigEntity
import com.example.model.GigPost
import com.example.model.GigPricingType
import com.example.model.GigStatus
import kotlinx.serialization.json.Json
import java.util.Locale

/**
 * Translates between the three representations of a gig:
 *
 *   GigDto (wire)  <->  GigEntity + GigContactEntity (Room cache)  <->  GigPost (UI model)
 *
 * Two things need care here:
 *
 *  - `client_phone` / `client_telegram` live in `public.gig_contacts`, which RLS only exposes to the
 *    gig owner or the winning bidder. For everyone else the contact fields are genuinely absent, so
 *    they map to null rather than to an empty string or a placeholder.
 *  - `status` is a Postgres enum (`open`, `under_review`, `assigned`, `completed`) while the UI enum
 *    is SCREAMING_SNAKE. Unknown values fall back to OPEN rather than throwing.
 */
object GigMapper {

    private val json = Json { ignoreUnknownKeys = true }

    // ---------------------------------------------------------------------------------------
    // Wire -> Room
    // ---------------------------------------------------------------------------------------

    fun dtoToEntity(dto: GigDto): GigEntity = GigEntity(
        id = dto.id,
        ownerId = dto.ownerId,
        title = dto.title,
        clientName = dto.clientName,
        clientType = dto.clientType,
        location = dto.location,
        category = dto.category,
        budgetEtb = dto.budgetEtb,
        pricingType = dto.pricingType,
        durationText = dto.durationText,
        description = dto.description,
        deliverables = json.encodeToString(dto.deliverables),
        requiredSkills = json.encodeToString(dto.requiredSkills),
        isUrgent = dto.isUrgent,
        status = dto.status,
        bidsCount = dto.bidsCount,
        createdAt = dto.createdAt.orEmpty()
    )

    /**
     * Extracts the contact row for a gig, if the caller was allowed to read it.
     *
     * Returns null when the embedded join is absent, which is the normal case for anonymous
     * browsing and is not an error.
     */
    fun dtoToContactEntity(dto: GigDto): GigContactEntity? {
        val contact = dto.contacts?.firstOrNull() ?: return null
        if (contact.clientPhone.isNullOrBlank() && contact.clientTelegram.isNullOrBlank()) return null
        return GigContactEntity(
            gigId = dto.id,
            clientPhone = contact.clientPhone?.takeIf { it.isNotBlank() },
            clientTelegram = contact.clientTelegram?.takeIf { it.isNotBlank() }
        )
    }

    // ---------------------------------------------------------------------------------------
    // Room -> UI model
    // ---------------------------------------------------------------------------------------

    /**
     * @param currentUserId the signed-in user's id, used to decide [GigPost.isPostedByUser].
     * @param contact the cached contact row, or null when the user may not read it.
     */
    fun entityToDomain(
        entity: GigEntity,
        contact: GigContactEntity?,
        currentUserId: String?
    ): GigPost = GigPost(
        id = entity.id,
        title = entity.title,
        clientName = entity.clientName,
        clientType = entity.clientType.orEmpty(),
        location = entity.location,
        category = entity.category,
        budgetETB = entity.budgetEtb,
        pricingType = pricingTypeFromDb(entity.pricingType),
        durationText = entity.durationText,
        description = entity.description,
        deliverables = decodeStringList(entity.deliverables),
        requiredSkills = decodeStringList(entity.requiredSkills),
        bidsCount = entity.bidsCount,
        postedTimeAgo = DateTimeFormatting.relativeTime(entity.createdAt),
        status = statusFromDb(entity.status),
        clientPhone = contact?.clientPhone,
        clientTelegram = contact?.clientTelegram,
        isPostedByUser = currentUserId != null && entity.ownerId == currentUserId,
        isUrgent = entity.isUrgent
    )

    // ---------------------------------------------------------------------------------------
    // UI model -> wire (create)
    // ---------------------------------------------------------------------------------------

    fun domainToNewDto(gig: GigPost, ownerId: String): NewGigDto = NewGigDto(
        ownerId = ownerId,
        title = gig.title.trim(),
        clientName = gig.clientName.trim(),
        clientType = gig.clientType.takeIf { it.isNotBlank() },
        location = gig.location.trim(),
        category = gig.category.trim(),
        budgetEtb = gig.budgetETB,
        pricingType = pricingTypeToDb(gig.pricingType),
        durationText = gig.durationText.trim(),
        description = gig.description.trim(),
        deliverables = gig.deliverables,
        requiredSkills = gig.requiredSkills,
        isUrgent = gig.isUrgent
    )

    fun domainToNewContactDto(gigId: String, gig: GigPost): NewGigContactDto? {
        val phone = gig.clientPhone?.takeIf { it.isNotBlank() }
        val telegram = gig.clientTelegram?.takeIf { it.isNotBlank() }
        if (phone == null && telegram == null) return null
        return NewGigContactDto(gigId = gigId, clientPhone = phone, clientTelegram = telegram)
    }

    // ---------------------------------------------------------------------------------------
    // Enum conversion
    // ---------------------------------------------------------------------------------------

    fun pricingTypeFromDb(value: String): GigPricingType =
        GigPricingType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: GigPricingType.FIXED

    fun statusFromDb(value: String): GigStatus =
        GigStatus.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: GigStatus.OPEN

    fun pricingTypeToDb(value: GigPricingType): String = value.name.lowercase(Locale.US)

    fun statusToDb(value: GigStatus): String = value.name.lowercase(Locale.US)

    private fun decodeStringList(encoded: String): List<String> =
        if (encoded.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<String>>(encoded) }.getOrDefault(emptyList())
}
