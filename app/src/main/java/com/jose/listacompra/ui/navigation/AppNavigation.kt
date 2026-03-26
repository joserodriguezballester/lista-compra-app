package com.jose.listacompra.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jose.listacompra.ui.screens.SplashScreen
import com.jose.listacompra.ui.screens.catalog.CatalogoScreen

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
        composable(NavScreen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(NavScreen.ShoppingList.route) {
            // TODO: ProductListScreen con los callbacks
            // ShoppingListScreen(
            //     isDarkMode = isDarkMode,
            //     onToggleTheme = onToggleTheme,
            //     onChangeColor = onChangeColor
            // )
        }
        composable(NavScreen.Catalogo.route) {
            CatalogoScreen(
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
