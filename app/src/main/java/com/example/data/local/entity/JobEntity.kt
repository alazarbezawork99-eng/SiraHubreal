package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room mirror of `public.jobs` (see supabase/migrations/0001_schema.sql).
 *
 * Column names and nullability follow the SQL schema, not the UI model, so that a row written by
 * the server can be stored without lossy translation. Arrays are stored as JSON text because Room
 * has no native array type; the mapper is the only place that encodes/decodes them.
 */
@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "company_id") val companyId: String?,
    @ColumnInfo(name = "posted_by") val postedBy: String,
    val title: String,
    val company: String,
    val location: String,
    val category: String,
    @ColumnInfo(name = "employment_type") val employmentType: String,
    @ColumnInfo(name = "workplace_type") val workplaceType: String,
    @ColumnInfo(name = "experience_level") val experienceLevel: String,
    @ColumnInfo(name = "salary_min_etb") val salaryMinEtb: Long,
    @ColumnInfo(name = "salary_max_etb") val salaryMaxEtb: Long,
    @ColumnInfo(name = "salary_period") val salaryPeriod: String,
    val description: String,
    /** JSON-encoded `text[]`. */
    val responsibilities: String,
    /** JSON-encoded `text[]`. */
    val requirements: String,
    /** JSON-encoded `text[]`. */
    val benefits: String,
    @ColumnInfo(name = "is_featured") val isFeatured: Boolean,
    @ColumnInfo(name = "is_urgent") val isUrgent: Boolean,
    @ColumnInfo(name = "is_verified_company") val isVerifiedCompany: Boolean,
    @ColumnInfo(name = "contact_email") val contactEmail: String?,
    @ColumnInfo(name = "contact_phone") val contactPhone: String?,
    val status: String,
    /** ISO-8601 timestamp, or null when the posting has no expiry. */
    @ColumnInfo(name = "expires_at") val expiresAt: String?,
    @ColumnInfo(name = "applicants_count") val applicantsCount: Int,
    @ColumnInfo(name = "created_at") val createdAt: String
)
