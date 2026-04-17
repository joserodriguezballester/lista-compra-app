package com.jose.listacompra.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.ui.AppUiConfig
import com.jose.listacompra.ui.components.AppDrawerScaffold
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.startDirectVoiceRecognition
import com.jose.listacompra.ui.navigation.AppNavigator
import com.jose.listacompra.ui.navigation.DrawerDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigator: AppNavigator,
    onNavigateToList: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTicketImport: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    productListViewModel: com.jose.listacompra.ui.viewmodel.ProductListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val appVersionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (_: Exception) {
        "?"
    }

    AppDrawerScaffold(
        title = "${AppUiConfig.HOME_TITLE} · $appVersionName",
        navigator = navigator,
        currentDestination = DrawerDestination.Home,
        onChangeColor = onChangeColor,
        onToggleDarkMode = onToggleDarkMode,
        isDarkMode = isDarkMode,
        onMicrophoneClick = { context, scope -> startDirectVoiceRecognition(context, productListViewModel, scope) },
        bottomBar = {
            CommonBottomBar(
                onNavigateToHome = { },
                onNavigateToList = onNavigateToList,
                currentRoute = "home"
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
                        emoji = "🛒",
                        onClick = onNavigateToList
                    )
                    
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Catálogo",
                        subtitle = "Artículos",
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
                        emoji = "🏪",
                        onClick = onNavigateToSupermarkets
                    )
                    
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Ofertas",
                        subtitle = "Ver ofertas",
                        emoji = "🏷️",
                        onClick = onNavigateToOffers
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Categorías",
                        subtitle = "Ver categorías",
                        emoji = "📂",
                        onClick = onNavigateToCategories
                    )
                    
                    HomeCard(
                        modifier = Modifier.weight(1f),
                        title = "Historial",
                        subtitle = "Estadísticas",
                        emoji = "📊",
                        onClick = onNavigateToHistory
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Importar ticket
                HomeCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Importar Ticket",
                    subtitle = "Escanea un ticket de Carrefour",
                    emoji = "🧾",
                    onClick = onNavigateToTicketImport
                )
            }
        }
}

@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
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
