package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    /** Screens observe this. Emits again whenever the cache changes. */
    @Query("SELECT * FROM jobs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun observeById(id: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE posted_by = :userId ORDER BY created_at DESC")
    fun observeByPoster(userId: String): Flow<List<JobEntity>>

    @Upsert
    suspend fun upsertAll(jobs: List<JobEntity>)

    /**
     * Replaces the whole cached job list in one transaction.
     *
     * Used by a refresh: rows the server no longer returns are deleted, so deletes propagate
     * instead of the old "cloud list only wins when non-empty" merge.
     */
    @Query("DELETE FROM jobs")
    suspend fun deleteAll()

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun count(): Int
}
