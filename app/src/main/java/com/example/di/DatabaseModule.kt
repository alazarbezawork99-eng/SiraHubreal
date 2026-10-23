package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.SiraHubDatabase
import com.example.data.local.dao.ApplicationDao
import com.example.data.local.dao.BidDao
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.CompanyDao
import com.example.data.local.dao.GigDao
import com.example.data.local.dao.JobDao
import com.example.data.local.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SiraHubDatabase =
        Room.databaseBuilder(context, SiraHubDatabase::class.java, "sirahub.db")
            // The cache is disposable: if the schema changes we rebuild it from Supabase rather
            // than shipping hand-written migrations for data we can re-fetch.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideJobDao(db: SiraHubDatabase): JobDao = db.jobDao()

    @Provides
    fun provideGigDao(db: SiraHubDatabase): GigDao = db.gigDao()

    @Provides
    fun provideApplicationDao(db: SiraHubDatabase): ApplicationDao = db.applicationDao()

    @Provides
    fun provideBidDao(db: SiraHubDatabase): BidDao = db.bidDao()

    @Provides
    fun provideProfileDao(db: SiraHubDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideCompanyDao(db: SiraHubDatabase): CompanyDao = db.companyDao()

    @Provides
    fun provideBookmarkDao(db: SiraHubDatabase): BookmarkDao = db.bookmarkDao()
}
