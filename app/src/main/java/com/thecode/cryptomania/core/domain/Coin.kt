package com.thecode.cryptomania.core.domain


data class Coin(
    val coins: List<CoinItem>
)

data class CoinItem(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Float,
    val market_cap: Float,
    val market_cap_rank: Int,
    val fully_diluted_valuation: Float?,
    val total_volume: Float,
    val high_24h: Float,
    val low_24h: Float,
    val price_change_24h: Float,
    val price_change_percentage_24h: Float,
    val market_cap_change_24h: Float,
    val market_cap_change_percentage_24h: Float,
)