package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT target_id FROM bookmarks WHERE user_id = :userId AND target_type = :targetType")
    fun observeIds(userId: String, targetType: String): Flow<List<String>>

    @Query("SELECT * FROM bookmarks WHERE user_id = :userId")
    fun observeAll(userId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE user_id = :userId AND target_type = :targetType AND target_id = :targetId")
    suspend fun delete(userId: String, targetType: String, targetId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE user_id = :userId AND target_type = :targetType AND target_id = :targetId)")
    suspend fun exists(userId: String, targetType: String, targetId: String): Boolean

    @Upsert
    suspend fun upsertAll(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()
}
