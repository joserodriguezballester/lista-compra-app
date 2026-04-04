package com.jose.listacompra.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : NavScreen("splash", "Splash", Icons.Default.Home)
    object Home : NavScreen("home", "Inicio", Icons.Default.Home)
    object ShoppingList : NavScreen("lista", "Mi Lista", Icons.Default.ShoppingCart)
    object Catalogo : NavScreen("articulos", "Catálogo", Icons.Default.Inventory)
    object Supermarkets : NavScreen("supermercados", "Supermercados", Icons.Default.Store)
    object Offers : NavScreen("ofertas", "Ofertas", Icons.Default.LocalOffer)
    object BarcodeScanner : NavScreen("scanner", "Escanear", Icons.Default.Home)
    
    // Rutas con argumentos
    object SupermarketAisles : NavScreen("supermercado/{supermarketId}/pasillos", "Pasillos", Icons.Default.Store) {
        fun createRoute(supermarketId: Long) = "supermercado/$supermarketId/pasillos"
    }
}
