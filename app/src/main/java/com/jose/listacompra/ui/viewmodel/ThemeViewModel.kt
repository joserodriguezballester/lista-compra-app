package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(private val themePreferences: ThemePreferences) : ViewModel() {
    
    private val _themeMode = MutableStateFlow("light")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    
    private val _followSystem = MutableStateFlow(false)
    val followSystem: StateFlow<Boolean> = _followSystem.asStateFlow()
    
    init {
        viewModelScope.launch {
            themePreferences.themeMode.collect { mode ->
                _themeMode.value = mode
            }
        }
        viewModelScope.launch {
            themePreferences.followSystem.collect { follow ->
                _followSystem.value = follow
            }
        }
    }
    
    fun toggleTheme() {
        viewModelScope.launch {
            val newMode = themePreferences.toggleManualTheme()
            _themeMode.value = newMode
        }
    }
    
    fun setFollowSystem(follow: Boolean) {
        viewModelScope.launch {
            themePreferences.saveFollowSystem(follow)
            _followSystem.value = follow
        }
    }
    
    fun isDarkTheme(systemIsDark: Boolean): Boolean {
        return if (_followSystem.value) {
            systemIsDark
        } else {
            _themeMode.value == "dark"
        }
    }
    
    fun getToolbarIcon(): String {
        return if (_themeMode.value == "light") "🌙" else "☀️"
    }
}
