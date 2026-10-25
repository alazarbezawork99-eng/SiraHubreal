package com.example.data.mapper

import com.example.data.dto.GigContactDto
import com.example.data.dto.GigDto
import com.example.model.GigPricingType
import com.example.model.GigStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GigMapperTest {

    private fun dto(
        id: String = "22222222-2222-2222-2222-222222222222",
        ownerId: String = "owner-1",
        pricingType: String = "fixed",
        status: String = "open",
        contacts: List<GigContactDto>? = null,
        createdAt: String? = "2024-05-01T10:00:00.123456+00:00"
    ) = GigDto(
        id = id,
        ownerId = ownerId,
        title = "Design a coffee shop logo",
        clientName = "Kaffa Origin Coffee",
        clientType = "Shop Owner",
        location = "Addis Ababa",
        category = "Graphic Design",
        budgetEtb = 17_500,
        pricingType = pricingType,
        durationText = "5 Days",
        description = "Culturally inspired branding with Jebena motifs.",
        deliverables = listOf("Logo", "Brand guide"),
        requiredSkills = listOf("Illustrator", "Branding"),
        isUrgent = true,
        status = status,
        bidsCount = 4,
        createdAt = createdAt,
        contacts = contacts
    )

    @Test
    fun `snake_case enums map to UI enums`() {
        val entity = GigMapper.dtoToEntity(dto(pricingType = "hourly", status = "under_review"))
        val gig = GigMapper.entityToDomain(entity, contact = null, currentUserId = null)

        assertEquals(GigPricingType.HOURLY, gig.pricingType)
        assertEquals(GigStatus.UNDER_REVIEW, gig.status)
    }

    @Test
    fun `unknown enum values fall back instead of throwing`() {
        val entity = GigMapper.dtoToEntity(dto(pricingType = "barter", status = "archived"))
        val gig = GigMapper.entityToDomain(entity, contact = null, currentUserId = null)

        assertEquals(GigPricingType.FIXED, gig.pricingType)
        assertEquals(GigStatus.OPEN, gig.status)
    }

    @Test
    fun `text array columns survive the round trip through Room`() {
        val entity = GigMapper.dtoToEntity(dto())
        val gig = GigMapper.entityToDomain(entity, contact = null, currentUserId = null)

        assertEquals(listOf("Logo", "Brand guide"), gig.deliverables)
        assertEquals(listOf("Illustrator", "Branding"), gig.requiredSkills)
    }

    @Test
    fun `contact details are null when the caller may not read them`() {
        // RLS hides gig_contacts from everyone but the owner and the winning bidder, so the
        // embedded join is simply absent. That must not be treated as an error.
        val entity = GigMapper.dtoToEntity(dto(contacts = null))
        val gig = GigMapper.entityToDomain(entity, contact = null, currentUserId = null)

        assertNull(gig.clientPhone)
        assertNull(gig.clientTelegram)
    }

    @Test
    fun `contact details are mapped when present`() {
        val dtoWithContacts = dto(
            contacts = listOf(GigContactDto("g1", "+251911234567", "@kaffa"))
        )
        val contact = GigMapper.dtoToContactEntity(dtoWithContacts)

        assertEquals("+251911234567", contact?.clientPhone)
        assertEquals("@kaffa", contact?.clientTelegram)
    }

    @Test
    fun `an all-blank contact row is not cached`() {
        val blank = dto(contacts = listOf(GigContactDto("g1", null, "")))
        assertNull(GigMapper.dtoToContactEntity(blank))
    }

    @Test
    fun `isPostedByUser is true only for the owner`() {
        val entity = GigMapper.dtoToEntity(dto(ownerId = "owner-1"))

        assertTrue(GigMapper.entityToDomain(entity, null, "owner-1").isPostedByUser)
        assertFalse(GigMapper.entityToDomain(entity, null, "someone-else").isPostedByUser)
        assertFalse(GigMapper.entityToDomain(entity, null, null).isPostedByUser)
    }

    @Test
    fun `creating a gig omits a contact row when no details were given`() {
        val entity = GigMapper.dtoToEntity(dto())
        val gig = GigMapper.entityToDomain(entity, contact = null, currentUserId = null)

        assertNull(GigMapper.domainToNewContactDto("g1", gig))
    }
}
