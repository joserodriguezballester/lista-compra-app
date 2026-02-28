package com.jose.listacompra.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para el DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceManager(private val context: Context) {
    
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val FOLLOW_SYSTEM_KEY = booleanPreferencesKey("follow_system_theme")
    }
    
    /**
     * Obtiene el flujo de preferencia de modo oscuro
     * null = seguir sistema, true = oscuro, false = claro
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val followSystem = preferences[FOLLOW_SYSTEM_KEY] ?: true
            if (followSystem) {
                ThemeMode.SYSTEM
            } else {
                val isDark = preferences[DARK_MODE_KEY] ?: false
                if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
            }
        }
    
    /**
     * Establece el modo de tema
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            when (mode) {
                ThemeMode.SYSTEM -> {
                    preferences[FOLLOW_SYSTEM_KEY] = true
                }
                ThemeMode.DARK -> {
                    preferences[FOLLOW_SYSTEM_KEY] = false
                    preferences[DARK_MODE_KEY] = true
                }
                ThemeMode.LIGHT -> {
                    preferences[FOLLOW_SYSTEM_KEY] = false
                    preferences[DARK_MODE_KEY] = false
                }
            }
        }
    }
}

enum class ThemeMode {
    SYSTEM,   // Seguir configuración del sistema
    LIGHT,    // Forzar modo claro
    DARK      // Forzar modo oscuro
}