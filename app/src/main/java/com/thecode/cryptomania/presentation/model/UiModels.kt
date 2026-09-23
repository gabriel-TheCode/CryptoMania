package com.thecode.cryptomania.presentation.model

import androidx.compose.runtime.Immutable
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.presentation.util.Formatters

enum class Trend { Up, Down, Flat, Unknown }

/** A formatted change value plus its direction, so color is never the only cue. */
@Immutable
data class ChangeUi(val trend: Trend, val text: String) {
    companion object {
        fun of(percent: Double?, formatters: Formatters): ChangeUi = ChangeUi(
            trend = when {
                percent == null -> Trend.Unknown
                formatters.percent(percent) == formatters.percent(0.0) -> Trend.Flat
                percent > 0 -> Trend.Up
                else -> Trend.Down
            },
            text = formatters.percent(percent),
        )
    }
}

/** Pre-formatted coin row: Composables only lay out strings, they never format numbers. */
@Immutable
data class CoinRowUi(
    val id: String,
    val name: String,
    val symbol: String,
    val logoUrl: String?,
    val rank: String,
    val price: String,
    /** Raw price, used to animate price-tick feedback. */
    val priceValue: Double?,
    val change24h: ChangeUi,
    val marketCap: String,
    val volume: String,
    val sparkline: List<Double>,
    val sparklineTrend: Trend,
    val isWatched: Boolean,
)

fun Coin.toRowUi(formatters: Formatters, isWatched: Boolean) = CoinRowUi(
    id = id,
    name = name,
    symbol = symbol,
    logoUrl = imageUrl,
    rank = marketCapRank?.toString() ?: Formatters.PLACEHOLDER,
    price = formatters.price(price),
    priceValue = price,
    change24h = ChangeUi.of(change24hPercent, formatters),
    marketCap = formatters.compactCurrency(marketCap),
    volume = formatters.compactCurrency(volume24h),
    sparkline = sparkline7d,
    sparklineTrend = when {
        sparkline7d.size < 2 -> Trend.Unknown
        sparkline7d.last() >= sparkline7d.first() -> Trend.Up
        else -> Trend.Down
    },
    isWatched = isWatched,
)
