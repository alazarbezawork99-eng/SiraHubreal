package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.data.local.dao.ApplicationDao
import com.example.data.local.dao.BidDao
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.CompanyDao
import com.example.data.local.dao.GigDao
import com.example.data.local.dao.JobDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.entity.ApplicationEntity
import com.example.data.local.entity.BidEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.EducationEntity
import com.example.data.local.entity.ExperienceEntity
import com.example.data.local.entity.GigContactEntity
import com.example.data.local.entity.GigEntity
import com.example.data.local.entity.JobEntity
import com.example.data.local.entity.PortfolioLinkEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SeekerCvEntity

/**
 * The offline read cache. Supabase is the source of truth; this database exists so screens can
 * render instantly and keep working without connectivity.
 *
 * Every write that mirrors a server response goes through a single transaction (see
 * [replaceJobs] and friends) so the cache never ends up half-updated.
 */
@Database(
    entities = [
        JobEntity::class,
        GigEntity::class,
        GigContactEntity::class,
        ApplicationEntity::class,
        BidEntity::class,
        ProfileEntity::class,
        SeekerCvEntity::class,
        EducationEntity::class,
        ExperienceEntity::class,
        PortfolioLinkEntity::class,
        CompanyEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SiraHubDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao
    abstract fun gigDao(): GigDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun bidDao(): BidDao
    abstract fun profileDao(): ProfileDao
    abstract fun companyDao(): CompanyDao
    abstract fun bookmarkDao(): BookmarkDao

    /**
     * Atomically replaces the cached job list with a fresh server snapshot.
     *
     * Deleting before inserting is what makes deletes propagate: a job removed on the server is
     * gone from the cache after a refresh, instead of lingering forever as it did with the old
     * "only replace when the cloud list is non-empty" merge.
     */
    suspend fun replaceJobs(jobs: List<JobEntity>) = withTransaction {
        jobDao().deleteAll()
        jobDao().upsertAll(jobs)
    }

    suspend fun replaceGigs(gigs: List<GigEntity>, contacts: List<GigContactEntity>) = withTransaction {
        gigDao().deleteAll()
        gigDao().deleteAllContacts()
        gigDao().upsertAll(gigs)
        gigDao().upsertContacts(contacts)
    }

    suspend fun replaceApplications(applications: List<ApplicationEntity>) = withTransaction {
        applicationDao().deleteAll()
        applicationDao().upsertAll(applications)
    }

    suspend fun replaceBids(bids: List<BidEntity>) = withTransaction {
        bidDao().deleteAll()
        bidDao().upsertAll(bids)
    }

    suspend fun replaceBookmarks(bookmarks: List<BookmarkEntity>) = withTransaction {
        bookmarkDao().deleteAll()
        bookmarkDao().upsertAll(bookmarks)
    }
}
