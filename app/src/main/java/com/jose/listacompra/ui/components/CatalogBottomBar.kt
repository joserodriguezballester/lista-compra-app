package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CatalogBottomBar(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCartClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(56.dp)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onSearchClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Buscar") },
            colors = NavigationBarItemDefaults.colors()
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onFilterClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Label,
                    contentDescription = "Filtrar",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Filtrar") },
            colors = NavigationBarItemDefaults.colors()
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onCartClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Lista",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Lista") },
            colors = NavigationBarItemDefaults.colors()
        )
    }
}
