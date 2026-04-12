package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawer(
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit,
    onNavigateToSupermarkets: () -> Unit,
    onNavigateToCatalogo: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTicketImport: () -> Unit = {}
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
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

        HorizontalDivider()

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Home") },
                selected = false,
                onClick = onNavigateToHome
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                label = { Text("Mi Lista") },
                selected = false,
                onClick = onNavigateToList
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                label = { Text("Catálogo") },
                selected = false,
                onClick = onNavigateToCatalogo
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                label = { Text("Categorías") },
                selected = false,
                onClick = onNavigateToCategories
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                label = { Text("Ofertas") },
                selected = false,
                onClick = onNavigateToOffers
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Store, contentDescription = null) },
                label = { Text("Supermercados") },
                selected = false,
                onClick = onNavigateToSupermarkets
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                label = { Text("Historial") },
                selected = false,
                onClick = onNavigateToHistory
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                label = { Text("Importar Ticket") },
                selected = false,
                onClick = onNavigateToTicketImport
            )
        }

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
