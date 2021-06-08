package com.thecode.cryptomania.core.local

import com.thecode.cryptomania.application.CryptoManiaDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AppLocalDataSource {

    fun isOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted()

    fun isNightModeEnabled(): Flow<Boolean>

    suspend fun setNightModeEnabled(state: Boolean)

    fun getUserLanguagePreference(): Flow<String>

    suspend fun setUserLanguagePreference(lang: String)

    suspend fun clearAppData()
}

class AppLocalDataSourceImpl @Inject constructor(
    private val dataStore: CryptoManiaDataStore
) : AppLocalDataSource {
    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.setOnboardingCompleted()
    }

    override fun isNightModeEnabled(): Flow<Boolean> {
        return dataStore.isNightModeEnabled()
    }

    override suspend fun setNightModeEnabled(state: Boolean) {
        dataStore.setNightModeEnabled(state)
    }

    override fun getUserLanguagePreference(): Flow<String> {
        return dataStore.getUserLanguagePreference()
    }

    override suspend fun setUserLanguagePreference(lang: String) {
        dataStore.setUserLanguagePreference(lang)
    }

    override suspend fun clearAppData() {
        dataStore.clearSession()
    }
}
