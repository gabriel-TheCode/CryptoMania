package com.thecode.cryptomania.core.repositories

import com.thecode.cryptomania.core.remote.AppRemoteDataSourceImpl
import com.thecode.cryptomania.datasource.network.mapper.MarketChartMapper
import javax.inject.Inject

class MarketChartRepository @Inject constructor(
    private val networkDataSource: AppRemoteDataSourceImpl,
    private val marketChartMapper: MarketChartMapper
) {

    suspend fun fetchMarketChartData(
        coinId: String,
        currency: String,
        days: Int
    ): List<List<Number>> {
        return networkDataSource.fetchMarketChartData(coinId, currency, days)
    }

}