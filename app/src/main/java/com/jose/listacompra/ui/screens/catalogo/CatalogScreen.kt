package com.jose.listacompra.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.screens.catalogo.ArticuloCard
import com.jose.listacompra.ui.viewmodel.ArticuloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onNavigateBack: () -> Unit = {},
    onScanBarcode: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onEditArticulo: (Articulo) -> Unit = {},
    viewModel: ArticuloViewModel = hiltViewModel(),

    // Añade estos parámetros para que el TopBar funcione correctamente
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
) {
    val articulos by viewModel.listaArticulos.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var articuloToEdit by remember { mutableStateOf<Articulo?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Articulo?>(null) }

    val articulosFiltrados = remember(articulos, searchQuery) {
        if (searchQuery.isBlank()) articulos
        else articulos.filter {
            it.name.contains(searchQuery, ignoreCase = true)
            // it.categoryId.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Catálogo de Artículos",
                onNavigateBack = onNavigateBack,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onOpenLists = onOpenLists,
                onChangeColor = onChangeColor,
                onOpenImport = onOpenImport,
                extraActions = {
                    // Aquí metemos el botón específico de esta pantalla
                    IconButton(onClick = onScanBarcode) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar artículos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            // Empty state
            if (articulosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "No hay artículos" else "Sin resultados",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isEmpty()) {
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Añadir primer artículo")
                            }
                        }
                    }
                }
            } else {
                // Grid of products
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(articulosFiltrados, key = { it.id }) { articulo ->
                        ArticuloCard(
                            articulo = articulo,
                            onClick = { onEditArticulo(articulo) },
                            onLongClick = { showDeleteConfirm = articulo }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || articuloToEdit != null) {
        AddEditArticuloDialog(
            articulo = articuloToEdit,
            onDismiss = {
                showAddDialog = false
                articuloToEdit = null
            },
            onSave = { nuevoArticulo ->
                if (articuloToEdit != null) {
                    viewModel.updateArticulo(nuevoArticulo)
                } else {
                    viewModel.addArticulo(nuevoArticulo)
                }
                showAddDialog = false
                articuloToEdit = null
            }
        )
    }

    // Delete Confirmation
    showDeleteConfirm?.let { articulo ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar artículo") },
            text = { Text("¿Eliminar '${articulo.name}' del catálogo?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArticulo(articulo)
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
