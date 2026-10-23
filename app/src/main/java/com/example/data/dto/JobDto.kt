package com.example.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire representation of a row in `public.jobs`.
 *
 * Field names and nullability mirror `supabase/migrations/0001_schema.sql` exactly. This type is
 * the only thing that talks to PostgREST; nothing outside `data/` should see it.
 *
 * `text[]` columns arrive as JSON arrays, which kotlinx.serialization maps to `List<String>`
 * directly. `timestamptz` columns arrive as ISO-8601 strings.
 */
@Serializable
data class JobDto(
    val id: String,
    @SerialName("company_id") val companyId: String? = null,
    @SerialName("posted_by") val postedBy: String,
    val title: String,
    val company: String,
    val location: String,
    val category: String,
    @SerialName("employment_type") val employmentType: String = "full_time",
    @SerialName("workplace_type") val workplaceType: String = "hybrid",
    @SerialName("experience_level") val experienceLevel: String = "mid",
    @SerialName("salary_min_etb") val salaryMinEtb: Long = 0,
    @SerialName("salary_max_etb") val salaryMaxEtb: Long = 0,
    @SerialName("salary_period") val salaryPeriod: String = "month",
    val description: String,
    val responsibilities: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("is_urgent") val isUrgent: Boolean = false,
    @SerialName("is_verified_company") val isVerifiedCompany: Boolean = true,
    @SerialName("contact_email") val contactEmail: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val status: String = "open",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("applicants_count") val applicantsCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

/** Body sent when creating a job. Only columns the client is allowed to set. */
@Serializable
data class NewJobDto(
    @SerialName("posted_by") val postedBy: String,
    @SerialName("company_id") val companyId: String? = null,
    val title: String,
    val company: String,
    val location: String,
    val category: String,
    @SerialName("employment_type") val employmentType: String,
    @SerialName("workplace_type") val workplaceType: String,
    @SerialName("experience_level") val experienceLevel: String,
    @SerialName("salary_min_etb") val salaryMinEtb: Long,
    @SerialName("salary_max_etb") val salaryMaxEtb: Long,
    @SerialName("salary_period") val salaryPeriod: String,
    val description: String,
    val responsibilities: List<String>,
    val requirements: List<String>,
    val benefits: List<String>,
    @SerialName("is_urgent") val isUrgent: Boolean = false,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("contact_email") val contactEmail: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null
)
