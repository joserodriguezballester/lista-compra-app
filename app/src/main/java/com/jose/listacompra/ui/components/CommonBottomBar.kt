package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Bottom bar simple con Home y Mi Lista
 * Solo iconos, sin texto, más compacta
 */
@Composable
fun CommonBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToList: () -> Unit,
    currentRoute: String? = null,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(56.dp),
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
            }
        )
        
        NavigationBarItem(
            selected = currentRoute == "lista",
            onClick = onNavigateToList,
            icon = { 
                Icon(
                    Icons.Default.ShoppingCart, 
                    contentDescription = "Mi Lista"
                ) 
            }
        )
    }
}
