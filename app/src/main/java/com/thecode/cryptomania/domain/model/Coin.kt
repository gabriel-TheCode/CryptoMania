package com.thecode.cryptomania.domain.model

import java.time.Instant

/**
 * Market snapshot of a single asset, priced in [Currency.USD].
 *
 * Every numeric field is nullable because CoinGecko returns `null` for assets that are
 * not tracked well enough (new listings, delisted tokens, uncapped supply, ...).
 */
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val marketCapRank: Int?,
    val price: Double?,
    val marketCap: Double?,
    val fullyDilutedValuation: Double?,
    val volume24h: Double?,
    val high24h: Double?,
    val low24h: Double?,
    val change1hPercent: Double?,
    val change24hPercent: Double?,
    val change7dPercent: Double?,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val allTimeHigh: Double?,
    val allTimeHighChangePercent: Double?,
    val allTimeHighDate: Instant?,
    /** Hourly-ish prices over the last 7 days, oldest first. Empty when unavailable. */
    val sparkline7d: List<Double>,
    val lastUpdated: Instant?,
)

/** Global market aggregates from CoinGecko's `/global` endpoint. */
data class GlobalMarket(
    val totalMarketCap: Double,
    val totalVolume24h: Double,
    val marketCapChange24hPercent: Double?,
    val btcDominancePercent: Double?,
    val ethDominancePercent: Double?,
    val activeCryptocurrencies: Int?,
)

/** Qualitative metadata about a coin; slow-changing, cached for a day. */
data class CoinProfile(
    val id: String,
    val description: String,
    val homepageUrl: String?,
    val categories: List<String>,
    val genesisDate: String?,
    val hashingAlgorithm: String?,
)

/** A lightweight search result that may point to a coin we have no market data for yet. */
data class CoinSearchHit(
    val id: String,
    val name: String,
    val symbol: String,
    val thumbUrl: String?,
    val marketCapRank: Int?,
)

data class Exchange(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val country: String?,
    val yearEstablished: Int?,
    val websiteUrl: String?,
    val trustScore: Int?,
    val trustScoreRank: Int?,
    val volume24hBtc: Double?,
)

object Currency {
    /** CoinGecko `vs_currency`. The whole cache is keyed on a single quote currency. */
    const val USD = "usd"
}
