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
import com.jose.listacompra.ui.screens.home.HomeScreen
import com.jose.listacompra.ui.screens.productlist.ProductListScreen
import com.jose.listacompra.ui.screens.scanner.BarcodeScannerScreen
import com.jose.listacompra.ui.screens.supermarket.SupermarketAislesScreen
import com.jose.listacompra.ui.screens.supermarket.SupermarketListScreen
import com.jose.listacompra.ui.screens.catalogo.CatalogoScreen
import com.jose.listacompra.ui.screens.offers.OffersScreen
import com.jose.listacompra.ui.screens.categories.CategoriesScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    padding: PaddingValues,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onChangeColor: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = NavScreen.Splash.route,
        modifier = Modifier.padding(padding)
    ) {
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
        
        composable(NavScreen.Home.route) {
            HomeScreen(
                onNavigateToList = { navController.navigate(NavScreen.ShoppingList.route) },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onChangeColor = onChangeColor
            )
        }
        
        composable(NavScreen.ShoppingList.route) {
            ProductListScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToScanner = { navController.navigate(NavScreen.BarcodeScanner.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { onToggleTheme() }
            )
        }
        
        composable(NavScreen.Catalogo.route) {
            CatalogoScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToList = { navController.navigate(NavScreen.ShoppingList.route) },
                navController = navController
            )
        }
        
        composable(NavScreen.Offers.route) {
            OffersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavScreen.Categories.route) {
            CategoriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavScreen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeScanned = { ean, name, imageUrl, quantity, categoryId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.apply {
                            set("scannedEan", ean)
                            set("scannedName", name)
                            set("scannedImageUrl", imageUrl)
                            set("scannedQuantity", quantity)
                            set("scannedCategoryId", categoryId)
                        }
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavScreen.Supermarkets.route) {
            SupermarketListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAisles = { supermarketId ->
                    navController.navigate(NavScreen.SupermarketAisles.createRoute(supermarketId))
                }
            )
        }
        
        composable(
            route = NavScreen.SupermarketAisles.route,
            arguments = listOf(
                navArgument("supermarketId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val supermarketId = backStackEntry.arguments?.getLong("supermarketId") ?: 1L
            SupermarketAislesScreen(
                supermarketId = supermarketId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}