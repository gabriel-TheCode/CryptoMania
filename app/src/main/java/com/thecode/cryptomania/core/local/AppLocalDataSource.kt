package com.thecode.cryptomania.core.local

import kotlinx.coroutines.flow.Flow

interface AppLocalDataSource {

    fun isOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted()

    suspend fun clearAppData()
}

