package com.thecode.cryptomania.core.repositories

import com.thecode.cryptomania.core.domain.CoinDomainModel
import com.thecode.cryptomania.core.remote.AppRemoteDataSourceImpl
import com.thecode.cryptomania.datasource.network.mapper.CoinMapper
import javax.inject.Inject

class CoinsRepository @Inject constructor(
    private val networkDataSource: AppRemoteDataSourceImpl,
    private val coinMapper: CoinMapper
) {

    suspend fun fetchCoins(currency: String): CoinDomainModel {
        return networkDataSource.fetchCoins(currency)
    }

    suspend fun fetchCoinById(id: String): CoinDomainModel {
        return networkDataSource.fetchCoinById(id)
    }
}