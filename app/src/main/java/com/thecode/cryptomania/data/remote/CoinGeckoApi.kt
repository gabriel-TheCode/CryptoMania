package com.thecode.cryptomania.data.remote

import com.thecode.cryptomania.data.remote.dto.CoinMarketDto
import com.thecode.cryptomania.data.remote.dto.CoinProfileDto
import com.thecode.cryptomania.data.remote.dto.ExchangeDto
import com.thecode.cryptomania.data.remote.dto.GlobalResponseDto
import com.thecode.cryptomania.data.remote.dto.MarketChartDto
import com.thecode.cryptomania.data.remote.dto.SearchResponseDto
import com.thecode.cryptomania.data.remote.dto.TrendingResponseDto
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** CoinGecko v3 endpoints available on the free Demo/keyless tier. */
interface CoinGeckoApi {

    /**
     * One call returns up to 250 coins including a 7-day sparkline and 1h/24h/7d changes,
     * which feeds the market list, movers, local search and coin headers at once.
     */
    @GET("coins/markets")
    suspend fun markets(
        @Query("vs_currency") currency: String,
        @Query("ids") ids: String? = null,
        @Query("per_page") perPage: Int = 250,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChangePercentage: String = "1h,24h,7d",
    ): List<CoinMarketDto>

    @GET("global")
    suspend fun global(): GlobalResponseDto

    @GET("coins/{id}/market_chart")
    suspend fun marketChart(
        @Path("id") coinId: String,
        @Query("vs_currency") currency: String,
        @Query("days") days: Int,
    ): MarketChartDto

    /** Metadata only: every heavy optional section is switched off to keep the payload small. */
    @GET("coins/{id}")
    suspend fun coinProfile(
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = false,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
        @Query("sparkline") sparkline: Boolean = false,
    ): CoinProfileDto

    @GET("exchanges")
    suspend fun exchanges(@Query("per_page") perPage: Int = 100): List<ExchangeDto>

    /** Coins trending in CoinGecko searches over the last 24 hours ("Hot"). */
    @GET("search/trending")
    suspend fun trending(): TrendingResponseDto

    @GET("search")
    suspend fun search(@Query("query") query: String): SearchResponseDto

    companion object {
        const val BASE_URL = "https://api.coingecko.com/api/v3/"
        const val API_KEY_HEADER = "x-cg-demo-api-key"

        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        fun create(client: OkHttpClient, baseUrl: String = BASE_URL): CoinGeckoApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CoinGeckoApi::class.java)
    }
}
