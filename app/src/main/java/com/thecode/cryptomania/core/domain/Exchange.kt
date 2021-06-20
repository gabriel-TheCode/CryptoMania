package com.thecode.cryptomania.core.domain


data class Exchange(
        val exchanges: List<ExchangeItem>
)

data class ExchangeItem(
        val id: String,
        val name: String,
        var year_established: Int? = null,
        var country: String? = null,
        var description: String? = null,
        val url: String,
        val image: String,
        val has_trading_incentive: Boolean? = null,
        val trust_score: Int,
        val trust_score_rank: Int,
        val trade_volume_24h_btc: Float,
        val trade_volume_24h_btc_normalized: Float
)