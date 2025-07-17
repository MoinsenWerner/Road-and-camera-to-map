package com.example.preciseroadmap.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private val syncKey = booleanPreferencesKey("sync_enabled")
    private val wifiOnlyKey = booleanPreferencesKey("wifi_only")
    private val themeKey = stringPreferencesKey("theme")
    private val detectionModeKey = stringPreferencesKey("detection_mode")

    val syncEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[syncKey] ?: true }
    val wifiOnly: Flow<Boolean> = context.settingsDataStore.data.map { it[wifiOnlyKey] ?: false }
    val theme: Flow<String> = context.settingsDataStore.data.map { it[themeKey] ?: "system" }
    val detectionMode: Flow<String> = context.settingsDataStore.data.map { it[detectionModeKey] ?: "camera" }

    suspend fun setSyncEnabled(value: Boolean) =
        context.settingsDataStore.edit { it[syncKey] = value }

    suspend fun setWifiOnly(value: Boolean) =
        context.settingsDataStore.edit { it[wifiOnlyKey] = value }

    suspend fun setTheme(value: String) =
        context.settingsDataStore.edit { it[themeKey] = value }

    suspend fun setDetectionMode(value: String) =
        context.settingsDataStore.edit { it[detectionModeKey] = value }
}