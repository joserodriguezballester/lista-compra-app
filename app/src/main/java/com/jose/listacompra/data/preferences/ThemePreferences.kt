package com.jose.listacompra.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    
    companion object {
        val PRIMARY_COLOR_KEY = intPreferencesKey("primary_color")
        // Color por defecto (verde #4CAF50)
        const val DEFAULT_COLOR = 0xFF4CAF50.toInt()
        
        // Colores predefinidos
        val PREDEFINED_COLORS = listOf(
            0xFF4CAF50.toInt(), // Verde (default)
            0xFF2196F3.toInt(), // Azul
            0xFFF44336.toInt(), // Rojo
            0xFFFF9800.toInt(), // Naranja
            0xFF9C27B0.toInt(), // Morado
        )
        
        val COLOR_NAMES = mapOf(
            0xFF4CAF50.toInt() to "Verde",
            0xFF2196F3.toInt() to "Azul",
            0xFFF44336.toInt() to "Rojo",
            0xFFFF9800.toInt() to "Naranja",
            0xFF9C27B0.toInt() to "Morado",
        )
    }
    
    val primaryColor: Flow<Int> = context.themeDataStore.data.map { preferences ->
        preferences[PRIMARY_COLOR_KEY] ?: DEFAULT_COLOR
    }
    
    suspend fun setPrimaryColor(color: Int) {
        context.themeDataStore.edit { preferences ->
            preferences[PRIMARY_COLOR_KEY] = color
        }
    }
}
