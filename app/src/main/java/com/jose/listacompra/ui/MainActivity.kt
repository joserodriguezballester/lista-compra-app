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
import com.jose.listacompra.ui.screens.ListsScreen
import com.jose.listacompra.ui.screens.MainScreen
import com.jose.listacompra.ui.screens.SplashScreen
import com.jose.listacompra.ui.theme.ListaCompraTheme
import com.jose.listacompra.ui.theme.ThemeViewModel
import com.jose.listacompra.ui.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

sealed class Screen {
    object Splash : Screen()
    object Main : Screen()
    object Lists : Screen()
}

class MainActivity : ComponentActivity() {
    
    private val themeViewModel: ThemeViewModel by viewModels()
    private lateinit var shoppingListViewModel: ShoppingListViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferences = ThemePreferences(this)
        
        // Inicializar el ViewModel manualmente para poder usarlo en navegación
        shoppingListViewModel = ViewModelProvider(this)[ShoppingListViewModel::class.java]
        
        setContent {
            // Estado para el color primario (null mientras carga)
            var primaryColor by remember { mutableStateOf<Int?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            // Observar el modo de tema desde el ViewModel
            val themeMode by themeViewModel.themeMode.collectAsState()
            val followSystem by themeViewModel.followSystem.collectAsState()
            
            // Estado de navegación
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
            
            // Cargar el color guardado al iniciar
            LaunchedEffect(Unit) {
                val color = themePreferences.primaryColor.first()
                primaryColor = color
                isLoading = false
            }
            
            // Función para actualizar el color
            val onColorChanged: (Int) -> Unit = { newColor ->
                primaryColor = newColor
                lifecycleScope.launch {
                    themePreferences.setPrimaryColor(newColor)
                }
            }
            
            // Función para cambiar de lista
            val onListSelected: (Long) -> Unit = { listId ->
                shoppingListViewModel.switchToList(listId)
                currentScreen = Screen.Main
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
                        when (currentScreen) {
                            is Screen.Splash -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        currentScreen = Screen.Main
                                    }
                                )
                            }
                            is Screen.Main -> {
                                MainScreen(
                                    viewModel = shoppingListViewModel,
                                    currentPrimaryColor = primaryColor ?: ThemePreferences.DEFAULT_COLOR,
                                    onColorChanged = onColorChanged,
                                    themeModeString = themeMode,
                                    onThemeToggle = { themeViewModel.toggleTheme() },
                                    followSystem = followSystem,
                                    onFollowSystemChange = { themeViewModel.setFollowSystem(it) },
                                    onNavigateToLists = {
                                        currentScreen = Screen.Lists
                                    }
                                )
                            }
                            is Screen.Lists -> {
                                ListsScreen(
                                    onListSelected = onListSelected,
                                    onNavigateBack = {
                                        currentScreen = Screen.Main
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}