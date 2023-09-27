package com.thecode.cryptomania.core.repositories

import com.thecode.cryptomania.core.domain.ExchangeDomainModel
import com.thecode.cryptomania.core.remote.AppRemoteDataSourceImpl
import com.thecode.cryptomania.datasource.network.mapper.ExchangeMapper
import javax.inject.Inject

class ExchangeRepository @Inject constructor(
    private val networkDataSource: AppRemoteDataSourceImpl,
    private val exchange: ExchangeMapper
) {

    suspend fun fetchExchanges(): ExchangeDomainModel {
        return networkDataSource.fetchExchanges()
    }

}