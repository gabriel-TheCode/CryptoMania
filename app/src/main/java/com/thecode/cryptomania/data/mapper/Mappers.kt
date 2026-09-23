package com.thecode.cryptomania.data.mapper

import com.thecode.cryptomania.data.local.entity.CoinEntity
import com.thecode.cryptomania.data.local.entity.CoinProfileEntity
import com.thecode.cryptomania.data.local.entity.ExchangeEntity
import com.thecode.cryptomania.data.local.entity.GlobalMarketEntity
import com.thecode.cryptomania.data.local.entity.PriceHistoryEntity
import com.thecode.cryptomania.data.remote.dto.CoinMarketDto
import com.thecode.cryptomania.data.remote.dto.CoinProfileDto
import com.thecode.cryptomania.data.remote.dto.ExchangeDto
import com.thecode.cryptomania.data.remote.dto.GlobalDataDto
import com.thecode.cryptomania.data.remote.dto.MarketChartDto
import com.thecode.cryptomania.data.remote.dto.SearchCoinDto
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.CoinProfile
import com.thecode.cryptomania.domain.model.CoinSearchHit
import com.thecode.cryptomania.domain.model.Currency
import com.thecode.cryptomania.domain.model.Exchange
import com.thecode.cryptomania.domain.model.GlobalMarket
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.domain.model.PriceHistory
import java.time.Instant
import java.time.format.DateTimeParseException

// DTO -> entity -> domain. Domain models never see wire or storage details.

private const val SPARKLINE_POINTS = 48

fun CoinMarketDto.toEntity(fetchedAtMillis: Long, isListed: Boolean) = CoinEntity(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = image,
    marketCapRank = marketCapRank,
    price = currentPrice,
    marketCap = marketCap,
    fullyDilutedValuation = fullyDilutedValuation,
    volume24h = totalVolume,
    high24h = high24h,
    low24h = low24h,
    change1hPercent = change1h,
    change24hPercent = priceChangePercentage24h,
    change7dPercent = change7d,
    circulatingSupply = circulatingSupply,
    totalSupply = totalSupply,
    maxSupply = maxSupply,
    allTimeHigh = ath,
    allTimeHighChangePercent = athChangePercentage,
    allTimeHighDateMillis = athDate.parseInstant()?.toEpochMilli(),
    sparkline = sparkline?.price.orEmpty().filterNotNull().downsample(SPARKLINE_POINTS).joinToString(","),
    lastUpdatedMillis = lastUpdated.parseInstant()?.toEpochMilli(),
    fetchedAtMillis = fetchedAtMillis,
    isListed = isListed,
)

fun CoinEntity.toDomain() = Coin(
    id = id,
    symbol = symbol.uppercase(),
    name = name,
    imageUrl = imageUrl,
    marketCapRank = marketCapRank,
    price = price,
    marketCap = marketCap,
    fullyDilutedValuation = fullyDilutedValuation,
    volume24h = volume24h,
    high24h = high24h,
    low24h = low24h,
    change1hPercent = change1hPercent,
    change24hPercent = change24hPercent,
    change7dPercent = change7dPercent,
    circulatingSupply = circulatingSupply,
    totalSupply = totalSupply,
    maxSupply = maxSupply,
    allTimeHigh = allTimeHigh,
    allTimeHighChangePercent = allTimeHighChangePercent,
    allTimeHighDate = allTimeHighDateMillis?.let(Instant::ofEpochMilli),
    sparkline7d = if (sparkline.isEmpty()) emptyList() else sparkline.split(',').mapNotNull(String::toDoubleOrNull),
    lastUpdated = lastUpdatedMillis?.let(Instant::ofEpochMilli),
)

fun GlobalDataDto.toEntity(fetchedAtMillis: Long) = GlobalMarketEntity(
    totalMarketCap = totalMarketCap[Currency.USD] ?: 0.0,
    totalVolume24h = totalVolume[Currency.USD] ?: 0.0,
    marketCapChange24hPercent = marketCapChangePercentage24hUsd,
    btcDominancePercent = marketCapPercentage["btc"],
    ethDominancePercent = marketCapPercentage["eth"],
    activeCryptocurrencies = activeCryptocurrencies,
    fetchedAtMillis = fetchedAtMillis,
)

fun GlobalMarketEntity.toDomain() = GlobalMarket(
    totalMarketCap = totalMarketCap,
    totalVolume24h = totalVolume24h,
    marketCapChange24hPercent = marketCapChange24hPercent,
    btcDominancePercent = btcDominancePercent,
    ethDominancePercent = ethDominancePercent,
    activeCryptocurrencies = activeCryptocurrencies,
)

/** Drops malformed pairs and keeps points in chronological order. */
fun MarketChartDto.toPricePoints(): List<PricePoint> = prices
    .mapNotNull { pair ->
        val time = pair.getOrNull(0)?.toLong() ?: return@mapNotNull null
        val price = pair.getOrNull(1)?.takeIf { it.isFinite() } ?: return@mapNotNull null
        PricePoint(time, price)
    }
    .sortedBy { it.epochMillis }
    .distinctBy { it.epochMillis }

fun List<PricePoint>.toEntity(coinId: String, range: ChartRange, fetchedAtMillis: Long) = PriceHistoryEntity(
    coinId = coinId,
    range = range.name,
    points = joinToString(",") { "${it.epochMillis}:${it.price}" },
    fetchedAtMillis = fetchedAtMillis,
)

fun PriceHistoryEntity.toDomain(): PriceHistory? {
    val range = ChartRange.entries.firstOrNull { it.name == this.range } ?: return null
    val points = if (points.isEmpty()) {
        emptyList()
    } else {
        points.split(',').mapNotNull { token ->
            val separator = token.indexOf(':')
            if (separator < 0) return@mapNotNull null
            val time = token.substring(0, separator).toLongOrNull() ?: return@mapNotNull null
            val price = token.substring(separator + 1).toDoubleOrNull() ?: return@mapNotNull null
            PricePoint(time, price)
        }
    }
    return PriceHistory(coinId, range, points)
}

fun CoinProfileDto.toEntity(fetchedAtMillis: Long) = CoinProfileEntity(
    id = id,
    description = description["en"].orEmpty().stripHtml(),
    homepageUrl = links?.homepage.orEmpty().firstOrNull { !it.isNullOrBlank() },
    categories = categories.filterNotNull().filter { it.isNotBlank() }.joinToString("\n"),
    genesisDate = genesisDate,
    hashingAlgorithm = hashingAlgorithm,
    fetchedAtMillis = fetchedAtMillis,
)

fun CoinProfileEntity.toDomain() = CoinProfile(
    id = id,
    description = description,
    homepageUrl = homepageUrl,
    categories = if (categories.isEmpty()) emptyList() else categories.split('\n'),
    genesisDate = genesisDate,
    hashingAlgorithm = hashingAlgorithm,
)

fun ExchangeDto.toEntity(position: Int, fetchedAtMillis: Long) = ExchangeEntity(
    id = id,
    position = position,
    name = name,
    imageUrl = image,
    country = country?.takeIf { it.isNotBlank() },
    yearEstablished = yearEstablished,
    websiteUrl = url?.takeIf { it.startsWith("https://") || it.startsWith("http://") },
    trustScore = trustScore,
    trustScoreRank = trustScoreRank,
    volume24hBtc = tradeVolume24hBtc,
    fetchedAtMillis = fetchedAtMillis,
)

fun ExchangeEntity.toDomain() = Exchange(
    id = id,
    name = name,
    imageUrl = imageUrl,
    country = country,
    yearEstablished = yearEstablished,
    websiteUrl = websiteUrl,
    trustScore = trustScore,
    trustScoreRank = trustScoreRank,
    volume24hBtc = volume24hBtc,
)

fun SearchCoinDto.toDomain() = CoinSearchHit(
    id = id,
    name = name,
    symbol = symbol.uppercase(),
    thumbUrl = thumb,
    marketCapRank = marketCapRank,
)

/** Evenly samples [target] values, always keeping the first and the last one. */
internal fun List<Double>.downsample(target: Int): List<Double> {
    if (size <= target || target < 2) return this
    val step = (size - 1).toDouble() / (target - 1)
    return List(target) { index -> this[Math.round(index * step).toInt()] }
}

private fun String?.parseInstant(): Instant? = try {
    this?.let(Instant::parse)
} catch (_: DateTimeParseException) {
    null
}

/** CoinGecko descriptions contain anchor tags and CRLFs; the app renders plain text. */
internal fun String.stripHtml(): String = replace(Regex("<[^>]*>"), "")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("\r\n", "\n")
    .replace(Regex("\n{3,}"), "\n\n")
    .trim()
