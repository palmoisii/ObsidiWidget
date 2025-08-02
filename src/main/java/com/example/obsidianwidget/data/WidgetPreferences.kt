package com.example.obsidianwidget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_settings")

class WidgetPreferences(private val context: Context) {
    
    companion object {
        private val VAULT_PATH_KEY = stringPreferencesKey("vault_path")
        private val DAILY_NOTE_PATH_KEY = stringPreferencesKey("daily_note_path")
        private val CUSTOM_SHORTCUTS_KEY = stringPreferencesKey("custom_shortcuts")
        private val json = Json { ignoreUnknownKeys = true }
    }
    
    val vaultPath: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[VAULT_PATH_KEY] ?: ""
    }
    
    val dailyNotePath: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DAILY_NOTE_PATH_KEY] ?: "Daily Notes"
    }
    
    
    val customShortcuts: Flow<List<CustomShortcut>> = context.dataStore.data.map { preferences ->
        val json = preferences[CUSTOM_SHORTCUTS_KEY] ?: ""
        if (json.isEmpty()) {
            List(2) { CustomShortcut() }
        } else {
            try {
                Json.decodeFromString<List<CustomShortcut>>(json)
            } catch (e: Exception) {
                List(2) { CustomShortcut() }
            }
        }
    }
    
    suspend fun setVaultPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_PATH_KEY] = path
        }
    }
    
    suspend fun setDailyNotePath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_NOTE_PATH_KEY] = path
        }
    }
    
    suspend fun updateCustomShortcut(index: Int, shortcut: CustomShortcut) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[CUSTOM_SHORTCUTS_KEY] ?: ""
            val shortcuts = if (currentJson.isEmpty()) {
                MutableList(2) { CustomShortcut() }
            } else {
                try {
                    json.decodeFromString<List<CustomShortcut>>(currentJson).toMutableList()
                } catch (e: Exception) {
                    MutableList(2) { CustomShortcut() }
                }
            }
            
            if (index in 0..1) {
                shortcuts[index] = shortcut
                preferences[CUSTOM_SHORTCUTS_KEY] = Json.encodeToString(shortcuts)
            }
        }
    }
}