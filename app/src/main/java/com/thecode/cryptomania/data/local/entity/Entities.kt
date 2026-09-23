package com.thecode.cryptomania.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached market snapshot. [isListed] marks membership of the latest top-coins fetch;
 * coins opened from search are cached too but stay out of the market list.
 */
@Entity(tableName = "coins", indices = [Index("isListed", "marketCapRank")])
data class CoinEntity(
    @PrimaryKey val id: String,
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
    val allTimeHighDateMillis: Long?,
    /** Comma-separated, down-sampled 7-day prices. */
    val sparkline: String,
    val lastUpdatedMillis: Long?,
    val fetchedAtMillis: Long,
    val isListed: Boolean,
)

@Entity(tableName = "global_market")
data class GlobalMarketEntity(
    @PrimaryKey val id: Int = 0,
    val totalMarketCap: Double,
    val totalVolume24h: Double,
    val marketCapChange24hPercent: Double?,
    val btcDominancePercent: Double?,
    val ethDominancePercent: Double?,
    val activeCryptocurrencies: Int?,
    val fetchedAtMillis: Long,
)

@Entity(tableName = "price_history", primaryKeys = ["coinId", "range"])
data class PriceHistoryEntity(
    val coinId: String,
    val range: String,
    /** `epochMillis:price` pairs separated by commas. */
    val points: String,
    val fetchedAtMillis: Long,
)

@Entity(tableName = "coin_profiles")
data class CoinProfileEntity(
    @PrimaryKey val id: String,
    val description: String,
    val homepageUrl: String?,
    /** Newline-separated. */
    val categories: String,
    val genesisDate: String?,
    val hashingAlgorithm: String?,
    val fetchedAtMillis: Long,
)

@Entity(tableName = "exchanges")
data class ExchangeEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val name: String,
    val imageUrl: String?,
    val country: String?,
    val yearEstablished: Int?,
    val websiteUrl: String?,
    val trustScore: Int?,
    val trustScoreRank: Int?,
    val volume24hBtc: Double?,
    val fetchedAtMillis: Long,
)

/** Ordered ids of CoinGecko trending coins ("Hot"). Market data lives in [CoinEntity]. */
@Entity(tableName = "trending")
data class TrendingEntity(
    @PrimaryKey val coinId: String,
    val position: Int,
    val fetchedAtMillis: Long,
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val coinId: String,
    val addedAtMillis: Long,
)
