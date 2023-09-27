package com.thecode.cryptomania.core.domain


data class ExchangeDomainModel(
    val exchanges: List<ExchangeItemDomainModel>
)

data class ExchangeItemDomainModel(
    val id: String,
    val name: String,
    var yearEstablished: Int? = null,
    var country: String? = null,
    var description: String? = null,
    val url: String,
    val image: String,
    val hasTradingIncentive: Boolean? = null,
    val trustScore: Int,
    val trustScoreRank: Int,
    val tradeVolume24hBtc: Float,
    val tradeVolume24hBtcNormalized: Float
)