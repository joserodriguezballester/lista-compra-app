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
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.ui.components.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAisles: (Long) -> Unit,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    viewModel: SupermarketListViewModel = hiltViewModel()
) {
    val supermarkets by viewModel.supermarkets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Supermarket?>(null) }
    var supermarketToEdit by remember { mutableStateOf<Supermarket?>(null) }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Supermercados",
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
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir supermercado")
            }
        }
    ) { paddingValues ->
        if (supermarkets.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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

@Composable
private fun SupermarketCard(
    supermarket: Supermarket,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Emoji
            Text(
                text = supermarket.emoji,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info
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

            // Acciones
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
            text = "Pulsa + para añadir uno nuevo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

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
