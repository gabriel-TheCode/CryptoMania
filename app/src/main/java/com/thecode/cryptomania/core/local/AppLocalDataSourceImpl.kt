package com.thecode.cryptomania.core.local

import com.thecode.cryptomania.application.CryptoManiaDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppLocalDataSourceImpl @Inject constructor(
    private val dataStore: CryptoManiaDataStore
) : AppLocalDataSource {
    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.setOnboardingCompleted()
    }

    override suspend fun clearAppData() {
        dataStore.clearSession()
    }
}
