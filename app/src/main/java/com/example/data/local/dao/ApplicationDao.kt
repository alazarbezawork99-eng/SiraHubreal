package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.ApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM applications ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE applicant_id = :userId ORDER BY created_at DESC")
    fun observeByApplicant(userId: String): Flow<List<ApplicationEntity>>

    /** Applications for jobs the given user posted - the company dashboard's list. */
    @Query(
        """
        SELECT a.* FROM applications a
        INNER JOIN jobs j ON j.id = a.job_id
        WHERE j.posted_by = :userId
        ORDER BY a.created_at DESC
        """
    )
    fun observeByJobPoster(userId: String): Flow<List<ApplicationEntity>>

    @Upsert
    suspend fun upsertAll(applications: List<ApplicationEntity>)

    @Upsert
    suspend fun upsert(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM applications")
    suspend fun deleteAll()

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteById(id: String)
}
