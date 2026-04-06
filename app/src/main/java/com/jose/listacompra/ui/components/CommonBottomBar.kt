package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Bottom bar simple con Home y Mi Lista
 * Para usar en pantallas que no tienen SupermarketBottomBar
 */
@Composable
fun CommonBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToList: () -> Unit,
    currentRoute: String? = null,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            icon = { 
                Icon(
                    Icons.Default.Home, 
                    contentDescription = "Home"
                ) 
            },
            label = { Text("Home") }
        )
        
        NavigationBarItem(
            selected = currentRoute == "lista",
            onClick = onNavigateToList,
            icon = { 
                Icon(
                    Icons.Default.ShoppingCart, 
                    contentDescription = "Mi Lista"
                ) 
            },
            label = { Text("Mi Lista") }
        )
    }
}
