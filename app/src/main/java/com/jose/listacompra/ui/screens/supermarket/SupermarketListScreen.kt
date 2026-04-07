package com.jose.listacompra.ui.screens.supermarket

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.R
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAisles: (Long) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    onChangeColor: () -> Unit = {},
    viewModel: SupermarketListViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val supermarkets by viewModel.supermarkets.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Supermarket?>(null) }
    var supermarketToEdit by remember { mutableStateOf<Supermarket?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                onNavigateToOffers = {
                    scope.launch { drawerState.close() }
                    onNavigateToOffers()
                },
                onNavigateToCategories = {
                    scope.launch { drawerState.close() }
                    onNavigateToCategories()
                },
                onNavigateToHistory = {
                    scope.launch { drawerState.close() }
                    onNavigateToHistory()
                },
                onNavigateToSupermarkets = {
                    scope.launch { drawerState.close() }
                },
                onNavigateToCatalogo = {
                    scope.launch { drawerState.close() }
                    onNavigateToCatalogo()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = "🏪 Supermercados",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onChangeColor = onChangeColor,
                    onToggleDarkMode = onToggleDarkMode,
                    isDarkMode = isDarkMode,
                    overflowActions = { expanded, onDismiss ->
                        DropdownMenuItem(
                            text = { Text("Añadir supermercado") },
                            onClick = {
                                showAddDialog = true
                                onDismiss()
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                )
            },
            bottomBar = {
                CommonBottomBar(
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToList = onNavigateToList,
                    currentRoute = "supermercados"
                )
            }
        ) { paddingValues ->
            if (supermarkets.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAddClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(supermarkets, key = { it.id }) { supermarket ->
                        SupermarketCard(
                            supermarket = supermarket,
                            onEdit = { supermarketToEdit = supermarket },
                            onDelete = { showDeleteConfirm = supermarket },
                            onClick = { onNavigateToAisles(supermarket.id) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de añadir
    if (showAddDialog) {
        SupermarketDialog(
            supermarket = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, emoji ->
                viewModel.addSupermarket(name, emoji)
                showAddDialog = false
            }
        )
    }

    // Diálogo de editar
    supermarketToEdit?.let { supermarket ->
        SupermarketDialog(
            supermarket = supermarket,
            onDismiss = { supermarketToEdit = null },
            onSave = { name, emoji ->
                viewModel.updateSupermarket(supermarket.copy(name = name, emoji = emoji))
                supermarketToEdit = null
            }
        )
    }

    // Confirmación de eliminar
    showDeleteConfirm?.let { supermarket ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar supermercado") },
            text = { Text("¿Eliminar '${supermarket.name}' y todos sus pasillos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSupermarket(supermarket)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Obtiene el resource ID del logo basado en el nombre del supermercado
 */
private fun getSupermarketLogo(name: String): Int? {
    val lowerName = name.lowercase()
    return when {
        lowerName.contains("carrefour") -> R.drawable.logo_carrefour
        lowerName.contains("mercadona") -> R.drawable.logo_mercadona
        lowerName.contains("lidl") -> R.drawable.logo_lidl
        lowerName.contains("aldi") -> R.drawable.logo_aldi
        lowerName.contains("dia") -> R.drawable.logo_dia
        lowerName.contains("consum") -> R.drawable.logo_consum
        else -> null
    }
}

@Composable
private fun SupermarketCard(
    supermarket: Supermarket,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val logoRes = getSupermarketLogo(supermarket.name)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (supermarket.isDefault)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo o emoji
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != null) {
                    Icon(
                        painter = painterResource(id = logoRes),
                        contentDescription = supermarket.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = androidx.compose.ui.graphics.Color.Unspecified
                    )
                } else {
                    Text(
                        text = supermarket.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supermarket.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (supermarket.isDefault) {
                    Text(
                        text = "Por defecto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay supermercados",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Añade tu supermercado favorito",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir supermercado")
        }
    }
}

@Composable
private fun SupermarketDialog(
    supermarket: Supermarket?,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf(supermarket?.name ?: "") }
    var emoji by remember { mutableStateOf(supermarket?.emoji ?: "🏪") }

    val emojiOptions = listOf("🏪", "🛒", "🟢", "🔵", "🟡", "🟠", "🔴", "🟣", "⭐", "❤️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (supermarket == null) "Nuevo supermercado" else "Editar supermercado") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Carrefour, Mercadona...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Emoji (si no hay logo):",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojiOptions.forEach { option ->
                        FilterChip(
                            selected = emoji == option,
                            onClick = { emoji = option },
                            label = { Text(option, style = MaterialTheme.typography.titleLarge) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, emoji) },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDrawerState(initialValue: DrawerValue): androidx.compose.material3.DrawerState {
    return androidx.compose.material3.rememberDrawerState(initialValue = initialValue)
}
