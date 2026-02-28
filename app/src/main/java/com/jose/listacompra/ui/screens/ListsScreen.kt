package com.jose.listacompra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.ui.viewmodel.ListsManagementViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: ListsManagementViewModel = viewModel(),
    onListSelected: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }
    var listToRename by remember { mutableStateOf<ShoppingList?>(null) }
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Listas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { showArchived = !showArchived }) {
                        Text(if (showArchived) "Ver activas" else "Ver archivadas")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showArchived) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva Lista") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            if (!showArchived) {
                // Sección de listas activas
                if (uiState.activeLists.isEmpty()) {
                    item {
                        EmptyListsMessage(
                            icon = Icons.Default.ShoppingCart,
                            message = "No tienes listas activas",
                            subMessage = "Crea una nueva lista para empezar"
                        )
                    }
                } else {
                    items(uiState.activeLists, key = { it.id }) { list ->
                        ListCard(
                            list = list,
                            isSelected = list.id == uiState.currentListId,
                            onClick = {
                                viewModel.selectList(list.id)
                                onListSelected(list.id)
                            },
                            onArchive = { viewModel.archiveList(list.id) },
                            onRename = { listToRename = list }
                        )
                    }
                }
            } else {
                // Sección de listas archivadas
                if (uiState.archivedLists.isEmpty()) {
                    item {
                        EmptyListsMessage(
                            icon = Icons.Default.Archive,
                            message = "No tienes listas archivadas",
                            subMessage = "Las listas archivadas aparecerán aquí"
                        )
                    }
                } else {
                    items(uiState.archivedLists, key = { it.id }) { list ->
                        ArchivedListCard(
                            list = list,
                            onUnarchive = { viewModel.unarchiveList(list.id) },
                            onDelete = { listToDelete = list }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    
    // Diálogo para crear nueva lista
    if (showCreateDialog) {
        CreateListDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, useDefaultAisles ->
                viewModel.createList(name, useDefaultAisles) { listId ->
                    onListSelected(listId)
                }
                showCreateDialog = false
            }
        )
    }
    
    // Diálogo para renombrar
    listToRename?.let { list ->
        RenameListDialog(
            list = list,
            onDismiss = { listToRename = null },
            onRename = { newName ->
                viewModel.renameList(list, newName)
                listToRename = null
            }
        )
    }
    
    // Diálogo de confirmación para eliminar
    listToDelete?.let { list ->
        AlertDialog(
            onDismissRequest = { listToDelete = null },
            title = { Text("Eliminar lista") },
            text = { Text("¿Estás seguro de que quieres eliminar \"${list.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteList(list)
                        listToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ListCard(
    list: ShoppingList,
    isSelected: Boolean,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
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
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Creada: ${dateFormat.format(Date(list.fechaCreacion))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Abrir") },
                        onClick = { 
                            showMenu = false
                            onClick()
                        },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Renombrar") },
                        onClick = { 
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Archivar") },
                        onClick = { 
                            showMenu = false
                            onArchive()
                        },
                        leadingIcon = { Icon(Icons.Default.Archive, null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchivedListCard(
    list: ShoppingList,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Archivada: ${dateFormat.format(Date(list.fechaCreacion))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Restaurar") },
                        onClick = { 
                            showMenu = false
                            onUnarchive()
                        },
                        leadingIcon = { Icon(Icons.Default.Unarchive, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = { 
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyListsMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CreateListDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var useDefaultAisles by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Lista") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la lista") },
                    placeholder = { Text("Ej: Carrefour Mislata") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Opción para usar pasillos por defecto
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = useDefaultAisles,
                        onCheckedChange = { useDefaultAisles = it }
                    )
                    Text(
                        text = "Usar pasillos por defecto",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (!useDefaultAisles) {
                    Text(
                        text = "Se creará una lista vacía sin categorías predefinidas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim(), useDefaultAisles) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RenameListDialog(
    list: ShoppingList,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(list.name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renombrar Lista") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nuevo nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(name.trim()) },
                enabled = name.trim().isNotEmpty() && name.trim() != list.name
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
