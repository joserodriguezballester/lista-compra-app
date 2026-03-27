package com.jose.listacompra.ui.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.ui.components.CatalogBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.navigation.NavScreen
import com.jose.listacompra.ui.screens.catalogo.AddEditArticuloDialog
import com.jose.listacompra.ui.screens.catalogo.ArticuloCard
import com.jose.listacompra.ui.screens.catalogo.ArticuloDetailDialog
import com.jose.listacompra.ui.screens.catalogo.CategoryFilterDialog
import com.jose.listacompra.ui.viewmodel.ArticuloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    navController: NavHostController? = null,
    // Theme settings
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    viewModel: ArticuloViewModel = hiltViewModel(),
) {
    val articulos by viewModel.listaArticulos.collectAsState()
    
    // Estados de UI
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedArticulo by remember { mutableStateOf<Articulo?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Articulo?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var scannedEan by remember { mutableStateOf<String?>(null) }

    // Filtrar artículos
    val articulosFiltrados = remember(articulos, searchQuery, selectedCategory) {
        var result = articulos
        
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.ean?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        if (selectedCategory != null) {
            result = result.filter { it.categoryId?.toString() == selectedCategory }
        }
        
        result
    }
    
    // Detectar variantes
    val articuloNames = remember(articulos) {
        articulos.groupBy { it.name.lowercase() }
            .filter { it.value.size > 1 }
            .keys
    }
    
    val categories = remember(articulos) {
        articulos.mapNotNull { it.categoryId?.toString() }
            .distinct()
            .sorted()
    }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = { 
                        showSearchBar = false
                        searchQuery = ""
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
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir artículo") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        if (articulosFiltrados.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(articulosFiltrados, key = { it.id }) { articulo ->
                    val hasVariants = articulo.name.lowercase() in articuloNames
                    
                    ArticuloCard(
                        articulo = articulo,
                        onClick = { selectedArticulo = articulo },
                        hasVariants = hasVariants
                    )
                }
            }
        }
    }

    // Diálogo de añadir/editar artículo
    if (showAddDialog) {
        AddEditArticuloDialog(
            articulo = null,
            ean = scannedEan, // EAN escaneado
            onDismiss = { 
                showAddDialog = false
                scannedEan = null
            },
            onSave = { articulo ->
                viewModel.addArticulo(articulo)
                showAddDialog = false
                scannedEan = null
            },
            onScanBarcode = {
                showAddDialog = false
                navController?.navigate(NavScreen.BarcodeScanner.route)
            }
        )
    }

    // Diálogo de detalle/editar
    selectedArticulo?.let { articulo ->
        ArticuloDetailDialog(
            articulo = articulo,
            onDismiss = { selectedArticulo = null },
            onSave = { updatedArticulo ->
                viewModel.updateArticulo(updatedArticulo)
                selectedArticulo = null
            },
            onDelete = {
                showDeleteConfirm = articulo
                selectedArticulo = null
            }
        )
    }

    // Diálogo de filtro
    if (showFilterDialog) {
        CategoryFilterDialog(
            categories = categories,
            selectedCategory = selectedCategory,
            onDismiss = { showFilterDialog = false },
            onCategorySelected = { category ->
                selectedCategory = category
            }
        )
    }

    // Confirmación de eliminar
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
    
    // Recibir EAN escaneado al volver del scanner
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry?.savedStateHandle
            ?.getStateFlow("scannedEan", null as String?)
            ?.collect { ean ->
                if (ean != null && scannedEan == null) {
                    scannedEan = ean
                    showAddDialog = true
                    navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedEan")
                }
            }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "No hay artículos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Buscar artículos...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Cerrar búsqueda"
                )
            }
        }
    )
}
