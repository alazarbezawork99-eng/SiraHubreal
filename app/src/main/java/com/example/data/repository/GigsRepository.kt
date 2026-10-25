package com.example.data.repository

import com.example.data.core.DataResult
import com.example.model.GigPost
import kotlinx.coroutines.flow.Flow

/**
 * The only way the UI reads or writes gigs.
 *
 * Reads are served from the Room cache as a [Flow]; [refresh] pulls from Supabase and replaces the
 * cache in one transaction.
 *
 * Client contact details are deliberately not guaranteed: `public.gig_contacts` is only readable by
 * the gig owner or the winning bidder, so `clientPhone` / `clientTelegram` are null for everyone
 * else. That is a privacy rule enforced by the database, not a bug.
 */
interface GigsRepository {

    /** Cached gigs, newest first. */
    fun observeGigs(): Flow<List<GigPost>>

    /** A single cached gig, or null if it is not in the cache. */
    fun observeGig(gigId: String): Flow<GigPost?>

    /** Gigs posted by the given user, newest first. */
    fun observeMyGigs(userId: String): Flow<List<GigPost>>

    /**
     * Fetches the current gig list from Supabase and replaces the cached copy atomically.
     * Returns the number of gigs stored, or a typed failure. On failure the cache is untouched.
     */
    suspend fun refresh(): DataResult<Int>

    /**
     * Publishes a new gig, then its contact row, then refreshes the cache.
     *
     * The contact row is written separately because `gig_contacts.gig_id` references the gig that
     * must exist first. If the contact insert fails the gig is still published; the failure is
     * reported so the user knows their contact details were not saved.
     */
    suspend fun createGig(gig: GigPost): DataResult<GigPost>

    /** Deletes a gig the signed-in user owns. RLS rejects gigs owned by anyone else. */
    suspend fun deleteGig(gigId: String): DataResult<Unit>
}
