package com.thecode.cryptomania.testutil

import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.CoinProfile
import com.thecode.cryptomania.domain.model.CoinSearchHit
import com.thecode.cryptomania.domain.model.Done
import com.thecode.cryptomania.domain.model.GlobalMarket
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.model.PriceHistory
import com.thecode.cryptomania.domain.model.ThemePreference
import com.thecode.cryptomania.domain.model.UserSettings
import com.thecode.cryptomania.domain.repository.CoinDetailsRepository
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.NetworkMonitor
import com.thecode.cryptomania.domain.repository.SettingsRepository
import com.thecode.cryptomania.domain.repository.WatchlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = kotlinx.coroutines.Dispatchers.resetMain()
}

/** Clock the test can move forward. */
class MutableClock(var now: Instant = Instant.parse("2026-09-23T12:00:00Z")) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId?): Clock = this
    override fun instant(): Instant = now
    fun advance(duration: Duration) {
        now = now.plus(duration)
    }
}

fun coin(
    id: String,
    rank: Int? = 1,
    price: Double? = 100.0,
    change24h: Double? = 1.0,
    symbol: String = id.take(3).uppercase(),
    name: String = id.replaceFirstChar(Char::uppercase),
) = Coin(
    id = id, symbol = symbol, name = name, imageUrl = null, marketCapRank = rank, price = price,
    marketCap = 1e9, fullyDilutedValuation = null, volume24h = 1e8, high24h = 110.0, low24h = 90.0,
    change1hPercent = 0.1, change24hPercent = change24h, change7dPercent = 2.0, circulatingSupply = 10.0,
    totalSupply = 10.0, maxSupply = 20.0, allTimeHigh = 200.0, allTimeHighChangePercent = -50.0,
    allTimeHighDate = null, sparkline7d = listOf(1.0, 2.0, 3.0), lastUpdated = Instant.EPOCH,
)

class FakeMarketRepository : MarketRepository {
    val topCoins = MutableStateFlow<Cached<List<Coin>>?>(null)
    val global = MutableStateFlow<GlobalMarket?>(null)
    val trending = MutableStateFlow<List<String>>(emptyList())
    var refreshResult: Outcome<Unit> = Done
    var refreshCoinResult: Outcome<Unit> = Done
    var searchResult: Outcome<List<CoinSearchHit>> = Outcome.Success(emptyList())
    val refreshCalls = mutableListOf<Boolean>()
    val searchQueries = mutableListOf<String>()

    override fun observeTopCoins() = topCoins
    override fun observeGlobalMarket() = global
    override fun observeTrendingIds() = trending
    override fun observeCoin(id: String): Flow<Coin?> = topCoins.map { cached -> cached?.value?.firstOrNull { it.id == id } }
    override fun observeCoins(ids: Set<String>): Flow<List<Coin>> = topCoins.map { cached -> cached?.value.orEmpty().filter { it.id in ids } }
    override suspend fun refreshMarket(force: Boolean): Outcome<Unit> = refreshResult.also { refreshCalls += force }
    override suspend fun refreshCoin(id: String, force: Boolean) = refreshCoinResult
    override suspend fun searchRemote(query: String) = searchResult.also { searchQueries += query }
}

class FakeCoinDetailsRepository : CoinDetailsRepository {
    val histories = mutableMapOf<ChartRange, MutableStateFlow<Cached<PriceHistory>?>>()
    val profile = MutableStateFlow<CoinProfile?>(null)
    val refreshedRanges = mutableListOf<ChartRange>()
    var chartResult: Outcome<Unit> = Done

    fun history(range: ChartRange) = histories.getOrPut(range) { MutableStateFlow(null) }
    override fun observePriceHistory(coinId: String, range: ChartRange) = history(range)
    override suspend fun refreshPriceHistory(coinId: String, range: ChartRange, force: Boolean) =
        chartResult.also { refreshedRanges += range }
    override fun observeProfile(coinId: String) = profile
    override suspend fun refreshProfile(coinId: String) = Done
}

class FakeWatchlistRepository : WatchlistRepository {
    val ids = MutableStateFlow<Set<String>>(emptySet())
    override fun observeWatchlist() = ids
    override suspend fun toggle(coinId: String) {
        ids.value = if (coinId in ids.value) ids.value - coinId else ids.value + coinId
    }
}

class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(UserSettings())
    override val recentSearches = MutableStateFlow<List<String>>(emptyList())
    override suspend fun setTheme(theme: ThemePreference) {
        settings.value = settings.value.copy(theme = theme)
    }
    override suspend fun setColorBlindFriendly(enabled: Boolean) {
        settings.value = settings.value.copy(colorBlindFriendly = enabled)
    }
    override suspend fun setOnboardingCompleted() {
        settings.value = settings.value.copy(onboardingCompleted = true)
    }
    override suspend fun addRecentSearch(query: String) {
        recentSearches.value = listOf(query) + recentSearches.value.filterNot { it == query }
    }
    override suspend fun clearRecentSearches() {
        recentSearches.value = emptyList()
    }
}

class FakeNetworkMonitor(online: Boolean = true) : NetworkMonitor {
    override val isOnline = MutableStateFlow(online)
}
