package com.example.data.mapper

import com.example.data.dto.JobDto
import com.example.model.EmploymentType
import com.example.model.ExperienceLevel
import com.example.model.WorkplaceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class JobMapperTest {

    private fun dto(
        id: String = "11111111-1111-1111-1111-111111111111",
        postedBy: String = "user-1",
        employmentType: String = "full_time",
        workplaceType: String = "hybrid",
        experienceLevel: String = "senior",
        expiresAt: String? = null,
        createdAt: String? = "2024-05-01T10:00:00.123456+00:00"
    ) = JobDto(
        id = id,
        postedBy = postedBy,
        title = "Senior Android Engineer",
        company = "Ethio Telecom",
        location = "Addis Ababa",
        category = "Tech & Software",
        employmentType = employmentType,
        workplaceType = workplaceType,
        experienceLevel = experienceLevel,
        salaryMinEtb = 70_000,
        salaryMaxEtb = 90_000,
        description = "Build the Telebirr integration.",
        responsibilities = listOf("Ship code", "Review PRs"),
        requirements = listOf("5+ years Android"),
        benefits = listOf("Health insurance"),
        isUrgent = true,
        isVerifiedCompany = true,
        contactEmail = "careers@ethio.et",
        contactPhone = null,
        expiresAt = expiresAt,
        applicantsCount = 3,
        createdAt = createdAt
    )

    @Test
    fun `snake_case enum values map to UI enums`() {
        val entity = JobMapper.dtoToEntity(dto())
        val job = JobMapper.entityToDomain(entity, currentUserId = null)

        assertEquals(EmploymentType.FULL_TIME, job.employmentType)
        assertEquals(WorkplaceType.HYBRID, job.workplaceType)
        assertEquals(ExperienceLevel.SENIOR, job.experienceLevel)
    }

    @Test
    fun `unknown enum value falls back instead of throwing`() {
        val entity = JobMapper.dtoToEntity(
            dto(employmentType = "gig_work", workplaceType = "moon", experienceLevel = "wizard")
        )
        val job = JobMapper.entityToDomain(entity, currentUserId = null)

        assertEquals(EmploymentType.FULL_TIME, job.employmentType)
        assertEquals(WorkplaceType.ON_SITE, job.workplaceType)
        assertEquals(ExperienceLevel.MID, job.experienceLevel)
    }

    @Test
    fun `text array columns survive the round trip through Room`() {
        val entity = JobMapper.dtoToEntity(dto())
        val job = JobMapper.entityToDomain(entity, currentUserId = null)

        assertEquals(listOf("Ship code", "Review PRs"), job.responsibilities)
        assertEquals(listOf("5+ years Android"), job.requirements)
        assertEquals(listOf("Health insurance"), job.benefits)
    }

    @Test
    fun `isPostedByUser is true only for the signed-in user`() {
        val entity = JobMapper.dtoToEntity(dto(postedBy = "user-1"))

        assertTrue(JobMapper.entityToDomain(entity, currentUserId = "user-1").isPostedByUser)
        assertFalse(JobMapper.entityToDomain(entity, currentUserId = "user-2").isPostedByUser)
        assertFalse(JobMapper.entityToDomain(entity, currentUserId = null).isPostedByUser)
    }

    @Test
    fun `missing expiry reports open until filled`() {
        val entity = JobMapper.dtoToEntity(dto(expiresAt = null))
        assertEquals("Open until filled", JobMapper.entityToDomain(entity, null).deadline)
    }

    @Test
    fun `present expiry is formatted as a date`() {
        val entity = JobMapper.dtoToEntity(dto(expiresAt = "2024-06-15T00:00:00+00:00"))
        assertEquals("Apply by Jun 15, 2024", JobMapper.entityToDomain(entity, null).deadline)
    }

    @Test
    fun `relative time handles recent and old timestamps`() {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }

        val tenMinutesAgo = now.format(Date(System.currentTimeMillis() - 10 * 60_000L))
        assertEquals("10m ago", JobMapper.relativeTime(tenMinutesAgo))

        val threeHoursAgo = now.format(Date(System.currentTimeMillis() - 3 * 3_600_000L))
        assertEquals("3h ago", JobMapper.relativeTime(threeHoursAgo))
    }

    @Test
    fun `blank created_at degrades to Recently rather than crashing`() {
        assertEquals("Recently", JobMapper.relativeTime(""))
        assertEquals("Recently", JobMapper.relativeTime("not-a-date"))
    }

    @Test
    fun `iso8601 parser handles fractional seconds and offsets`() {
        assertEquals(
            JobMapper.parseIso8601("2024-05-01T10:00:00.123456+00:00"),
            JobMapper.parseIso8601("2024-05-01T10:00:00Z")
        )
        assertNull(JobMapper.parseIso8601("garbage"))
    }
}
