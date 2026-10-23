package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.EducationEntity
import com.example.data.local.entity.ExperienceEntity
import com.example.data.local.entity.PortfolioLinkEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SeekerCvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles WHERE id = :userId")
    fun observeProfile(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM seeker_cvs WHERE seeker_id = :userId")
    fun observeCv(userId: String): Flow<SeekerCvEntity?>

    @Query("SELECT * FROM education WHERE cv_id = :cvId ORDER BY end_year DESC")
    fun observeEducation(cvId: String): Flow<List<EducationEntity>>

    @Query("SELECT * FROM experience WHERE cv_id = :cvId ORDER BY start_date DESC")
    fun observeExperience(cvId: String): Flow<List<ExperienceEntity>>

    @Query("SELECT * FROM portfolio_links WHERE cv_id = :cvId")
    fun observePortfolioLinks(cvId: String): Flow<List<PortfolioLinkEntity>>

    @Upsert
    suspend fun upsertProfile(profile: ProfileEntity)

    @Upsert
    suspend fun upsertCv(cv: SeekerCvEntity)

    @Upsert
    suspend fun upsertEducation(items: List<EducationEntity>)

    @Upsert
    suspend fun upsertExperience(items: List<ExperienceEntity>)

    @Upsert
    suspend fun upsertPortfolioLinks(items: List<PortfolioLinkEntity>)

    @Query("DELETE FROM education WHERE cv_id = :cvId")
    suspend fun deleteEducationFor(cvId: String)

    @Query("DELETE FROM experience WHERE cv_id = :cvId")
    suspend fun deleteExperienceFor(cvId: String)

    @Query("DELETE FROM portfolio_links WHERE cv_id = :cvId")
    suspend fun deletePortfolioLinksFor(cvId: String)

    @Query("DELETE FROM profiles")
    suspend fun deleteAllProfiles()
}
