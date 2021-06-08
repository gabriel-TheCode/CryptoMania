package com.thecode.cryptomania.core.domain


data class Exchange(
    val articles: List<ExchangeItem>
)

data class ExchangeItem (
    val id: String,
    val name: String,
    val year_established: Int,
    val country: String,
    val description: String,
    val url: String,
    val image: String,
    val has_trading_incentive: Boolean,
    val trust_score: Int,
    val trust_score_rank: Int,
    val trade_volume_24h_btc: Float,
    val trade_volume_24h_btc_normalized: Float
)