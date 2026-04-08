package com.jose.listacompra.ui.screens.catalogo

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.ui.components.*
import com.jose.listacompra.ui.navigation.NavScreen
import com.jose.listacompra.ui.viewmodel.ArticuloViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    navController: NavHostController? = null,
    viewModel: ArticuloViewModel = hiltViewModel(),
    productListViewModel: com.jose.listacompra.ui.viewmodel.ProductListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    onChangeColor: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val articulos by viewModel.listaArticulos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedArticulo by remember { mutableStateOf<Articulo?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Articulo?>(null) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var scannedEan by remember { mutableStateOf<String?>(null) }
    var scannedName by remember { mutableStateOf<String?>(null) }
    var scannedImageUrl by remember { mutableStateOf<String?>(null) }
    var scannedQuantity by remember { mutableStateOf<String?>(null) }
    var scannedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Recibir datos del scanner
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("scannedEan")
            ?.observeForever { ean ->
                if (ean != null && showAddDialog.not()) {
                    scannedEan = ean
                    scannedName = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("scannedName")
                    scannedImageUrl = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("scannedImageUrl")
                    scannedQuantity = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("scannedQuantity")
                    scannedCategoryId = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("scannedCategoryId")
                    
                    showAddDialog = true
                    
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("scannedEan")
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("scannedName")
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("scannedImageUrl")
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("scannedQuantity")
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("scannedCategoryId")
                }
            }
    }

    val articulosFiltrados = remember(articulos, searchQuery, selectedCategoryId) {
        var result = articulos
        if (searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(searchQuery, ignoreCase = true) || it.ean?.contains(searchQuery, ignoreCase = true) == true }
        }
        if (selectedCategoryId != null) {
            result = result.filter { it.categoryId?.toString() == selectedCategoryId }
        }
        result
    }
    
    // Map de categorías para acceso rápido
    val categoryMap = remember(categorias) {
        categorias.associateBy { it.id }
    }

    val articuloNames = remember(articulos) {
        articulos.groupBy { it.name.lowercase() }.filter { it.value.size > 1 }.keys
    }

    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                onNavigateToHome = {
                    scope.launch { drawerState.close() }
                    onNavigateToHome()
                },
                onNavigateToList = {
                    scope.launch { drawerState.close() }
                    onNavigateToList()
                },
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
                    onNavigateToSupermarkets()
                },
                onNavigateToCatalogo = {
                    scope.launch { drawerState.close() }
                    // Ya estamos en catálogo
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (showSearchBar) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Buscar artículos...") }
                            )
                        },
                        navigationIcon = {
                            IconButton({ showSearchBar = false; searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Cerrar")
                            }
                        }
                    )
                } else {
                    CommonTopBar(
                        title = "📦 Catálogo",
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onChangeColor = onChangeColor,
                        onToggleDarkMode = onToggleDarkMode,
                        isDarkMode = isDarkMode,
                        onMicrophoneClick = { showVoiceDialog = true },
                        overflowActions = { expanded, onDismiss ->
                            DropdownMenuItem(
                                text = { Text("Añadir manual") },
                                onClick = {
                                    showAddDialog = true
                                    onDismiss()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Escanear código") },
                                onClick = {
                                    navController?.navigate(NavScreen.BarcodeScanner.route)
                                    onDismiss()
                                },
                                leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                            )
                        }
                    )
                }
            },
            bottomBar = {
                CatalogBottomBar(
                    onSearchClick = { showSearchBar = true },
                    onFilterClick = { showFilterDialog = true },
                    onCartClick = onNavigateToList,
                    onHomeClick = onNavigateToHome,
                    onScanClick = { navController?.navigate(NavScreen.BarcodeScanner.route) },
                    onAddClick = { showAddDialog = true }
                )
            }
        ) { paddingValues ->
            if (articulosFiltrados.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay artículos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir artículo")
                    }
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
                        val category = categorias.find { it.id.toString() == articulo.categoryId?.toString() }
                        ArticuloCard(
                            articulo,
                            { selectedArticulo = articulo; selectedImageUri = null },
                            articulo.name.lowercase() in articuloNames,
                            category
                        )
                    }
                }
            }
        }
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraSelected = { imagePicker.openCamera() },
            onGallerySelected = { imagePicker.openGallery() }
        )
    }

    if (showAddDialog) {
        AddEditArticuloDialog(
            articulo = null,
            ean = scannedEan,
            selectedImageUri = scannedImageUrl ?: selectedImageUri?.toString(),
            categories = categorias,
            prefillName = scannedName,
            prefillQuantity = scannedQuantity,
            prefillCategoryId = scannedCategoryId,
            onDismiss = { 
                showAddDialog = false
                scannedEan = null
                scannedName = null
                scannedImageUrl = null
                scannedQuantity = null
                scannedCategoryId = null
                selectedImageUri = null
            },
            onSave = { 
                viewModel.addArticulo(it)
                showAddDialog = false
                scannedEan = null
                scannedName = null
                scannedImageUrl = null
                scannedQuantity = null
                scannedCategoryId = null
                selectedImageUri = null
            },
            onScanBarcode = { 
                showAddDialog = false
                navController?.navigate(NavScreen.BarcodeScanner.route) 
            },
            onSelectImage = { showImageSourceDialog = true }
        )
    }

    selectedArticulo?.let { articulo ->
        ArticuloDetailDialog(
            articulo = articulo.copy(photoUri = selectedImageUri?.toString() ?: articulo.photoUri),
            categories = categorias,
            onDismiss = { selectedArticulo = null; selectedImageUri = null },
            onSave = { viewModel.updateArticulo(it); selectedArticulo = null; selectedImageUri = null },
            onDelete = { showDeleteConfirm = articulo; selectedArticulo = null },
            onSelectImage = { showImageSourceDialog = true }
        )
    }

    if (showFilterDialog) {
        CategoryFilterDialog(
            categories = categorias,
            selectedCategoryId = selectedCategoryId,
            onDismiss = { showFilterDialog = false },
            onCategorySelected = { selectedCategoryId = it }
        )
    }

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
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton({ showDeleteConfirm = null }) { Text("Cancelar") } }
        )
    }

    // Diálogo de voz (T5 refactor)
    if (showVoiceDialog) {
        com.jose.listacompra.ui.components.VoiceInputDialog(
            viewModel = productListViewModel,
            onDismiss = { showVoiceDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDrawerState(initialValue: DrawerValue): androidx.compose.material3.DrawerState {
    return androidx.compose.material3.rememberDrawerState(initialValue = initialValue)
}
