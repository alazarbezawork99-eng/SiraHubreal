package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room mirror of `public.applications`.
 *
 * The schema deliberately does not denormalise the applicant's name, skills or the job title onto
 * this row: `profiles` is only readable by its owner, so the applicant's CV is captured at submit
 * time in [cvSnapshot] (JSON) and the job title is joined from `jobs`.
 */
@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "job_id") val jobId: String,
    @ColumnInfo(name = "applicant_id") val applicantId: String,
    @ColumnInfo(name = "cover_note") val coverNote: String?,
    @ColumnInfo(name = "expected_salary_etb") val expectedSalaryEtb: Long?,
    val status: String,
    /** JSON object holding the applicant's CV as it was at submit time. */
    @ColumnInfo(name = "cv_snapshot") val cvSnapshot: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)

/** Room mirror of `public.bids`. */
@Entity(tableName = "bids")
data class BidEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "gig_id") val gigId: String,
    @ColumnInfo(name = "bidder_id") val bidderId: String,
    @ColumnInfo(name = "amount_etb") val amountEtb: Long,
    /** Whole days, matching `bids.delivery_days integer`. */
    @ColumnInfo(name = "delivery_days") val deliveryDays: Int,
    val pitch: String?,
    val status: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)
