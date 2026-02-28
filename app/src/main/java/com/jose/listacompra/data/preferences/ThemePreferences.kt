package com.jose.listacompra.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    
    companion object {
        // Para tema oscuro/claro
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode") // "light" o "dark"
        val FOLLOW_SYSTEM_KEY = booleanPreferencesKey("follow_system")
        
        // Para color primario personalizable
        val PRIMARY_COLOR_KEY = intPreferencesKey("primary_color")
        
        // Valores por defecto
        const val DEFAULT_MODE = "light"
        const val DEFAULT_FOLLOW_SYSTEM = false
        const val DEFAULT_COLOR = 0xFF4CAF50.toInt() // Verde
        
        // Colores predefinidos
        val PREDEFINED_COLORS = listOf(
            0xFF4CAF50.toInt(), // Verde (default)
            0xFF2196F3.toInt(), // Azul
            0xFFF44336.toInt(), // Rojo
            0xFFFF9800.toInt(), // Naranja
            0xFF9C27B0.toInt(), // Morado
        )
    }
    
    // ========== TEMA OSCURO/CLARO ==========
    
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
    
    // ========== COLOR PRIMARIO ==========
    
    val primaryColor: Flow<Int> = context.themeDataStore.data.map { preferences ->
        preferences[PRIMARY_COLOR_KEY] ?: DEFAULT_COLOR
    }
    
    suspend fun setPrimaryColor(color: Int) {
        context.themeDataStore.edit { preferences ->
            preferences[PRIMARY_COLOR_KEY] = color
        }
    }
}
