package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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