package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entity.BidEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BidDao {

    @Query("SELECT * FROM bids ORDER BY created_at DESC")
    fun observeAll(): Flow<List<BidEntity>>

    @Query("SELECT * FROM bids WHERE bidder_id = :userId ORDER BY created_at DESC")
    fun observeByBidder(userId: String): Flow<List<BidEntity>>

    /** Bids on gigs the given user owns - the gig provider's review list. */
    @Query(
        """
        SELECT b.* FROM bids b
        INNER JOIN gigs g ON g.id = b.gig_id
        WHERE g.owner_id = :userId
        ORDER BY b.created_at DESC
        """
    )
    fun observeByGigOwner(userId: String): Flow<List<BidEntity>>

    @Upsert
    suspend fun upsertAll(bids: List<BidEntity>)

    @Upsert
    suspend fun upsert(bid: BidEntity)

    @Query("UPDATE bids SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM bids")
    suspend fun deleteAll()

    @Query("DELETE FROM bids WHERE id = :id")
    suspend fun deleteById(id: String)
}
