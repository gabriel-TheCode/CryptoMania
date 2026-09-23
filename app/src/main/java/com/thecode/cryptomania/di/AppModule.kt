package com.thecode.cryptomania.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.thecode.cryptomania.BuildConfig
import com.thecode.cryptomania.data.local.CryptoManiaDatabase
import com.thecode.cryptomania.data.network.ConnectivityNetworkMonitor
import com.thecode.cryptomania.data.preferences.DataStoreSettingsRepository
import com.thecode.cryptomania.data.remote.CoinGeckoApi
import com.thecode.cryptomania.data.repository.OfflineFirstCoinDetailsRepository
import com.thecode.cryptomania.data.repository.OfflineFirstExchangeRepository
import com.thecode.cryptomania.data.repository.OfflineFirstMarketRepository
import com.thecode.cryptomania.data.repository.RoomCacheRepository
import com.thecode.cryptomania.data.repository.RoomWatchlistRepository
import com.thecode.cryptomania.domain.repository.CacheRepository
import com.thecode.cryptomania.domain.repository.CoinDetailsRepository
import com.thecode.cryptomania.domain.repository.ExchangeRepository
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.NetworkMonitor
import com.thecode.cryptomania.domain.repository.SettingsRepository
import com.thecode.cryptomania.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/** Dispatcher for CPU-bound mapping work; injected so tests can run it on virtual time. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val apiKey = BuildConfig.COINGECKO_API_KEY
            val request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .apply { if (apiKey.isNotBlank()) header(CoinGeckoApi.API_KEY_HEADER, apiKey) }
                .build()
            chain.proceed(request)
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader(CoinGeckoApi.API_KEY_HEADER)
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideCoinGeckoApi(client: OkHttpClient): CoinGeckoApi = CoinGeckoApi.create(client)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CryptoManiaDatabase =
        Room.databaseBuilder(context, CryptoManiaDatabase::class.java, CryptoManiaDatabase.NAME)
            // The database is a cache plus a watchlist; a schema bump rebuilds it rather than
            // shipping migrations for data that can be re-downloaded.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideCoinDao(db: CryptoManiaDatabase) = db.coinDao()
    @Provides fun provideGlobalMarketDao(db: CryptoManiaDatabase) = db.globalMarketDao()
    @Provides fun providePriceHistoryDao(db: CryptoManiaDatabase) = db.priceHistoryDao()
    @Provides fun provideCoinProfileDao(db: CryptoManiaDatabase) = db.coinProfileDao()
    @Provides fun provideExchangeDao(db: CryptoManiaDatabase) = db.exchangeDao()
    @Provides fun provideWatchlistDao(db: CryptoManiaDatabase) = db.watchlistDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun marketRepository(impl: OfflineFirstMarketRepository): MarketRepository
    @Binds abstract fun coinDetailsRepository(impl: OfflineFirstCoinDetailsRepository): CoinDetailsRepository
    @Binds abstract fun exchangeRepository(impl: OfflineFirstExchangeRepository): ExchangeRepository
    @Binds abstract fun watchlistRepository(impl: RoomWatchlistRepository): WatchlistRepository
    @Binds abstract fun cacheRepository(impl: RoomCacheRepository): CacheRepository
    @Binds abstract fun settingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
    @Binds abstract fun networkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
