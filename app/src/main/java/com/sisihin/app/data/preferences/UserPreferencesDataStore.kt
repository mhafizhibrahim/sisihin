package com.sisihin.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sisihin.app.domain.model.AppThemeMode
import com.sisihin.app.domain.model.SortDirection
import com.sisihin.app.domain.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sisihin_prefs")

class UserPreferencesDataStore(private val context: Context) {
    private val sortOptionKey = stringPreferencesKey("sort_criteria")
    private val sortDirectionKey = stringPreferencesKey("sort_direction")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")
    private val lockDelayKey = intPreferencesKey("lock_delay_seconds")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val sortOption: Flow<SortOption> = context.dataStore.data.map {
        it[sortOptionKey]?.let { value -> runCatching { SortOption.valueOf(value) }.getOrNull() } ?: SortOption.TARGET
    }
    val sortDirection: Flow<SortDirection> = context.dataStore.data.map {
        it[sortDirectionKey]?.let { value -> runCatching { SortDirection.valueOf(value) }.getOrNull() } ?: SortDirection.ASC
    }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[biometricEnabledKey] ?: false }
    val lockDelaySeconds: Flow<Int> = context.dataStore.data.map { it[lockDelayKey] ?: 0 }
    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map {
        it[themeModeKey]?.let { value -> runCatching { AppThemeMode.valueOf(value) }.getOrNull() } ?: AppThemeMode.SYSTEM
    }

    suspend fun setSortOption(v: SortOption) { context.dataStore.edit { it[sortOptionKey] = v.name } }
    suspend fun setSortDirection(v: SortDirection) { context.dataStore.edit { it[sortDirectionKey] = v.name } }
    suspend fun setBiometricEnabled(v: Boolean) { context.dataStore.edit { it[biometricEnabledKey] = v } }
    suspend fun setLockDelaySeconds(v: Int) { context.dataStore.edit { it[lockDelayKey] = v } }
    suspend fun setThemeMode(v: AppThemeMode) { context.dataStore.edit { it[themeModeKey] = v.name } }
}
