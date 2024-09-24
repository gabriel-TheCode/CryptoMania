package com.thecode.cryptomania.datasource.network.model

import com.google.gson.annotations.SerializedName


class CoinObjectResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("current_price")
    val currentPrice: Float,

    @SerializedName("market_cap")
    val marketCap: Float,

    @SerializedName("market_cap_rank")
    val marketCapRank: Int,

    @SerializedName("fully_diluted_valuation")
    var fullyDilutedValuation: Float? = null,

    @SerializedName("total_volume")
    val totalVolume: Float,

    @SerializedName("high_24h")
    val high24h: Float,

    @SerializedName("low_24h")
    val low24h: Float,

    @SerializedName("price_change_24h")
    val priceChange24h: Float,

    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Float,

    @SerializedName("market_cap_change_24h")
    val marketCapChange24h: Float,

    @SerializedName("market_cap_change_percentage_24h")
    val marketCapChangePercentage24h: Float,

    @SerializedName("ath")
    val ath: Float,

    @SerializedName("max_supply")
    val maxSupply: Float,

    )
