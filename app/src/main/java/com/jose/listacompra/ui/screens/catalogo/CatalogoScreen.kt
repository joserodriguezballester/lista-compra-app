package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
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
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavHostController
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.ui.components.CatalogBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.navigation.NavScreen
import com.jose.listacompra.ui.viewmodel.ArticuloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    navController: NavHostController? = null,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    viewModel: ArticuloViewModel = hiltViewModel(),
) {
    val articulos by viewModel.listaArticulos.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedArticulo by remember { mutableStateOf<Articulo?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Articulo?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var scannedEan by remember { mutableStateOf<String?>(null) }

    val articulosFiltrados = remember(articulos, searchQuery, selectedCategory) {
        var result = articulos
        if (searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(searchQuery, ignoreCase = true) || it.ean?.contains(searchQuery, ignoreCase = true) == true }
        }
        if (selectedCategory != null) {
            result = result.filter { it.categoryId?.toString() == selectedCategory }
        }
        result
    }
    
    val articuloNames = remember(articulos) {
        articulos.groupBy { it.name.lowercase() }.filter { it.value.size > 1 }.keys
    }
    
    val categories = remember(articulos) {
        articulos.mapNotNull { it.categoryId?.toString() }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            searchQuery, 
                            { searchQuery = it },
                            { Text("Buscar artículos...") },
                            Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    },
                    navigationIcon = {
                        IconButton({ showSearchBar = false; searchQuery = "" }) {
                            Icon(Icons.Default.ArrowBack, "Cerrar")
                        }
                    }
                )
            } else {
                CommonTopBar(
                    title = "Catálogo de Artículos",
                    onNavigateBack = onNavigateBack,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onOpenLists = onOpenLists,
                    onChangeColor = onChangeColor,
                    onOpenImport = onOpenImport
                )
            }
        },
        bottomBar = {
            CatalogBottomBar(
                onSearchClick = { showSearchBar = true },
                onFilterClick = { showFilterDialog = true },
                onCartClick = onNavigateToList
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Añadir artículo") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        if (articulosFiltrados.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(paddingValues),
                Alignment.CenterHorizontally,
                Arrangement.Center
            ) {
                Icon(Icons.Default.Inventory, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Text("No hay artículos", MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(articulosFiltrados, { it.id }) { articulo ->
                    ArticuloCard(
                        articulo,
                        { selectedArticulo = articulo },
                        articulo.name.lowercase() in articuloNames
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditArticuloDialog(
            null,
            scannedEan,
            { showAddDialog = false; scannedEan = null },
            { viewModel.addArticulo(it); showAddDialog = false; scannedEan = null },
            { showAddDialog = false; navController?.navigate(NavScreen.BarcodeScanner.route) }
        )
    }

    selectedArticulo?.let { articulo ->
        ArticuloDetailDialog(
            articulo,
            { selectedArticulo = null },
            { viewModel.updateArticulo(it); selectedArticulo = null },
            { showDeleteConfirm = articulo; selectedArticulo = null }
        )
    }

    if (showFilterDialog) {
        CategoryFilterDialog(
            categories,
            selectedCategory,
            { showFilterDialog = false },
            { selectedCategory = it }
        )
    }

    showDeleteConfirm?.let { articulo ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar artículo") },
            text = { Text("¿Eliminar '${articulo.name}' del catálogo?") },
            confirmButton = {
                TextButton({
                    viewModel.deleteArticulo(articulo)
                    showDeleteConfirm = null
                }, ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton({ showDeleteConfirm = null }) { Text("Cancelar") } }
        )
    }
}
