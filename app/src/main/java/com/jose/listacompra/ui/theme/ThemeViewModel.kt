package com.jose.listacompra.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.preferences.ThemePreferences
import com.jose.listacompra.data.preferences.ThemePreferences.Companion.DEFAULT_COLOR
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val themePreferences = ThemePreferences(application)
    
    /**
     * Estado actual del tema (expuesto como StateFlow)
     */
    val themeMode: StateFlow<String> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences.DEFAULT_MODE
        )
    val primaryColor = themePreferences.primaryColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_COLOR)
    val followSystem: StateFlow<Boolean> = themePreferences.followSystem
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences.DEFAULT_FOLLOW_SYSTEM
        )
    
    /**
     * Cambia el modo del tema manualmente
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            themePreferences.saveManualTheme(mode)
        }
    }
    
    /**
     * Activa/desactiva seguir sistema
     */
    fun setFollowSystem(follow: Boolean) {
        viewModelScope.launch {
            themePreferences.saveFollowSystem(follow)
        }
    }
    
    /**
     * Alterna entre claro y oscuro (toggle rápido)
     */
    fun toggleTheme() {
        viewModelScope.launch {
            themePreferences.toggleManualTheme()
        }
    }
    
    /**
     * Cambia el color primario de la app
     */
    fun setPrimaryColor(color: Int) {
        viewModelScope.launch {
            themePreferences.setPrimaryColor(color)
        }
    }
    
    /**
     * Determina si usar tema oscuro basado en preferencias
     */
    fun isDarkTheme(systemIsDark: Boolean): Boolean {
        return if (followSystem.value) {
            systemIsDark
        } else {
            themeMode.value == "dark"
        }
    }
}