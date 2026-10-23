package com.example.data.repository

import com.example.data.core.DataResult
import com.example.model.JobPost
import kotlinx.coroutines.flow.Flow

/**
 * The only way the UI reads or writes jobs.
 *
 * Reads are served from the Room cache as a [Flow], so screens keep rendering instantly and still
 * work offline. [refresh] is the single entry point that pulls from Supabase and writes the result
 * to Room in one transaction.
 *
 * No method returns an empty list to signal failure: an empty [Flow] emission means "the server
 * said there are no jobs", while a [DataResult] failure means "we could not ask".
 */
interface JobsRepository {

    /** Cached jobs, newest first. Emits again whenever the cache changes. */
    fun observeJobs(): Flow<List<JobPost>>

    /** A single cached job, or null if it is not in the cache. */
    fun observeJob(jobId: String): Flow<JobPost?>

    /** Jobs posted by the given user, newest first. */
    fun observeMyJobs(userId: String): Flow<List<JobPost>>

    /**
     * Fetches the current job list from Supabase and replaces the cached copy atomically.
     *
     * Returns [DataResult.Success] with the number of jobs stored, or a typed failure. On failure
     * the cache is left untouched, so the last good data stays on screen.
     */
    suspend fun refresh(): DataResult<Int>

    /**
     * Publishes a new job to Supabase, then refreshes the cache.
     *
     * The job is NOT written to the cache optimistically: `jobs.posted_by` and the generated `id`
     * come from the server, so a local insert would create a row that can never be reconciled.
     */
    suspend fun createJob(job: JobPost): DataResult<JobPost>

    /** Deletes a job the signed-in user owns. RLS rejects jobs posted by anyone else. */
    suspend fun deleteJob(jobId: String): DataResult<Unit>
}
