package com.jose.listacompra.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jose.listacompra.ui.components.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    // Theme settings
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Lista Compra",
                onNavigateBack = null, // Home no tiene back
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onOpenLists = onOpenLists,
                onChangeColor = onChangeColor,
                onOpenImport = onOpenImport
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
            // Título de bienvenida
            Text(
                text = "¿Qué quieres hacer?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Grid de cards (2 columnas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Mi Lista
                HomeCard(
                    modifier = Modifier.weight(1f),
                    title = "Mi Lista",
                    subtitle = "Lista de la compra",
                    icon = Icons.Default.ShoppingCart,
                    emoji = "🛒",
                    onClick = onNavigateToList
                )
                
                // Card 2: Catálogo
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
                // Card 3: Ofertas (placeholder)
                HomeCard(
                    modifier = Modifier.weight(1f),
                    title = "Ofertas",
                    subtitle = "Próximamente",
                    icon = Icons.Default.LocalOffer,
                    emoji = "🏷️",
                    enabled = false,
                    onClick = { /* TODO */ }
                )
                
                // Card 4: Supermercados (placeholder)
                HomeCard(
                    modifier = Modifier.weight(1f),
                    title = "Supermercados",
                    subtitle = "Próximamente",
                    icon = Icons.Default.Store,
                    emoji = "🏪",
                    enabled = false,
                    onClick = { /* TODO */ }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 5: Historial (placeholder)
                HomeCard(
                    modifier = Modifier.weight(1f),
                    title = "Historial",
                    subtitle = "Próximamente",
                    icon = Icons.Default.History,
                    emoji = "📊",
                    enabled = false,
                    onClick = { /* TODO */ }
                )
                
                // Card 6: Categorías (placeholder)
                HomeCard(
                    modifier = Modifier.weight(1f),
                    title = "Categorías",
                    subtitle = "Próximamente",
                    icon = Icons.Default.Category,
                    emoji = "📂",
                    enabled = false,
                    onClick = { /* TODO */ }
                )
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
        modifier = modifier
            .height(140.dp),
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
            // Emoji o icono
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Título
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            
            // Subtítulo
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
