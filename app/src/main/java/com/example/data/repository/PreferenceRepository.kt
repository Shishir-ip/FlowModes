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
    private val keyFlipToShhh = booleanPreferencesKey("flip_to_shhh_enabled")
    private val keyShakeTrigger = booleanPreferencesKey("shake_trigger_enabled")
    private val keyCalendarTrigger = booleanPreferencesKey("calendar_trigger_enabled")

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

    val isFlipToShhhEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyFlipToShhh] ?: false
    }

    val isShakeTriggerEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyShakeTrigger] ?: false
    }

    val isCalendarTriggerEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyCalendarTrigger] ?: false
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

    suspend fun setFlipToShhhEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyFlipToShhh] = enabled }
    }

    suspend fun setShakeTriggerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyShakeTrigger] = enabled }
    }

    suspend fun setCalendarTriggerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyCalendarTrigger] = enabled }
    }
}
