package com.thecode.cryptomania.domain.repository

import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.CoinProfile
import com.thecode.cryptomania.domain.model.CoinSearchHit
import com.thecode.cryptomania.domain.model.Exchange
import com.thecode.cryptomania.domain.model.GlobalMarket
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.model.PriceHistory
import com.thecode.cryptomania.domain.model.ThemePreference
import com.thecode.cryptomania.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Contracts follow one pattern: `observe*` streams from the local cache (the single source
 * of truth) and `refresh*` asks the network to update that cache. Refreshes are skipped
 * while cached data is still fresh unless `force` is set.
 */
interface MarketRepository {
    /** Top coins by market cap, or null before the first successful fetch. */
    fun observeTopCoins(): Flow<Cached<List<Coin>>?>

    fun observeGlobalMarket(): Flow<GlobalMarket?>

    fun observeCoin(id: String): Flow<Coin?>

    fun observeCoins(ids: Set<String>): Flow<List<Coin>>

    suspend fun refreshMarket(force: Boolean = false): Outcome<Unit>

    /** Ensures market data exists for a coin that may not be part of the top list. */
    suspend fun refreshCoin(id: String, force: Boolean = false): Outcome<Unit>

    suspend fun searchRemote(query: String): Outcome<List<CoinSearchHit>>
}

interface CoinDetailsRepository {
    fun observePriceHistory(coinId: String, range: ChartRange): Flow<Cached<PriceHistory>?>

    suspend fun refreshPriceHistory(coinId: String, range: ChartRange, force: Boolean = false): Outcome<Unit>

    fun observeProfile(coinId: String): Flow<CoinProfile?>

    suspend fun refreshProfile(coinId: String): Outcome<Unit>
}

interface ExchangeRepository {
    fun observeExchanges(): Flow<Cached<List<Exchange>>?>

    suspend fun refreshExchanges(force: Boolean = false): Outcome<Unit>
}

interface WatchlistRepository {
    fun observeWatchlist(): Flow<Set<String>>

    suspend fun toggle(coinId: String)
}

interface SettingsRepository {
    val settings: Flow<UserSettings>
    val recentSearches: Flow<List<String>>

    suspend fun setTheme(theme: ThemePreference)
    suspend fun setColorBlindFriendly(enabled: Boolean)
    suspend fun setOnboardingCompleted()
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()
}

interface CacheRepository {
    /** Drops cached market data (the watchlist and settings are kept). */
    suspend fun clearMarketCache()
}

/** Connectivity hint. Only a hint: actual request results remain the source of truth. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
