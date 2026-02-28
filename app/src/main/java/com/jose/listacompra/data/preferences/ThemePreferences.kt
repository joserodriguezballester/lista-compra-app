package com.jose.listacompra.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    
    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode") // "light" o "dark"
        val FOLLOW_SYSTEM_KEY = booleanPreferencesKey("follow_system")
        
        const val DEFAULT_MODE = "light"
        const val DEFAULT_FOLLOW_SYSTEM = false
    }
    
    val themeMode: Flow<String> = context.themeDataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: DEFAULT_MODE
    }
    
    val followSystem: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[FOLLOW_SYSTEM_KEY] ?: DEFAULT_FOLLOW_SYSTEM
    }
    
    suspend fun saveManualTheme(mode: String) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
    
    suspend fun saveFollowSystem(follow: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[FOLLOW_SYSTEM_KEY] = follow
        }
    }
    
    suspend fun toggleManualTheme(): String {
        var newMode = "light"
        context.themeDataStore.edit { preferences ->
            val current = preferences[THEME_MODE_KEY] ?: DEFAULT_MODE
            newMode = if (current == "light") "dark" else "light"
            preferences[THEME_MODE_KEY] = newMode
        }
        return newMode
    }
}
