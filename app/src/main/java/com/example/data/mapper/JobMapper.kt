package com.example.data.mapper

import com.example.data.dto.JobDto
import com.example.data.dto.NewJobDto
import com.example.data.local.entity.JobEntity
import com.example.model.EmploymentType
import com.example.model.ExperienceLevel
import com.example.model.JobPost
import com.example.model.WorkplaceType
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Translates between the three representations of a job:
 *
 *   JobDto (wire)  <->  JobEntity (Room cache)  <->  JobPost (UI model)
 *
 * The schema is the source of truth. Where the UI model wants something the schema does not store,
 * the value is derived here in one place rather than faked at the call site:
 *
 *  - `JobPost.deadline` is a human string; the schema stores `expires_at` (timestamptz). We format
 *    it, and show "Open until filled" only when the server genuinely has no expiry.
 *  - `JobPost.postedTimeAgo` is derived from `created_at`.
 *  - `JobPost.isPostedByUser` is derived by comparing `posted_by` with the signed-in user id.
 *
 * Enum columns are stored as snake_case strings (`full_time`); the UI enums are SCREAMING_SNAKE
 * (`FULL_TIME`). Unknown server values fall back to a safe default instead of throwing, so one bad
 * row cannot take down the whole feed.
 */
object JobMapper {

    private val json = Json { ignoreUnknownKeys = true }

    // ---------------------------------------------------------------------------------------
    // Wire -> Room
    // ---------------------------------------------------------------------------------------

    fun dtoToEntity(dto: JobDto): JobEntity = JobEntity(
        id = dto.id,
        companyId = dto.companyId,
        postedBy = dto.postedBy,
        title = dto.title,
        company = dto.company,
        location = dto.location,
        category = dto.category,
        employmentType = dto.employmentType,
        workplaceType = dto.workplaceType,
        experienceLevel = dto.experienceLevel,
        salaryMinEtb = dto.salaryMinEtb,
        salaryMaxEtb = dto.salaryMaxEtb,
        salaryPeriod = dto.salaryPeriod,
        description = dto.description,
        responsibilities = json.encodeToString(dto.responsibilities),
        requirements = json.encodeToString(dto.requirements),
        benefits = json.encodeToString(dto.benefits),
        isFeatured = dto.isFeatured,
        isUrgent = dto.isUrgent,
        isVerifiedCompany = dto.isVerifiedCompany,
        contactEmail = dto.contactEmail,
        contactPhone = dto.contactPhone,
        status = dto.status,
        expiresAt = dto.expiresAt,
        applicantsCount = dto.applicantsCount,
        createdAt = dto.createdAt.orEmpty()
    )

    // ---------------------------------------------------------------------------------------
    // Room -> UI model
    // ---------------------------------------------------------------------------------------

    /**
     * @param currentUserId the signed-in user's id, used to decide [JobPost.isPostedByUser].
     *   Null when browsing as a guest, in which case nothing is marked as the user's own post.
     */
    fun entityToDomain(entity: JobEntity, currentUserId: String?): JobPost = JobPost(
        id = entity.id,
        title = entity.title,
        company = entity.company,
        location = entity.location,
        category = entity.category,
        employmentType = employmentTypeFromDb(entity.employmentType),
        workplaceType = workplaceTypeFromDb(entity.workplaceType),
        experienceLevel = experienceLevelFromDb(entity.experienceLevel),
        salaryMinETB = entity.salaryMinEtb,
        salaryMaxETB = entity.salaryMaxEtb,
        salaryPeriod = entity.salaryPeriod,
        description = entity.description,
        responsibilities = decodeStringList(entity.responsibilities),
        requirements = decodeStringList(entity.requirements),
        benefits = decodeStringList(entity.benefits),
        postedTimeAgo = relativeTime(entity.createdAt),
        deadline = formatDeadline(entity.expiresAt),
        applicantsCount = entity.applicantsCount,
        isFeatured = entity.isFeatured,
        isUrgent = entity.isUrgent,
        isVerifiedCompany = entity.isVerifiedCompany,
        contactEmail = entity.contactEmail.orEmpty(),
        contactPhone = entity.contactPhone,
        isPostedByUser = currentUserId != null && entity.postedBy == currentUserId
    )

    // ---------------------------------------------------------------------------------------
    // UI model -> wire (create)
    // ---------------------------------------------------------------------------------------

    fun domainToNewDto(job: JobPost, postedBy: String, companyId: String?): NewJobDto = NewJobDto(
        postedBy = postedBy,
        companyId = companyId,
        title = job.title.trim(),
        company = job.company.trim(),
        location = job.location.trim(),
        category = job.category.trim(),
        employmentType = employmentTypeToDb(job.employmentType),
        workplaceType = workplaceTypeToDb(job.workplaceType),
        experienceLevel = experienceLevelToDb(job.experienceLevel),
        salaryMinEtb = job.salaryMinETB,
        salaryMaxEtb = job.salaryMaxETB,
        salaryPeriod = job.salaryPeriod,
        description = job.description.trim(),
        responsibilities = job.responsibilities,
        requirements = job.requirements,
        benefits = job.benefits,
        isUrgent = job.isUrgent,
        isFeatured = job.isFeatured,
        contactEmail = job.contactEmail.takeIf { it.isNotBlank() },
        contactPhone = job.contactPhone?.takeIf { it.isNotBlank() }
    )

    // ---------------------------------------------------------------------------------------
    // Enum conversion
    // ---------------------------------------------------------------------------------------

    fun employmentTypeFromDb(value: String): EmploymentType =
        EmploymentType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: EmploymentType.FULL_TIME

    fun workplaceTypeFromDb(value: String): WorkplaceType =
        WorkplaceType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: WorkplaceType.ON_SITE

    fun experienceLevelFromDb(value: String): ExperienceLevel =
        ExperienceLevel.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: ExperienceLevel.MID

    fun employmentTypeToDb(value: EmploymentType): String = value.name.lowercase(Locale.US)

    fun workplaceTypeToDb(value: WorkplaceType): String = value.name.lowercase(Locale.US)

    fun experienceLevelToDb(value: ExperienceLevel): String = value.name.lowercase(Locale.US)

    // ---------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------

    private fun decodeStringList(encoded: String): List<String> =
        if (encoded.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<String>>(encoded) }.getOrDefault(emptyList())

    /**
     * Formats an ISO-8601 `timestamptz` into the short label the job card shows.
     * Returns "Open until filled" when the server has no expiry, which is the honest reading.
     */
    internal fun formatDeadline(expiresAt: String?): String {
        if (expiresAt.isNullOrBlank()) return "Open until filled"
        val date = parseIso8601(expiresAt) ?: return "Open until filled"
        return "Apply by " + SimpleDateFormat("MMM d, yyyy", Locale.US).format(date)
    }

    /** Turns a `created_at` timestamp into "Just now", "3h ago", "2d ago", or a date. */
    internal fun relativeTime(createdAt: String): String {
        if (createdAt.isBlank()) return "Recently"
        val created = parseIso8601(createdAt) ?: return "Recently"
        val diffMs = System.currentTimeMillis() - created.time
        if (diffMs < 0) return "Just now"

        val minutes = diffMs / 60_000
        val hours = diffMs / 3_600_000
        val days = diffMs / 86_400_000

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days}d ago"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(created)
        }
    }

    /**
     * Parses the ISO-8601 timestamps PostgREST returns. Supabase emits fractional seconds and a
     * timezone offset; `SimpleDateFormat` cannot handle the `T` separator or 6-digit fractions, so
     * we normalise before parsing rather than pulling in a date library.
     */
    internal fun parseIso8601(value: String): Date? {
        val normalised = value
            .replace('T', ' ')
            .substringBefore('.')
            .substringBefore('+')
            .trim()
            .removeSuffix("Z")

        val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(normalised)
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }
}
