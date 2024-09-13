package com.thecode.cryptomania.core.remote

import com.thecode.cryptomania.core.domain.CoinDomainModel
import com.thecode.cryptomania.core.domain.ExchangeDomainModel

interface AppRemoteDataSource {

    suspend fun fetchCoins(currency: String): CoinDomainModel

    suspend fun fetchCoinById(coinId: String): CoinDomainModel

    suspend fun fetchExchanges(): ExchangeDomainModel

    suspend fun fetchMarketChartData(
        coinId: String,
        currency: String,
        days: Int
    ): List<List<Number>>
}