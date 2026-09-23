package com.thecode.cryptomania.data.repository

import com.thecode.cryptomania.data.local.dao.CoinProfileDao
import com.thecode.cryptomania.data.local.dao.PriceHistoryDao
import com.thecode.cryptomania.data.mapper.toDomain
import com.thecode.cryptomania.data.mapper.toEntity
import com.thecode.cryptomania.data.mapper.toPricePoints
import com.thecode.cryptomania.data.network.RequestCoordinator
import com.thecode.cryptomania.data.remote.CoinGeckoApi
import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.CoinProfile
import com.thecode.cryptomania.domain.model.Currency
import com.thecode.cryptomania.domain.model.Done
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.model.PriceHistory
import com.thecode.cryptomania.domain.repository.CoinDetailsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstCoinDetailsRepository @Inject constructor(
    private val api: CoinGeckoApi,
    private val priceHistoryDao: PriceHistoryDao,
    private val profileDao: CoinProfileDao,
    private val coordinator: RequestCoordinator,
    private val clock: Clock,
) : CoinDetailsRepository {

    override fun observePriceHistory(coinId: String, range: ChartRange): Flow<Cached<PriceHistory>?> =
        priceHistoryDao.observe(coinId, range.name)
            .distinctUntilChanged()
            .map { entity ->
                val history = entity?.toDomain() ?: return@map null
                Cached(history, Instant.ofEpochMilli(entity.fetchedAtMillis))
            }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshPriceHistory(coinId: String, range: ChartRange, force: Boolean): Outcome<Unit> {
        val fetchedAt = priceHistoryDao.fetchedAt(coinId, range.name)
        if (!CachePolicy.shouldRefresh(fetchedAt, range.cacheTtl, force, clock)) return Done
        return coordinator.execute("chart:$coinId:${range.name}") {
            val points = api.marketChart(coinId, Currency.USD, range.days).toPricePoints()
            val now = clock.millis()
            priceHistoryDao.upsert(points.toEntity(coinId, range, now))
            priceHistoryDao.deleteOlderThan(now - CachePolicy.EVICTION_AGE.toMillis())
        }
    }

    override fun observeProfile(coinId: String): Flow<CoinProfile?> =
        profileDao.observe(coinId).map { it?.toDomain() }.distinctUntilChanged()

    override suspend fun refreshProfile(coinId: String): Outcome<Unit> {
        if (!CachePolicy.shouldRefresh(profileDao.fetchedAt(coinId), CachePolicy.PROFILE_TTL, false, clock)) return Done
        return coordinator.execute("profile:$coinId") {
            profileDao.upsert(api.coinProfile(coinId).toEntity(clock.millis()))
        }
    }
}
