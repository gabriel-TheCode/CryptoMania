package com.thecode.cryptomania.datasource

import com.thecode.cryptomania.datasource.network.api.CoinGeckoApi
import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse
import com.thecode.cryptomania.datasource.network.model.ExchangeObjectResponse
import com.thecode.cryptomania.datasource.network.model.MarketChartObjectResponse

interface CoinGeckoApiRemoteService {
    //region COIN
    suspend fun getAllCoins(currency: String): List<CoinObjectResponse>

    suspend fun getCoinById(id: String): CoinObjectResponse

    suspend fun getMarketChart(
        coinId: String,
        currency: String,
        days: Int
    ): MarketChartObjectResponse

    //endregion

    //region EXCHANGE

    suspend fun getAllExchanges(): List<ExchangeObjectResponse>

    //endregion
}

class CoinGeckoApiRemoteServiceImpl constructor(
    private val coinGeckoApi: CoinGeckoApi
) : CoinGeckoApiRemoteService {

    override suspend fun getAllCoins(
        currency: String
    ): List<CoinObjectResponse> {
        return coinGeckoApi.getAllCoins(currency)
    }

    override suspend fun getCoinById(id: String): CoinObjectResponse {
        return coinGeckoApi.getCoinById(id)
    }

    override suspend fun getMarketChart(
        coinId: String,
        currency: String,
        days: Int
    ): MarketChartObjectResponse {
        return coinGeckoApi.getMarketChart(coinId, currency, days)
    }

    override suspend fun getAllExchanges(): List<ExchangeObjectResponse> {
        return coinGeckoApi.getAllExchanges()
    }
}
