package com.thecode.cryptomania.data

import com.thecode.cryptomania.data.mapper.downsample
import com.thecode.cryptomania.data.mapper.stripHtml
import com.thecode.cryptomania.data.mapper.toDomain
import com.thecode.cryptomania.data.mapper.toEntity
import com.thecode.cryptomania.data.mapper.toPricePoints
import com.thecode.cryptomania.data.remote.dto.CoinMarketDto
import com.thecode.cryptomania.data.remote.dto.MarketChartDto
import com.thecode.cryptomania.data.remote.dto.SparklineDto
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.PricePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class MappersTest {

    @Test
    fun `missing numeric fields stay null instead of becoming zero`() {
        val coin = CoinMarketDto(id = "new-token", symbol = "new", name = "New Token")
            .toEntity(fetchedAtMillis = 0, isListed = true)
            .toDomain()

        assertNull(coin.price)
        assertNull(coin.maxSupply)
        assertNull(coin.marketCapRank)
        assertEquals("NEW", coin.symbol)
        assertEquals(emptyList<Double>(), coin.sparkline7d)
    }

    @Test
    fun `coin survives the entity round trip`() {
        val dto = CoinMarketDto(
            id = "bitcoin", symbol = "btc", name = "Bitcoin", currentPrice = 84_456.12,
            marketCap = 1.69e12, athDate = "2025-10-06T10:57:42.000Z",
            sparkline = SparklineDto(listOf(1.0, null, 3.0)),
        )
        val coin = dto.toEntity(fetchedAtMillis = 5, isListed = true).toDomain()

        assertEquals(84_456.12, coin.price!!, 0.0)
        assertEquals(1.69e12, coin.marketCap!!, 0.0)
        assertEquals(Instant.parse("2025-10-06T10:57:42Z"), coin.allTimeHighDate)
        assertEquals(listOf(1.0, 3.0), coin.sparkline7d)
    }

    @Test
    fun `chart parsing drops malformed pairs and sorts by time`() {
        val points = MarketChartDto(
            prices = listOf(listOf(3000.0, 3.0), listOf(1000.0, 1.0), listOf(2000.0), listOf(4000.0, null), listOf(1000.0, 9.0)),
        ).toPricePoints()

        assertEquals(listOf(PricePoint(1000, 1.0), PricePoint(3000, 3.0)), points)
    }

    @Test
    fun `price history survives the entity round trip`() {
        val points = listOf(PricePoint(1, 0.00001234), PricePoint(2, 65_000.5))
        val history = points.toEntity("pepe", ChartRange.OneWeek, fetchedAtMillis = 9).toDomain()!!

        assertEquals(ChartRange.OneWeek, history.range)
        assertEquals(points, history.points)
    }

    @Test
    fun `downsampling keeps both ends and the requested size`() {
        val values = (0..167).map(Int::toDouble)
        val sampled = values.downsample(48)

        assertEquals(48, sampled.size)
        assertEquals(0.0, sampled.first(), 0.0)
        assertEquals(167.0, sampled.last(), 0.0)
        assertEquals(values.take(10), values.take(10).downsample(48))
    }

    @Test
    fun `descriptions are rendered as plain text`() {
        assertEquals(
            "Bitcoin & \"friends\" <3",
            "<a href=\"https://bitcoin.org\">Bitcoin</a> &amp; &quot;friends&quot; &lt;3".stripHtml(),
        )
    }
}
