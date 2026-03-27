package com.jose.listacompra.ui.screens.supermarket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.ui.components.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketAislesScreen(
    supermarketId: Long,
    onNavigateBack: () -> Unit,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    viewModel: SupermarketAislesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAisleDialog by remember { mutableStateOf(false) }
    var aisleToEdit by remember { mutableStateOf<Aisle?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Aisle?>(null) }

    LaunchedEffect(supermarketId) {
        viewModel.loadSupermarket(supermarketId)
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = uiState.supermarket?.name ?: "Pasillos",
                onNavigateBack = onNavigateBack,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onOpenLists = onOpenLists,
                onChangeColor = onChangeColor,
                onOpenImport = onOpenImport
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAisleDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir pasillo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Info banner si usa categorías
            if (uiState.aisles.isEmpty() && uiState.usesCategories) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Este supermercado usa categorías como pasillos. Puedes personalizar los pasillos específicos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Lista de pasillos
            if (uiState.aisles.isEmpty() && !uiState.usesCategories) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.aisles, key = { it.id }) { aisle ->
                        AisleItem(
                            aisle = aisle,
                            onEdit = { aisleToEdit = aisle },
                            onDelete = { showDeleteConfirm = aisle }
                        )
                    }
                }
            }
        }
    }

    // Diálogo añadir pasillo
    if (showAddAisleDialog) {
        AisleDialog(
            aisle = null,
            supermarketId = supermarketId,
            nextOrderIndex = uiState.aisles.size,
            onDismiss = { showAddAisleDialog = false },
            onSave = { name, emoji, orderIndex ->
                viewModel.addAisle(name, emoji, orderIndex)
                showAddAisleDialog = false
            }
        )
    }

    // Diálogo editar pasillo
    aisleToEdit?.let { aisle ->
        AisleDialog(
            aisle = aisle,
            supermarketId = supermarketId,
            nextOrderIndex = aisle.orderIndex,
            onDismiss = { aisleToEdit = null },
            onSave = { name, emoji, orderIndex ->
                viewModel.updateAisle(aisle.copy(name = name, emoji = emoji, orderIndex = orderIndex))
                aisleToEdit = null
            }
        )
    }

    // Confirmación eliminar
    showDeleteConfirm?.let { aisle ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar pasillo") },
            text = { Text("¿Eliminar '${aisle.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAisle(aisle)
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

@Composable
private fun AisleItem(
    aisle: Aisle,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = aisle.emoji,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = aisle.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Orden: ${aisle.orderIndex}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Drag handle (visual)
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Arrastrar",
                tint = MaterialTheme.colorScheme.outline
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ViewAgenda,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay pasillos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pulsa + para añadir pasillos personalizados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AisleDialog(
    aisle: Aisle?,
    supermarketId: Long,
    nextOrderIndex: Int,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, orderIndex: Int) -> Unit
) {
    var name by remember { mutableStateOf(aisle?.name ?: "") }
    var emoji by remember { mutableStateOf(aisle?.emoji ?: "") }
    var orderIndex by remember { mutableStateOf(aisle?.orderIndex ?: nextOrderIndex) }

    val emojiOptions = listOf("🧴", "🍎", "🥓", "🥩", "🥫", "🧻", "🧼", "🥤", "🧀", "🧊", "🥛", "🍞")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (aisle == null) "Nuevo pasillo" else "Editar pasillo") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = orderIndex.toString(),
                    onValueChange = { orderIndex = it.toIntOrNull() ?: orderIndex },
                    label = { Text("Orden") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Emoji:",
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
                            label = { Text(option) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji personalizado") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, emoji, orderIndex) },
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
