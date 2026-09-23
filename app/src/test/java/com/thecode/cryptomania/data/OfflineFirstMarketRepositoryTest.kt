package com.thecode.cryptomania.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.thecode.cryptomania.data.local.CryptoManiaDatabase
import com.thecode.cryptomania.data.local.entity.WatchlistEntity
import com.thecode.cryptomania.data.network.RequestCoordinator
import com.thecode.cryptomania.data.remote.CoinGeckoApi
import com.thecode.cryptomania.data.repository.OfflineFirstMarketRepository
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Done
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.testutil.MutableClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
class OfflineFirstMarketRepositoryTest {

    private val server = MockWebServer()
    private val requests = CopyOnWriteArrayList<String>()
    private val clock = MutableClock()
    private lateinit var db: CryptoManiaDatabase

    /** Per-path HTTP status; everything else answers with the canned payloads below. */
    private val statusByPath = mutableMapOf<String, Int>()

    @Before
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.url.encodedPath
                requests += request.url.toString()
                statusByPath[path]?.let { return MockResponse.Builder().code(it).body("{}").build() }
                val body = when {
                    path.endsWith("/coins/markets") && request.url.queryParameter("ids") != null -> WATCHED_JSON
                    path.endsWith("/coins/markets") -> MARKETS_JSON
                    path.endsWith("/global") -> GLOBAL_JSON
                    path.endsWith("/search/trending") -> TRENDING_JSON
                    path.endsWith("/search") -> SEARCH_JSON
                    else -> return MockResponse.Builder().code(404).build()
                }
                return MockResponse.Builder().code(200).body(body).build()
            }
        }
        server.start()
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CryptoManiaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        server.close()
    }

    private fun TestScope.repository(scope: CoroutineScope = backgroundScope) = OfflineFirstMarketRepository(
        api = CoinGeckoApi.create(OkHttpClient(), server.url("/api/v3/").toString()),
        coinDao = db.coinDao(),
        globalMarketDao = db.globalMarketDao(),
        trendingDao = db.trendingDao(),
        coordinator = RequestCoordinator(clock, scope),
        clock = clock,
    )

    private fun marketRequests() = requests.count { "/coins/markets" in it && "ids=" !in it }

    @Test
    fun `first refresh stores coins and global data, nulls included`() = runTest {
        val repository = repository()

        assertEquals(Done, repository.refreshMarket())

        val coins = repository.observeTopCoins().first()!!.value
        assertEquals(listOf("bitcoin", "ethereum"), coins.map { it.id })
        assertNull("ETH has no max supply", coins[1].maxSupply)
        assertEquals(2.88e12, repository.observeGlobalMarket().first()!!.totalMarketCap, 1.0)
    }

    @Test
    fun `fresh cache does not hit the network, stale cache does`() = runTest {
        val repository = repository()
        repository.refreshMarket()
        repository.refreshMarket()
        repository.refreshMarket(force = true) // Inside the 30 s window: ignored as well.
        assertEquals(1, marketRequests())

        clock.advance(Duration.ofMinutes(3))
        repository.refreshMarket()
        assertEquals(2, marketRequests())
    }

    @Test
    fun `opening a listed coin costs no request`() = runTest {
        val repository = repository()
        repository.refreshMarket()
        val before = requests.size

        assertEquals(Done, repository.refreshCoin("bitcoin"))
        assertEquals(before, requests.size)
    }

    @Test
    fun `failures keep cached data and surface a domain error`() = runTest {
        val repository = repository()
        repository.refreshMarket()
        clock.advance(Duration.ofMinutes(3))
        statusByPath["/api/v3/coins/markets"] = 503

        val outcome = repository.refreshMarket()

        assertEquals(Outcome.Failure(AppError.ServerUnavailable), outcome)
        assertEquals(2, repository.observeTopCoins().first()!!.value.size)
    }

    @Test
    fun `rate limiting stops requests during the cooldown`() = runTest {
        val repository = repository()
        statusByPath["/api/v3/coins/markets"] = 429
        statusByPath["/api/v3/global"] = 429

        assertEquals(Outcome.Failure(AppError.RateLimited), repository.refreshMarket())
        val afterLimit = requests.size
        assertEquals(Outcome.Failure(AppError.RateLimited), repository.refreshMarket(force = true))
        assertEquals(afterLimit, requests.size)
    }

    @Test
    fun `watched coins outside the top list are refreshed in one batched call`() = runTest {
        db.watchlistDao().insert(WatchlistEntity("pepecoin", 0))
        db.watchlistDao().insert(WatchlistEntity("dogwifhat", 1))
        val repository = repository()

        repository.refreshMarket()

        val batched = requests.filter { "ids=" in it }
        assertEquals(1, batched.size)
        assertEquals(setOf("pepecoin", "dogwifhat"), repository.observeCoins(setOf("pepecoin", "dogwifhat")).first().map { it.id }.toSet())
        assertEquals("unlisted coins stay out of the market list", 2, repository.observeTopCoins().first()!!.value.size)
    }

    @Test
    fun `trending coins are cached in order, with market data fetched in the same batch`() = runTest {
        val repository = repository()

        repository.refreshMarket()

        assertEquals(listOf("pepecoin", "bitcoin"), repository.observeTrendingIds().first())
        assertEquals(1, requests.count { "/search/trending" in it })
        assertEquals("only unlisted trending coins are batched", 1, requests.count { "ids=" in it })
        assertEquals(setOf("pepecoin", "bitcoin"), repository.observeCoins(setOf("pepecoin", "bitcoin")).first().map { it.id }.toSet())
    }

    @Test
    fun `identical searches are served from memory`() = runTest {
        val repository = repository()

        val first = repository.searchRemote("Pepe")
        repository.searchRemote("pepe ")

        assertNotNull((first as Outcome.Success).value.firstOrNull { it.id == "pepecoin" })
        assertEquals(1, requests.count { "/search" in it })
    }

    private companion object {
        const val MARKETS_JSON = """[
          {"id":"bitcoin","symbol":"btc","name":"Bitcoin","image":"https://img/btc.png","current_price":84456,
           "market_cap":1697050447995,"market_cap_rank":1,"total_volume":43890904157,"high_24h":87251,"low_24h":85266,
           "price_change_percentage_24h":-2.01725,"circulating_supply":20088325.0,"max_supply":21000000.0,"ath":126080,
           "ath_date":"2025-10-06T10:57:42.000Z","last_updated":"2026-09-23T14:56:00.000Z",
           "sparkline_in_7d":{"price":[75608.3,75628.2,76006.3]},"price_change_percentage_7d_in_currency":11.7,
           "some_new_field":"ignored"},
          {"id":"ethereum","symbol":"eth","name":"Ethereum","image":null,"current_price":2670.5,"market_cap":3.2e11,
           "market_cap_rank":2,"max_supply":null,"price_change_percentage_24h":-2.69,"roi":null}
        ]"""
        const val WATCHED_JSON = """[
          {"id":"pepecoin","symbol":"pepecoin","name":"PepeCoin","current_price":0.15,"market_cap_rank":1045},
          {"id":"dogwifhat","symbol":"wif","name":"dogwifhat","current_price":1.2,"market_cap_rank":300}
        ]"""
        const val GLOBAL_JSON = """{"data":{"active_cryptocurrencies":21532,"total_market_cap":{"usd":2.88e12,"btc":3.4e7},
          "total_volume":{"usd":1.28e11},"market_cap_percentage":{"btc":58.7,"eth":11.3},
          "market_cap_change_percentage_24h_usd":-4.2}}"""
        const val TRENDING_JSON = """{"coins":[{"item":{"id":"pepecoin","name":"PepeCoin","score":0}},
          {"item":{"id":"bitcoin","name":"Bitcoin","score":1}}],"nfts":[],"categories":[]}"""
        const val SEARCH_JSON = """{"coins":[{"id":"pepecoin","name":"PepeCoin","symbol":"PEPECOIN","thumb":"t","market_cap_rank":1045}],
          "exchanges":[],"nfts":[]}"""
    }
}
