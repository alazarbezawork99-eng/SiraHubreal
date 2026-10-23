package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Query("SELECT * FROM companies WHERE owner_id = :ownerId LIMIT 1")
    fun observeByOwner(ownerId: String): Flow<CompanyEntity?>

    @Query("SELECT * FROM companies WHERE id = :id")
    fun observeById(id: String): Flow<CompanyEntity?>

    @Upsert
    suspend fun upsert(company: CompanyEntity)

    @Query("DELETE FROM companies")
    suspend fun deleteAll()
}
