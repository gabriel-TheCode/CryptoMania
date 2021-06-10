package com.thecode.cryptomania.core.remote

import com.thecode.cryptomania.core.domain.Coin
import com.thecode.cryptomania.core.domain.Exchange
import com.thecode.cryptomania.datasource.CoinGeckoApiRemoteService
import com.thecode.cryptomania.datasource.network.mapper.CoinMapper
import com.thecode.cryptomania.datasource.network.mapper.ExchangeMapper
import javax.inject.Inject

interface AppRemoteDataSource {

    suspend fun fetchCoins(currency: String): Coin

    suspend fun fetchCoinById(coinId: String): Coin

    suspend fun fetchExchanges(): Exchange

    suspend fun fetchMarketChartData(coinId: String, currency: String, days: Int)
}

class AppRemoteDataSourceImpl @Inject constructor(
    private val apiService: CoinGeckoApiRemoteService,
    private val coinMapper: CoinMapper,
    private val exchangeMapper: ExchangeMapper

) : AppRemoteDataSource {

    override suspend fun fetchCoins(currency: String): Coin {
        return coinMapper.mapToDomain(apiService.getAllCoins(currency))
    }

    override suspend fun fetchCoinById(coinId: String): Coin {
        TODO("Not yet implemented")
    }

    override suspend fun fetchExchanges(): Exchange {
        return exchangeMapper.mapToDomain(apiService.getAllExchanges())
    }

    override suspend fun fetchMarketChartData(coinId: String, currency: String, days: Int) {
        TODO("Not yet implemented")
    }


}
