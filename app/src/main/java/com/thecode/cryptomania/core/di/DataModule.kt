package com.thecode.cryptomania.core.di


import com.thecode.cryptomania.core.local.AppLocalDataSource
import com.thecode.cryptomania.core.local.AppLocalDataSourceImpl
import com.thecode.cryptomania.application.CryptoManiaDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object DataModule {
    @Singleton
    @Provides
    fun provideInfotifyDataStore(dataStore: CryptoManiaDataStore): AppLocalDataSource {
        return AppLocalDataSourceImpl(dataStore)
    }
}
