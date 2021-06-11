package com.thecode.cryptomania.datasource.network.model

import com.google.gson.annotations.SerializedName

class MarketChartObjectResponse(
        @SerializedName("prices")
        val prices: List<List<MarketChart>>,
) {
    data class MarketChart(
            val timestamp: Long,

            val price: Float
    )
}

