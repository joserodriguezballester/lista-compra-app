package com.jose.listacompra.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jose.listacompra.ui.screens.SplashScreen
import com.jose.listacompra.ui.screens.catalog.CatalogoScreen
import com.jose.listacompra.ui.screens.home.HomeScreen
import com.jose.listacompra.ui.screens.productlist.ProductListScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    padding: PaddingValues,
    // Theme settings (pasados desde MainActivity)
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenLists: () -> Unit = {},
    onOpenImport: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = NavScreen.Splash.route,
        modifier = Modifier.padding(padding)
    ) {
        // Splash → Home
        composable(NavScreen.Splash.route) {
            SplashScreen(
                navController = navController,
                onNavigateToHome = { 
                    navController.navigate(NavScreen.Home.route) {
                        popUpTo(NavScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home
        composable(NavScreen.Home.route) {
            HomeScreen(
                onNavigateToList = { 
                    navController.navigate(NavScreen.ShoppingList.route) 
                },
                onNavigateToCatalogo = { 
                    navController.navigate(NavScreen.Catalogo.route) 
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() },
                onChangeColor = onChangeColor,
                onOpenLists = onOpenLists,
                onOpenImport = onOpenImport
            )
        }
        
        // ShoppingList (ProductListScreen)
        composable(NavScreen.ShoppingList.route) {
            ProductListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() },
                onChangeColor = onChangeColor,
                onOpenLists = onOpenLists,
                onOpenImport = onOpenImport
            )
        }
        
        // Catálogo
        composable(NavScreen.Catalogo.route) {
            CatalogoScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToList = { navController.navigate(NavScreen.ShoppingList.route) },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() },
                onChangeColor = onChangeColor,
                onOpenLists = onOpenLists,
                onOpenImport = onOpenImport
            )
        }
    }
}
