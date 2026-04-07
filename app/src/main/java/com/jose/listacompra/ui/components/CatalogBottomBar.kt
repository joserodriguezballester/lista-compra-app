package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * BottomBar específica para el catálogo.
 * Solo iconos, sin texto. Altura 56dp.
 */
@Composable
fun CatalogBottomBar(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCartClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    NavigationBar(
        modifier = Modifier.height(56.dp)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onSearchClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onFilterClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar",
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onScanClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Escanear",
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onAddClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir",
                    modifier = Modifier.size(24.dp)
                )
            }
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
            }
        )
    }
}
