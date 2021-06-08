package com.thecode.cryptomania.datasource.network.api

import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse
import com.thecode.cryptomania.datasource.network.model.ExchangeObjectResponse
import com.thecode.cryptomania.datasource.network.model.MarketChartObjectResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {

    //region Coins
    @GET("coins/markets")
    fun getAllCoins(
        @Query("vs_currency") currency: String
    ): CoinObjectResponse

    @GET("/coins/{id}") //TODO L'id doit etre passé dans la requete, j'ai oublié la syntaxe
    suspend fun getCoinById(
        @Query("id") id: String
    ): CoinObjectResponse

    @GET("/coins/{id}/market_chart") //TODO L'id doit etre passé dans la requete, j'ai oublié la syntaxe
    suspend fun getMarketChart(
        @Query("id") id: String,
        @Query("vs_currency") currency: String,
        @Query("days") days: String
    ): MarketChartObjectResponse

    //endregion

    //region Exchange
    @GET("/exchanges")
    suspend fun getAllExchanges(): ExchangeObjectResponse

    @GET("/exchanges")
    suspend fun getExchangesPerPage(
        @Query("per_page") perPage: String,
        @Query("pages") page: String
    ): ExchangeObjectResponse

    //endregion
}
