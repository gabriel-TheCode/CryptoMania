package com.thecode.cryptomania.datasource.network.model

import com.google.gson.annotations.SerializedName

class MarketChartObjectResponse(
    @SerializedName("prices")
    val prices: List<List<Float>>,
)


