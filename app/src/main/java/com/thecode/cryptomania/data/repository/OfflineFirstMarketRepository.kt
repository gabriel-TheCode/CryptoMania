package com.thecode.cryptomania.data.repository

import com.thecode.cryptomania.data.local.dao.CoinDao
import com.thecode.cryptomania.data.local.dao.GlobalMarketDao
import com.thecode.cryptomania.data.mapper.toDomain
import com.thecode.cryptomania.data.mapper.toEntity
import com.thecode.cryptomania.data.network.RequestCoordinator
import com.thecode.cryptomania.data.remote.CoinGeckoApi
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.CoinSearchHit
import com.thecode.cryptomania.domain.model.Currency
import com.thecode.cryptomania.domain.model.Done
import com.thecode.cryptomania.domain.model.GlobalMarket
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstMarketRepository @Inject constructor(
    private val api: CoinGeckoApi,
    private val coinDao: CoinDao,
    private val globalMarketDao: GlobalMarketDao,
    private val coordinator: RequestCoordinator,
    private val clock: Clock,
) : MarketRepository {

    /** Small in-memory cache so retyping a query does not hit the network again. */
    private val searchCache = object : LinkedHashMap<String, Cached<List<CoinSearchHit>>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Cached<List<CoinSearchHit>>>) =
            size > SEARCH_CACHE_SIZE
    }

    override fun observeTopCoins(): Flow<Cached<List<Coin>>?> = coinDao.observeListed()
        .map { entities ->
            if (entities.isEmpty()) {
                null
            } else {
                Cached(entities.map { it.toDomain() }, Instant.ofEpochMilli(entities.maxOf { it.fetchedAtMillis }))
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    override fun observeGlobalMarket(): Flow<GlobalMarket?> =
        globalMarketDao.observe().map { it?.toDomain() }.distinctUntilChanged()

    override fun observeCoin(id: String): Flow<Coin?> =
        coinDao.observe(id).map { it?.toDomain() }.distinctUntilChanged()

    override fun observeCoins(ids: Set<String>): Flow<List<Coin>> =
        if (ids.isEmpty()) flowOf(emptyList()) else coinDao.observeByIds(ids).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshMarket(force: Boolean): Outcome<Unit> = coroutineScope {
        val global = async { refreshGlobal(force) }
        val coins = refreshTopCoins(force)
        global.await()
        coins
    }

    private suspend fun refreshTopCoins(force: Boolean): Outcome<Unit> {
        if (!CachePolicy.shouldRefresh(coinDao.listedFetchedAt(), CachePolicy.MARKET_TTL, force, clock)) return Done
        val outcome = coordinator.execute(KEY_MARKETS) {
            val coins = api.markets(Currency.USD)
            val now = clock.millis()
            coinDao.replaceListed(
                coins = coins.map { it.toEntity(fetchedAtMillis = now, isListed = true) },
                orphanCutoffMillis = now - CachePolicy.EVICTION_AGE.toMillis(),
            )
        }
        if (outcome is Outcome.Success) refreshUnlistedWatchedCoins()
        return outcome
    }

    /** Watched coins outside the top list are refreshed in a single batched request. */
    private suspend fun refreshUnlistedWatchedCoins() {
        val ids = coinDao.unlistedWatchedIds().take(MAX_IDS_PER_REQUEST)
        if (ids.isEmpty()) return
        coordinator.execute(KEY_WATCHED) {
            val now = clock.millis()
            val coins = api.markets(Currency.USD, ids = ids.joinToString(","), perPage = ids.size)
            coinDao.upsert(coins.map { it.toEntity(fetchedAtMillis = now, isListed = false) })
        }
    }

    private suspend fun refreshGlobal(force: Boolean): Outcome<Unit> {
        if (!CachePolicy.shouldRefresh(globalMarketDao.fetchedAt(), CachePolicy.GLOBAL_TTL, force, clock)) return Done
        return coordinator.execute(KEY_GLOBAL) {
            globalMarketDao.upsert(api.global().data.toEntity(clock.millis()))
        }
    }

    override suspend fun refreshCoin(id: String, force: Boolean): Outcome<Unit> {
        if (!CachePolicy.shouldRefresh(coinDao.fetchedAt(id), CachePolicy.MARKET_TTL, force, clock)) return Done
        val outcome = coordinator.execute("$KEY_COIN$id") {
            val dto = api.markets(Currency.USD, ids = id, perPage = 1).firstOrNull() ?: return@execute false
            val isListed = coinDao.isListed(id) ?: false
            coinDao.upsert(listOf(dto.toEntity(fetchedAtMillis = clock.millis(), isListed = isListed)))
            true
        }
        return when (outcome) {
            is Outcome.Failure -> outcome
            is Outcome.Success -> if (outcome.value) Done else Outcome.Failure(AppError.NotFound)
        }
    }

    override suspend fun searchRemote(query: String): Outcome<List<CoinSearchHit>> {
        val key = query.trim().lowercase()
        synchronized(searchCache) { searchCache[key] }
            ?.takeIf { Duration.between(it.fetchedAt, clock.instant()) < SEARCH_TTL }
            ?.let { return Outcome.Success(it.value) }
        val outcome = coordinator.execute("$KEY_SEARCH$key") { api.search(key).coins.map { it.toDomain() } }
        if (outcome is Outcome.Success) {
            synchronized(searchCache) { searchCache[key] = Cached(outcome.value, clock.instant()) }
        }
        return outcome
    }

    private companion object {
        const val KEY_MARKETS = "markets"
        const val KEY_WATCHED = "markets:watched"
        const val KEY_GLOBAL = "global"
        const val KEY_COIN = "coin:"
        const val KEY_SEARCH = "search:"
        const val MAX_IDS_PER_REQUEST = 250
        const val SEARCH_CACHE_SIZE = 32
        val SEARCH_TTL: Duration = Duration.ofMinutes(10)
    }
}
