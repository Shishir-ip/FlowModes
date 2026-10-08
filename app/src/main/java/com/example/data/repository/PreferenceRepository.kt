package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "flowmodes_prefs")

class PreferenceRepository(private val context: Context) {
    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyOledBlack = booleanPreferencesKey("oled_black")
    private val keyDeveloperMode = booleanPreferencesKey("developer_mode")
    private val keyFirstLaunch = booleanPreferencesKey("first_launch_done")

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyThemeMode] ?: "system" // "system", "dark", "light"
    }

    val oledBlack: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyOledBlack] ?: false
    }

    val isDeveloperMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyDeveloperMode] ?: false
    }

    val isFirstLaunchDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyFirstLaunch] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[keyThemeMode] = mode }
    }

    suspend fun setOledBlack(enabled: Boolean) {
        context.dataStore.edit { it[keyOledBlack] = enabled }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        context.dataStore.edit { it[keyDeveloperMode] = enabled }
    }

    suspend fun setFirstLaunchDone(done: Boolean) {
        context.dataStore.edit { it[keyFirstLaunch] = done }
    }
}
