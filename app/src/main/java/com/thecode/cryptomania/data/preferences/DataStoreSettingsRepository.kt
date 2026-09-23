package com.thecode.cryptomania.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thecode.cryptomania.domain.model.ThemePreference
import com.thecode.cryptomania.domain.model.UserSettings
import com.thecode.cryptomania.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** User preferences only. Market data lives in Room. */
@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val preferences: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    override val settings: Flow<UserSettings> = preferences.map { prefs ->
        UserSettings(
            theme = prefs[THEME]?.let { name -> ThemePreference.entries.firstOrNull { it.name == name } }
                ?: ThemePreference.System,
            colorBlindFriendly = prefs[COLOR_BLIND] ?: false,
        )
    }.distinctUntilChanged()

    override val recentSearches: Flow<List<String>> = preferences.map { prefs ->
        prefs[RECENT_SEARCHES]?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
    }.distinctUntilChanged()

    override suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { it[THEME] = theme.name }
    }

    override suspend fun setColorBlindFriendly(enabled: Boolean) {
        dataStore.edit { it[COLOR_BLIND] = enabled }
    }

    override suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim().replace(SEPARATOR, ' ')
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs ->
            val current = prefs[RECENT_SEARCHES]?.split(SEPARATOR).orEmpty()
            val updated = (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) })
                .filter { it.isNotBlank() }
                .take(MAX_RECENT_SEARCHES)
            prefs[RECENT_SEARCHES] = updated.joinToString(SEPARATOR.toString())
        }
    }

    override suspend fun clearRecentSearches() {
        dataStore.edit { it.remove(RECENT_SEARCHES) }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val COLOR_BLIND = booleanPreferencesKey("color_blind_friendly")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        const val SEPARATOR = '\n'
        const val MAX_RECENT_SEARCHES = 8
    }
}
