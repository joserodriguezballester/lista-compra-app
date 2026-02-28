package com.jose.listacompra.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferenceManager = ThemePreferenceManager(application)
    
    /**
     * Estado actual del tema (expuesto como StateFlow)
     */
    val themeMode: StateFlow<ThemeMode> = preferenceManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )
    
    /**
     * Cambia el modo del tema
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferenceManager.setThemeMode(mode)
        }
    }
    
    /**
     * Alterna entre claro y oscuro (útil para toggle rápido)
     */
    fun toggleTheme() {
        viewModelScope.launch {
            val current = themeMode.value
            val newMode = when (current) {
                ThemeMode.DARK -> ThemeMode.LIGHT
                else -> ThemeMode.DARK
            }
            preferenceManager.setThemeMode(newMode)
        }
    }
}