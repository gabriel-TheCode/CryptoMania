package com.thecode.cryptomania.datasource.network.model

import com.google.gson.annotations.SerializedName

class ExchangeObjectResponse (

        @SerializedName("id")
        val id: String,

        @SerializedName("name")
        val name: String,

        @SerializedName("year_established")
        val year_established: Int,

        @SerializedName("country")
        val country: String,

        @SerializedName("description")
        val description: String,

        @SerializedName("url")
        val url: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("has_trading_incentive")
        val has_trading_incentive: Boolean,

        @SerializedName("trust_score")
        val trust_score: Int,

        @SerializedName("trust_score_rank")
        val trust_score_rank: Int,

        @SerializedName("trade_volume_24h_btc")
        val trade_volume_24h_btc: Float,

        @SerializedName("trade_volume_24h_btc_normalized")
        val trade_volume_24h_btc_normalized: Float
    )