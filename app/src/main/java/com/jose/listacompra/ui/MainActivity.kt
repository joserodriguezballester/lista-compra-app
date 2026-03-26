package com.jose.listacompra.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jose.listacompra.ui.navigation.AppNavigation
import com.jose.listacompra.ui.screens.ColorSettingsDialog
import com.jose.listacompra.ui.theme.ListaCompraTheme
import com.jose.listacompra.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen {
    object Splash : Screen()
    object Main : Screen()
    object Lists : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val themeViewModel: ThemeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Observar tema y color desde el ViewModel
            val themeMode by themeViewModel.themeMode.collectAsState()
            val primaryColor by themeViewModel.primaryColor.collectAsState()
            
            // Estado para el diálogo de color
            var showColorDialog by remember { mutableStateOf(false) }

            val navController = rememberNavController()
            
            // Aplicamos el tema usando los valores del ViewModel
            ListaCompraTheme(
                themeMode = themeMode,
                primaryColorInt = primaryColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // La estructura principal de la App
                    Scaffold { paddingValues ->
                        // Todo el flujo de pantallas se gestiona aquí
                        AppNavigation(
                            navController = navController,
                            padding = paddingValues,
                            isDarkMode = themeMode == "dark",
                            onToggleTheme = { themeViewModel.toggleTheme() },
                            onChangeColor = { showColorDialog = true }
                        )
                    }
                    
                    // Diálogo de color (global, accesible desde cualquier pantalla)
                    if (showColorDialog) {
                        ColorSettingsDialog(
                            currentColor = primaryColor,
                            onDismiss = { showColorDialog = false },
                            onColorSelected = { color ->
                                themeViewModel.setPrimaryColor(color)
                            }
                        )
                    }
                }
            }
        }
    }
}
