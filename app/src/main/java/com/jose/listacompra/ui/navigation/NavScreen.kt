package com.jose.listacompra.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    object ShoppingList : NavScreen("lista", "Mi Lista", Icons.Default.ShoppingCart)
    object Catalogo : NavScreen("articulos", "Catálogo", Icons.Default.Inventory)
}