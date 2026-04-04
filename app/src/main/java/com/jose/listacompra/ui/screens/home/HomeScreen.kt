package com.jose.listacompra.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onChangeColor: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                isDarkMode = false,
                onToggleDarkMode = { },
                onNavigateToOffers = {
                    scope.launch { drawerState.close() }
                    onNavigateToOffers()
                },
                onNavigateToSupermarkets = {
                    scope.launch { drawerState.close() }
                    onNavigateToSupermarkets()
                },
                onNavigateToCatalogo = {
                    scope.launch { drawerState.close() }
                    onNavigateToCatalogo()
                },
                onChangeColor = {
                    scope.launch { drawerState.close() }
                    onChangeColor()
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = "Lista Compra",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onChangeColor = onChangeColor
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "¿Qué quieres hacer?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Mi Lista",
                        subtitle = "Lista de la compra",
                        icon = Icons.Default.ShoppingCart,
                        emoji = "🛒",
                        onClick = onNavigateToList
                    )
                    
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Catálogo",
                        subtitle = "Artículos",
                        icon = Icons.Default.Inventory,
                        emoji = "📦",
                        onClick = onNavigateToCatalogo
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Supermercados",
                        subtitle = "Gestionar",
                        icon = Icons.Default.Store,
                        emoji = "🏪",
                        enabled = true,
                        onClick = onNavigateToSupermarkets
                    )
                    
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Ofertas",
                        subtitle = "Ver ofertas",
                        icon = Icons.Default.LocalOffer,
                        emoji = "🏷️",
                        enabled = true,
                        onClick = onNavigateToOffers
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Historial",
                        subtitle = "Próximamente",
                        icon = Icons.Default.History,
                        emoji = "📊",
                        enabled = false,
                        onClick = { }
                    )
                    
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Categorías",
                        subtitle = "Próximamente",
                        icon = Icons.Default.Category,
                        emoji = "📂",
                        enabled = false,
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    emoji: String = "",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(140.dp),
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}