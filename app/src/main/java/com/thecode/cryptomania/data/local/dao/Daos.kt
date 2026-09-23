package com.thecode.cryptomania.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.thecode.cryptomania.data.local.entity.CoinEntity
import com.thecode.cryptomania.data.local.entity.CoinProfileEntity
import com.thecode.cryptomania.data.local.entity.ExchangeEntity
import com.thecode.cryptomania.data.local.entity.GlobalMarketEntity
import com.thecode.cryptomania.data.local.entity.PriceHistoryEntity
import com.thecode.cryptomania.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    @Query("SELECT * FROM coins WHERE isListed = 1 ORDER BY marketCapRank IS NULL, marketCapRank")
    fun observeListed(): Flow<List<CoinEntity>>

    @Query("SELECT * FROM coins WHERE id = :id")
    fun observe(id: String): Flow<CoinEntity?>

    @Query("SELECT fetchedAtMillis FROM coins WHERE id = :id")
    suspend fun fetchedAt(id: String): Long?

    @Query("SELECT isListed FROM coins WHERE id = :id")
    suspend fun isListed(id: String): Boolean?

    @Query("SELECT MAX(fetchedAtMillis) FROM coins WHERE isListed = 1")
    suspend fun listedFetchedAt(): Long?

    @Query("SELECT * FROM coins WHERE id IN (:ids)")
    fun observeByIds(ids: Collection<String>): Flow<List<CoinEntity>>

    /** Watched coins outside the top list: refreshed together in one batched request. */
    @Query("SELECT coinId FROM watchlist WHERE coinId NOT IN (SELECT id FROM coins WHERE isListed = 1)")
    suspend fun unlistedWatchedIds(): List<String>

    @Upsert
    suspend fun upsert(coins: List<CoinEntity>)

    /** Replaces the top list atomically so observers never see a half-updated market. */
    @Transaction
    suspend fun replaceListed(coins: List<CoinEntity>, orphanCutoffMillis: Long) {
        unlistAll()
        upsert(coins)
        deleteOrphans(orphanCutoffMillis)
    }

    @Query("UPDATE coins SET isListed = 0")
    suspend fun unlistAll()

    /** Unlisted, unwatched coins (e.g. opened from search) are dropped once they are old. */
    @Query(
        "DELETE FROM coins WHERE isListed = 0 AND fetchedAtMillis < :cutoffMillis " +
            "AND id NOT IN (SELECT coinId FROM watchlist)",
    )
    suspend fun deleteOrphans(cutoffMillis: Long)

    @Query("DELETE FROM coins")
    suspend fun clear()
}

@Dao
interface GlobalMarketDao {
    @Query("SELECT * FROM global_market WHERE id = 0")
    fun observe(): Flow<GlobalMarketEntity?>

    @Query("SELECT fetchedAtMillis FROM global_market WHERE id = 0")
    suspend fun fetchedAt(): Long?

    @Upsert
    suspend fun upsert(entity: GlobalMarketEntity)

    @Query("DELETE FROM global_market")
    suspend fun clear()
}

@Dao
interface PriceHistoryDao {
    @Query("SELECT * FROM price_history WHERE coinId = :coinId AND range = :range")
    fun observe(coinId: String, range: String): Flow<PriceHistoryEntity?>

    @Query("SELECT fetchedAtMillis FROM price_history WHERE coinId = :coinId AND range = :range")
    suspend fun fetchedAt(coinId: String, range: String): Long?

    @Upsert
    suspend fun upsert(entity: PriceHistoryEntity)

    /** Keeps the cache bounded: charts older than [olderThanMillis] are dropped. */
    @Query("DELETE FROM price_history WHERE fetchedAtMillis < :olderThanMillis")
    suspend fun deleteOlderThan(olderThanMillis: Long)

    @Query("DELETE FROM price_history")
    suspend fun clear()
}

@Dao
interface CoinProfileDao {
    @Query("SELECT * FROM coin_profiles WHERE id = :id")
    fun observe(id: String): Flow<CoinProfileEntity?>

    @Query("SELECT fetchedAtMillis FROM coin_profiles WHERE id = :id")
    suspend fun fetchedAt(id: String): Long?

    @Upsert
    suspend fun upsert(entity: CoinProfileEntity)

    @Query("DELETE FROM coin_profiles")
    suspend fun clear()
}

@Dao
interface ExchangeDao {
    @Query("SELECT * FROM exchanges ORDER BY position")
    fun observeAll(): Flow<List<ExchangeEntity>>

    @Query("SELECT MAX(fetchedAtMillis) FROM exchanges")
    suspend fun fetchedAt(): Long?

    @Transaction
    suspend fun replaceAll(exchanges: List<ExchangeEntity>) {
        clear()
        insertAll(exchanges)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exchanges: List<ExchangeEntity>)

    @Query("DELETE FROM exchanges")
    suspend fun clear()
}

@Dao
interface WatchlistDao {
    @Query("SELECT coinId FROM watchlist ORDER BY addedAtMillis")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE coinId = :coinId)")
    suspend fun contains(coinId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE coinId = :coinId")
    suspend fun delete(coinId: String)
}
