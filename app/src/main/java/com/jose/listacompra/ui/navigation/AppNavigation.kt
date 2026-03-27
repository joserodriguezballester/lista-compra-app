package com.jose.listacompra.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jose.listacompra.ui.screens.SplashScreen
import com.jose.listacompra.ui.screens.catalogo.CatalogoScreen
import com.jose.listacompra.ui.screens.home.HomeScreen
import com.jose.listacompra.ui.screens.productlist.ProductListScreen
import com.jose.listacompra.ui.screens.scanner.BarcodeScannerScreen
import com.jose.listacompra.ui.screens.supermarket.SupermarketAislesScreen
import com.jose.listacompra.ui.screens.supermarket.SupermarketListScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    padding: PaddingValues,
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
                onNavigateToList = { navController.navigate(NavScreen.ShoppingList.route) },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
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
                navController = navController,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() },
                onChangeColor = onChangeColor,
                onOpenLists = onOpenLists,
                onOpenImport = onOpenImport
            )
        }
        
        // Scanner de códigos de barras
        composable(NavScreen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scannedEan", barcode)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Lista de Supermercados
        composable(NavScreen.Supermarkets.route) {
            SupermarketListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAisles = { supermarketId ->
                    navController.navigate(NavScreen.SupermarketAisles.createRoute(supermarketId))
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() },
                onChangeColor = onChangeColor,
                onOpenLists = onOpenLists,
                onOpenImport = onOpenImport
            )
        }
        
        // Pasillos de un Supermercado
        composable(
            route = NavScreen.SupermarketAisles.route,
            arguments = listOf(
                navArgument("supermarketId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val supermarketId = backStackEntry.arguments?.getLong("supermarketId") ?: 1L
            SupermarketAislesScreen(
                supermarketId = supermarketId,
                onNavigateBack = { navController.popBackStack() },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() },
                onChangeColor = onChangeColor,
                onOpenLists = onOpenLists,
                onOpenImport = onOpenImport
            )
        }
    }
}