package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawer(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onNavigateToOffers: () -> Unit,
    onNavigateToSupermarkets: () -> Unit,
    onNavigateToCatalogo: () -> Unit,
    onNavigateToCategories:() -> Unit,
    onNavigateToHistory: () -> Unit,
    onChangeColor: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🛒 Lista Compra",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tu asistente de compras",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        // Navigation items
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                label = { Text("Mi Lista") },
                selected = false,
                onClick = onClose
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                label = { Text("Catálogo") },
                selected = false,
                onClick = onNavigateToCatalogo
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Store, contentDescription = null) },
                label = { Text("Supermercados") },
                selected = false,
                onClick = onNavigateToSupermarkets
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                label = { Text("Ofertas") },
                selected = false,
                onClick = onNavigateToOffers
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                label = { Text("Categorías") },
                selected = false,
                onClick = onNavigateToCategories
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                label = { Text("Historial") },
                selected = false,
                onClick = onNavigateToHistory
            )

        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Settings section
        Text(
            text = "AJUSTES",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Modo oscuro")
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggleDarkMode
            )
        }

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Palette, contentDescription = null) },
            label = { Text("Cambiar color") },
            selected = false,
            onClick = onChangeColor
        )

        // Footer
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()
        Text(
            text = "v1.0.0 • Jose Rodríguez",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}