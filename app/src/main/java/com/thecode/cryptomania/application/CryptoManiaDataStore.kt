package com.thecode.cryptomania.application

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.thecode.cryptomania.utils.AppConstants.PREFERENCE_NAME
import com.thecode.cryptomania.utils.extensions.getValueFlow
import com.thecode.cryptomania.utils.extensions.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCE_NAME)

class CryptoManiaDataStore @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val IS_ONBOARDING_COMPLETED = "IS_ONBOARDING_COMPLETED"
    }

    private val dataStore = context.dataStore

    suspend fun setOnboardingCompleted() {
        val dataStoreKey = booleanPreferencesKey(IS_ONBOARDING_COMPLETED)
        dataStore.setValue(dataStoreKey, true)
    }

    fun isOnboardingCompleted(): Flow<Boolean> {
        val dataStoreKey = booleanPreferencesKey(IS_ONBOARDING_COMPLETED)
        return dataStore.getValueFlow(dataStoreKey, false)
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
