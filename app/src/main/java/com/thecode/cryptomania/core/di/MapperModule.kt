package com.thecode.cryptomania.core.di

import com.thecode.cryptomania.core.domain.Coin
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.core.domain.Exchange
import com.thecode.cryptomania.core.domain.ExchangeItem
import com.thecode.cryptomania.datasource.network.mapper.*
import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse
import com.thecode.cryptomania.datasource.network.model.ExchangeObjectResponse
import com.thecode.cryptomania.datasource.network.model.MarketChartObjectResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Singleton
    @Provides
    fun provideCoinResponseMapper(): EntityMapper<CoinObjectResponse, CoinItem> {
        return CoinMapper()
    }

    @Singleton
    @Provides
    fun provideExchangeResponseMapper(): EntityMapper<ExchangeObjectResponse, ExchangeItem> {
        return ExchangeMapper()
    }

    @Singleton
    @Provides
    fun provideMarketChartResponseMapper(): EntityMapper<MarketChartObjectResponse, List<List<Number>>> {
        return MarketChartMapper()
    }
}
