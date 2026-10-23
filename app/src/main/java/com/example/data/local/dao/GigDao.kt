package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.GigContactEntity
import com.example.data.local.entity.GigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GigDao {

    @Query("SELECT * FROM gigs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE id = :id")
    fun observeById(id: String): Flow<GigEntity?>

    @Query("SELECT * FROM gigs WHERE owner_id = :userId ORDER BY created_at DESC")
    fun observeByOwner(userId: String): Flow<List<GigEntity>>

    @Query("SELECT * FROM gig_contacts")
    fun observeAllContacts(): Flow<List<GigContactEntity>>

    @Upsert
    suspend fun upsertAll(gigs: List<GigEntity>)

    @Upsert
    suspend fun upsertContacts(contacts: List<GigContactEntity>)

    @Query("DELETE FROM gigs")
    suspend fun deleteAll()

    @Query("DELETE FROM gig_contacts")
    suspend fun deleteAllContacts()

    @Query("DELETE FROM gigs WHERE id = :id")
    suspend fun deleteById(id: String)
}
