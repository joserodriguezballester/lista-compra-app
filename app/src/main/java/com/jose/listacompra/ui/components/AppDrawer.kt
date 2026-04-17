package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jose.listacompra.ui.navigation.DrawerDestination

@Composable
fun AppDrawer(
    currentDestination: DrawerDestination? = null,
    onDestinationSelected: ((DrawerDestination) -> Unit)? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTicketImport: () -> Unit = {}
) {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (_: Exception) {
        "?"
    }

    fun resolveDestination(destination: DrawerDestination) {
        if (onDestinationSelected != null) {
            onDestinationSelected(destination)
            return
        }
        when (destination) {
            DrawerDestination.Home -> onNavigateToHome()
            DrawerDestination.ShoppingList -> onNavigateToList()
            DrawerDestination.Catalog -> onNavigateToCatalogo()
            DrawerDestination.Categories -> onNavigateToCategories()
            DrawerDestination.Offers -> onNavigateToOffers()
            DrawerDestination.Supermarkets -> onNavigateToSupermarkets()
            DrawerDestination.History -> onNavigateToHistory()
            DrawerDestination.TicketImport -> onNavigateToTicketImport()
        }
    }

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
            DrawerDestination.all.forEach { destination ->
                if (destination == DrawerDestination.TicketImport) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                NavigationDrawerItem(
                    icon = { Text(destination.emoji) },
                    label = { Text(destination.label) },
                    selected = currentDestination == destination,
                    onClick = { resolveDestination(destination) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()
        Text(
            text = "$versionName • Jose Rodríguez",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
