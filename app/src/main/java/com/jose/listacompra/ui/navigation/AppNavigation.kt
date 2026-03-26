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
 //   shoppingViewModel: ShoppingListViewModel,
    // articuloViewModel: ArticuloViewModel // Añadirás este luego
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
            // Aquí mueves todo tu LazyVerticalGrid de la lista
       //     ShoppingListScreen(viewModel = shoppingViewModel)
        }
        composable(NavScreen.Catalogo.route) {
            // Tu nueva pantalla de catálogo
            CatalogoScreen()
        }
    }
}