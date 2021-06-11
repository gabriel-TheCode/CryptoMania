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
    val current_price: Float,

    @SerializedName("market_cap")
    val market_cap: Float,

    @SerializedName("market_cap_rank")
    val market_cap_rank: Int,

    @SerializedName("fully_diluted_valuation")
    var fully_diluted_valuation: Float? = null,

    @SerializedName("total_volume")
    val total_volume: Float,

    @SerializedName("high_24h")
    val high_24h: Float,

    @SerializedName("low_24h")
    val low_24h: Float,

    @SerializedName("price_change_24h")
    val price_change_24h: Float,

    @SerializedName("price_change_percentage_24h")
    val price_change_percentage_24h: Float,

    @SerializedName("market_cap_change_24h")
    val market_cap_change_24h: Float,

    @SerializedName("market_cap_change_percentage_24h")
    val market_cap_change_percentage_24h: Float,

    @SerializedName("ath")
    val ath: Float,

    @SerializedName("max_supply")
    val max_supply: Float,

    )
