package com.jose.listacompra.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.jose.listacompra.data.preferences.ThemePreferences
import com.jose.listacompra.ui.screens.MainScreen
import com.jose.listacompra.ui.screens.SplashScreen
import com.jose.listacompra.ui.theme.ListaCompraTheme
import com.jose.listacompra.ui.theme.ThemeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val themeViewModel: ThemeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferences = ThemePreferences(this)
        
        setContent {
            // Estado para el color primario (null mientras carga)
            var primaryColor by remember { mutableStateOf<Int?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            // Observar el modo de tema desde el ViewModel
            val themeMode by themeViewModel.themeMode.collectAsState()
            
            // Cargar el color guardado al iniciar
            LaunchedEffect(Unit) {
                primaryColor = themePreferences.primaryColor.first()
                isLoading = false
            }
            
            // Función para actualizar el color
            val onColorChanged: (Int) -> Unit = { newColor ->
                primaryColor = newColor
                lifecycleScope.launch {
                    themePreferences.setPrimaryColor(newColor)
                }
            }
            
            // Mostrar pantalla de carga mientras se lee la preferencia
            if (isLoading) {
                // Pantalla de carga simple
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Podríamos mostrar un spinner aquí, pero por ahora dejamos el fondo
                }
            } else {
                ListaCompraTheme(
                    themeMode = themeMode,
                    primaryColorInt = primaryColor
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var showSplash by remember { mutableStateOf(true) }
                        
                        if (showSplash) {
                            SplashScreen(
                                onSplashFinished = {
                                    showSplash = false
                                }
                            )
                        } else {
                            MainScreen(
                                currentPrimaryColor = primaryColor ?: ThemePreferences.DEFAULT_COLOR,
                                onColorChanged = onColorChanged,
                                themeMode = themeMode,
                                onThemeModeChange = { themeViewModel.setThemeMode(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}