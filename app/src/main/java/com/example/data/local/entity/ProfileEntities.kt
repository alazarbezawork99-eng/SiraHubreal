package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room mirror of `public.profiles`. */
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "account_type") val accountType: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    val phone: String?,
    val city: String?,
    @ColumnInfo(name = "avatar_path") val avatarPath: String?,
    @ColumnInfo(name = "created_at") val createdAt: String
)

/** Room mirror of `public.seeker_cvs`. One row per job seeker. */
@Entity(tableName = "seeker_cvs")
data class SeekerCvEntity(
    @PrimaryKey @ColumnInfo(name = "seeker_id") val seekerId: String,
    val headline: String?,
    val bio: String?,
    /** JSON-encoded `text[]`. */
    val skills: String,
    /** JSON-encoded `text[]`. */
    val languages: String,
    @ColumnInfo(name = "expected_salary_min_etb") val expectedSalaryMinEtb: Long?,
    val availability: String?
)

/** Room mirror of `public.education`. */
@Entity(tableName = "education")
data class EducationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cv_id") val cvId: String,
    val institution: String,
    val degree: String,
    @ColumnInfo(name = "field_of_study") val fieldOfStudy: String?,
    @ColumnInfo(name = "start_year") val startYear: String?,
    @ColumnInfo(name = "end_year") val endYear: String?
)

/** Room mirror of `public.experience`. */
@Entity(tableName = "experience")
data class ExperienceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cv_id") val cvId: String,
    @ColumnInfo(name = "role_title") val roleTitle: String,
    @ColumnInfo(name = "company_name") val companyName: String,
    val location: String?,
    @ColumnInfo(name = "start_date") val startDate: String?,
    @ColumnInfo(name = "end_date") val endDate: String?,
    val description: String?
)

/** Room mirror of `public.portfolio_links`. */
@Entity(tableName = "portfolio_links")
data class PortfolioLinkEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "cv_id") val cvId: String,
    val title: String,
    val url: String
)

/** Room mirror of `public.companies`. */
@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    val name: String,
    val industry: String?,
    val location: String?,
    val website: String?,
    @ColumnInfo(name = "contact_email") val contactEmail: String?,
    @ColumnInfo(name = "contact_phone") val contactPhone: String?,
    val description: String?,
    @ColumnInfo(name = "verification_status") val verificationStatus: String,
    @ColumnInfo(name = "employees_count") val employeesCount: String?,
    @ColumnInfo(name = "founded_year") val foundedYear: String?
)

/** Room mirror of `public.bookmarks`. */
@Entity(tableName = "bookmarks", primaryKeys = ["user_id", "target_type", "target_id"])
data class BookmarkEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "target_type") val targetType: String,
    @ColumnInfo(name = "target_id") val targetId: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)
