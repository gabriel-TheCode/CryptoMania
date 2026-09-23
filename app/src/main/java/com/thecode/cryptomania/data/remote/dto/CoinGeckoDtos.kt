package com.thecode.cryptomania.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wire models for the CoinGecko v3 API. Nullability mirrors the official OpenAPI spec:
// most numeric fields may be null for thinly tracked assets.

@Serializable
data class CoinMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String? = null,
    @SerialName("current_price") val currentPrice: Double? = null,
    @SerialName("market_cap") val marketCap: Double? = null,
    @SerialName("market_cap_rank") val marketCapRank: Int? = null,
    @SerialName("fully_diluted_valuation") val fullyDilutedValuation: Double? = null,
    @SerialName("total_volume") val totalVolume: Double? = null,
    @SerialName("high_24h") val high24h: Double? = null,
    @SerialName("low_24h") val low24h: Double? = null,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @SerialName("circulating_supply") val circulatingSupply: Double? = null,
    @SerialName("total_supply") val totalSupply: Double? = null,
    @SerialName("max_supply") val maxSupply: Double? = null,
    val ath: Double? = null,
    @SerialName("ath_change_percentage") val athChangePercentage: Double? = null,
    @SerialName("ath_date") val athDate: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    @SerialName("sparkline_in_7d") val sparkline: SparklineDto? = null,
    @SerialName("price_change_percentage_1h_in_currency") val change1h: Double? = null,
    @SerialName("price_change_percentage_7d_in_currency") val change7d: Double? = null,
)

@Serializable
data class SparklineDto(val price: List<Double?> = emptyList())

@Serializable
data class GlobalResponseDto(val data: GlobalDataDto)

@Serializable
data class GlobalDataDto(
    @SerialName("active_cryptocurrencies") val activeCryptocurrencies: Int? = null,
    @SerialName("total_market_cap") val totalMarketCap: Map<String, Double> = emptyMap(),
    @SerialName("total_volume") val totalVolume: Map<String, Double> = emptyMap(),
    @SerialName("market_cap_percentage") val marketCapPercentage: Map<String, Double> = emptyMap(),
    @SerialName("market_cap_change_percentage_24h_usd") val marketCapChangePercentage24hUsd: Double? = null,
)

@Serializable
data class MarketChartDto(
    /** Pairs of `[unixMillis, price]`. */
    val prices: List<List<Double?>> = emptyList(),
)

@Serializable
data class CoinProfileDto(
    val id: String,
    val description: Map<String, String?> = emptyMap(),
    val links: LinksDto? = null,
    val categories: List<String?> = emptyList(),
    @SerialName("genesis_date") val genesisDate: String? = null,
    @SerialName("hashing_algorithm") val hashingAlgorithm: String? = null,
)

@Serializable
data class LinksDto(val homepage: List<String?> = emptyList())

@Serializable
data class ExchangeDto(
    val id: String,
    val name: String,
    @SerialName("year_established") val yearEstablished: Int? = null,
    val country: String? = null,
    val url: String? = null,
    val image: String? = null,
    @SerialName("trust_score") val trustScore: Int? = null,
    @SerialName("trust_score_rank") val trustScoreRank: Int? = null,
    @SerialName("trade_volume_24h_btc") val tradeVolume24hBtc: Double? = null,
)

@Serializable
data class TrendingResponseDto(val coins: List<TrendingCoinDto> = emptyList())

@Serializable
data class TrendingCoinDto(val item: TrendingItemDto)

@Serializable
data class TrendingItemDto(val id: String)

@Serializable
data class SearchResponseDto(val coins: List<SearchCoinDto> = emptyList())

@Serializable
data class SearchCoinDto(
    val id: String,
    val name: String,
    val symbol: String,
    val thumb: String? = null,
    @SerialName("market_cap_rank") val marketCapRank: Int? = null,
)
