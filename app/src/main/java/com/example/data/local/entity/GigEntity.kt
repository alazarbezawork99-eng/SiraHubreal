package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room mirror of `public.gigs`.
 *
 * Client contact details live in the separate `gig_contacts` table in the schema, so they are
 * stored in [GigContactEntity] and joined in the mapper rather than duplicated here.
 */
@Entity(tableName = "gigs")
data class GigEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    val title: String,
    @ColumnInfo(name = "client_name") val clientName: String,
    @ColumnInfo(name = "client_type") val clientType: String?,
    val location: String,
    val category: String,
    @ColumnInfo(name = "budget_etb") val budgetEtb: Long,
    @ColumnInfo(name = "pricing_type") val pricingType: String,
    @ColumnInfo(name = "duration_text") val durationText: String,
    val description: String,
    /** JSON-encoded `text[]`. */
    val deliverables: String,
    /** JSON-encoded `text[]`. */
    @ColumnInfo(name = "required_skills") val requiredSkills: String,
    @ColumnInfo(name = "is_urgent") val isUrgent: Boolean,
    val status: String,
    @ColumnInfo(name = "bids_count") val bidsCount: Int,
    @ColumnInfo(name = "created_at") val createdAt: String
)

/** Room mirror of `public.gig_contacts`. One row per gig. */
@Entity(tableName = "gig_contacts")
data class GigContactEntity(
    @PrimaryKey @ColumnInfo(name = "gig_id") val gigId: String,
    @ColumnInfo(name = "client_phone") val clientPhone: String?,
    @ColumnInfo(name = "client_telegram") val clientTelegram: String?
)
