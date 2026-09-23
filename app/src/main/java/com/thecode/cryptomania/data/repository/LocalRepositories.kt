package com.thecode.cryptomania.data.repository

import androidx.room.withTransaction
import com.thecode.cryptomania.data.local.CryptoManiaDatabase
import com.thecode.cryptomania.data.local.dao.WatchlistDao
import com.thecode.cryptomania.data.local.entity.WatchlistEntity
import com.thecode.cryptomania.domain.repository.CacheRepository
import com.thecode.cryptomania.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomWatchlistRepository @Inject constructor(
    private val database: CryptoManiaDatabase,
    private val watchlistDao: WatchlistDao,
    private val clock: Clock,
) : WatchlistRepository {

    override fun observeWatchlist(): Flow<Set<String>> =
        watchlistDao.observeIds().map { it.toSet() }.distinctUntilChanged()

    override suspend fun toggle(coinId: String) {
        database.withTransaction {
            if (watchlistDao.contains(coinId)) {
                watchlistDao.delete(coinId)
            } else {
                watchlistDao.insert(WatchlistEntity(coinId, clock.millis()))
            }
        }
    }
}

@Singleton
class RoomCacheRepository @Inject constructor(
    private val database: CryptoManiaDatabase,
) : CacheRepository {

    override suspend fun clearMarketCache() {
        database.withTransaction {
            database.coinDao().clear()
            database.globalMarketDao().clear()
            database.priceHistoryDao().clear()
            database.coinProfileDao().clear()
            database.exchangeDao().clear()
            database.trendingDao().clear()
        }
    }
}
