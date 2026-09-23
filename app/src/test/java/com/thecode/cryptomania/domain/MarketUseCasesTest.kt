package com.thecode.cryptomania.domain

import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.PriceHistory
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.domain.usecase.MarketFilter
import com.thecode.cryptomania.domain.usecase.MarketOverview
import com.thecode.cryptomania.domain.usecase.RankLocalSearchUseCase
import com.thecode.cryptomania.testutil.coin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class MarketUseCasesTest {

    private val coins = listOf(
        coin("bitcoin", rank = 1, change24h = -2.0, symbol = "BTC", name = "Bitcoin"),
        coin("ethereum", rank = 2, change24h = 5.0, symbol = "ETH", name = "Ethereum"),
        coin("bitcoin-cash", rank = 20, change24h = 12.0, symbol = "BCH", name = "Bitcoin Cash"),
        coin("microcap", rank = 240, change24h = 400.0, symbol = "MIC", name = "Micro"),
        coin("wrapped-bitcoin", rank = 15, change24h = null, symbol = "WBTC", name = "Wrapped Bitcoin"),
    )
    private val overview = MarketOverview(coins, null, setOf("ethereum"), listOf(coins[1]), hotCoins = listOf(coins[3], coins[0]), fetchedAt = Instant.EPOCH)

    @Test
    fun `movers ignore micro caps and coins without data`() {
        assertEquals(listOf("bitcoin-cash", "ethereum"), overview.topGainers.map { it.id })
        assertEquals(listOf("bitcoin"), overview.topLosers.map { it.id })
    }

    @Test
    fun `filters sort locally without touching the network`() {
        assertEquals(coins, overview.filtered(MarketFilter.All))
        assertEquals("hot keeps trending order", listOf("microcap", "bitcoin"), overview.filtered(MarketFilter.Hot).map { it.id })
        assertEquals(listOf("ethereum"), overview.filtered(MarketFilter.Watchlist).map { it.id })
        assertEquals(listOf("microcap", "bitcoin-cash", "ethereum"), overview.filtered(MarketFilter.Gainers).map { it.id })
        assertEquals(listOf("bitcoin"), overview.filtered(MarketFilter.Losers).map { it.id })
    }

    @Test
    fun `local search ranks exact symbol, then prefixes, then substrings`() {
        val rank = RankLocalSearchUseCase()

        assertEquals("bitcoin", rank(coins, "btc").first().id)
        assertEquals(listOf("bitcoin", "bitcoin-cash", "wrapped-bitcoin"), rank(coins, " Bitcoin").map { it.id })
        assertEquals(emptyList<Any>(), rank(coins, "   "))
        assertEquals("single letters only match prefixes", listOf("ethereum"), rank(coins, "e").map { it.id })
    }

    @Test
    fun `price history change is computed from first and last point`() {
        val history = PriceHistory("btc", ChartRange.OneWeek, listOf(PricePoint(1, 100.0), PricePoint(2, 90.0), PricePoint(3, 110.0)))
        assertEquals(10.0, history.changePercent!!, 1e-9)
        assertEquals(90.0, history.low!!, 0.0)
        assertNull(PriceHistory("btc", ChartRange.OneDay, listOf(PricePoint(1, 1.0))).changePercent)
    }
}
