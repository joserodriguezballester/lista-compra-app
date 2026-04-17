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
import com.jose.listacompra.ui.screens.categories.CategoriesScreen
import com.jose.listacompra.ui.screens.history.HistoryScreen
import com.jose.listacompra.ui.screens.home.HomeScreen
import com.jose.listacompra.ui.screens.offers.OffersScreen
import com.jose.listacompra.ui.screens.productlist.ProductListScreen
import com.jose.listacompra.ui.screens.scanner.BarcodeScannerScreen
import com.jose.listacompra.ui.screens.supermarket.SupermarketAislesScreen
import com.jose.listacompra.ui.screens.supermarket.SupermarketListScreen
import com.jose.listacompra.ui.screens.ticket.TicketImportScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    padding: PaddingValues,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onChangeColor: () -> Unit = {},
) {
    val appNavigator = AppNavigatorImpl(navController)

    // Función común para navegar a Home
    val navigateToHome: () -> Unit = {
        navController.navigate(NavScreen.Home.route) {
            popUpTo(NavScreen.Home.route) { inclusive = true }
        }
    }
    
    // Función común para navegar a Lista
    val navigateToList: () -> Unit = {
        navController.navigate(NavScreen.ShoppingList.route) {
            popUpTo(NavScreen.Home.route) { inclusive = false }
        }
    }

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
                navigator = appNavigator,
                onNavigateToList = navigateToList,
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onNavigateToHistory = { navController.navigate(NavScreen.History.route) },
                onNavigateToTicketImport = { navController.navigate(NavScreen.TicketImport.route) },
                onChangeColor = onChangeColor,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleTheme
            )
        }
        
        composable(NavScreen.ShoppingList.route) {
            ProductListScreen(
                navigator = appNavigator,
                onNavigateToHome = navigateToHome,
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToScanner = { navController.navigate(NavScreen.BarcodeScanner.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onNavigateToHistory = { navController.navigate(NavScreen.History.route) },
                navController = navController
            )
        }
        
        composable(NavScreen.Catalogo.route) {
            CatalogoScreen(
                navigator = appNavigator,
                onNavigateToList = navigateToList,
                onNavigateToHome = navigateToHome,
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onNavigateToHistory = { navController.navigate(NavScreen.History.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                navController = navController,
                onToggleDarkMode = onToggleTheme,
                isDarkMode = isDarkMode,
                onChangeColor = onChangeColor
            )
        }
        
        composable(NavScreen.Offers.route) {
            OffersScreen(
                navigator = appNavigator,
                onNavigateToHome = navigateToHome,
                onNavigateToList = navigateToList,
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onNavigateToHistory = { navController.navigate(NavScreen.History.route) },
                onToggleDarkMode = onToggleTheme,
                isDarkMode = isDarkMode,
                onChangeColor = onChangeColor
            )
        }
        
        composable(NavScreen.Categories.route) {
            CategoriesScreen(
                navigator = appNavigator,
                onNavigateToHome = navigateToHome,
                onNavigateToList = navigateToList,
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToHistory = { navController.navigate(NavScreen.History.route) },
                onToggleDarkMode = onToggleTheme,
                isDarkMode = isDarkMode,
                onChangeColor = onChangeColor
            )
        }
        
        composable(NavScreen.History.route) {
            HistoryScreen(
                navigator = appNavigator,
                onNavigateToHome = navigateToHome,
                onNavigateToList = navigateToList,
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onNavigateToSupermarkets = { navController.navigate(NavScreen.Supermarkets.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onToggleDarkMode = onToggleTheme,
                isDarkMode = isDarkMode,
                onChangeColor = onChangeColor
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
        
        composable(NavScreen.TicketImport.route) {
            TicketImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = navigateToHome,
                onNavigateToHistory = {
                    navController.navigate(NavScreen.History.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(NavScreen.Supermarkets.route) {
            SupermarketListScreen(
                navigator = appNavigator,
                onNavigateToAisles = { supermarketId ->
                    navController.navigate(NavScreen.SupermarketAisles.createRoute(supermarketId))
                },
                onNavigateToHome = navigateToHome,
                onNavigateToList = navigateToList,
                onNavigateToOffers = { navController.navigate(NavScreen.Offers.route) },
                onNavigateToCategories = { navController.navigate(NavScreen.Categories.route) },
                onNavigateToHistory = { navController.navigate(NavScreen.History.route) },
                onNavigateToCatalogo = { navController.navigate(NavScreen.Catalogo.route) },
                onToggleDarkMode = onToggleTheme,
                isDarkMode = isDarkMode,
                onChangeColor = onChangeColor
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