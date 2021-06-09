package com.thecode.cryptomania.core.remote

import com.thecode.cryptomania.core.domain.Coin
import com.thecode.cryptomania.core.domain.Exchange
import com.thecode.cryptomania.datasource.CoinGeckoApiRemoteService
import com.thecode.cryptomania.datasource.network.mapper.CoinMapper
import javax.inject.Inject

interface AppRemoteDataSource {

    suspend fun fetchCoins(currency: String): Coin

    //suspend fun fetchCoinById(coinId: String): Coin

    //suspend fun fetchExchanges(): Exchange

    //suspend fun fetchMarketChartData(coinId: String, currency: String, days: Int)
}

class AppRemoteDataSourceImpl @Inject constructor(
    private val apiService: CoinGeckoApiRemoteService,
    private val coinMapper: CoinMapper

) : AppRemoteDataSource {

    override suspend fun fetchCoins(currency: String): Coin {
        return coinMapper.mapToDomain(apiService.getAllCoins(currency))
    }


}
