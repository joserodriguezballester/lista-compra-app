package com.jose.listacompra.ui.screens.productlist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.components.AisleHeader
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.components.PriceHistoryChart
import com.jose.listacompra.ui.components.PriceStatsCard
import com.jose.listacompra.ui.components.ProductCard
import com.jose.listacompra.ui.components.SupermarketBottomBar
import com.jose.listacompra.ui.components.VoiceInputButton
import com.jose.listacompra.ui.screens.ColorSettingsDialog
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
import com.jose.listacompra.ui.components.AisleHeader
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.components.PriceHistoryChart
import com.jose.listacompra.ui.components.PriceStatsCard
import com.jose.listacompra.ui.components.ProductCard
import com.jose.listacompra.ui.components.SupermarketBottomBar
import com.jose.listacompra.ui.components.VoiceInputButton
import com.jose.listacompra.ui.screens.ColorSettingsDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    navController: NavController? = null,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiState by viewModel.uiState.collectAsState()

    // Estados para diálogos
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    
    // Datos del scanner
    var scannedName by remember { mutableStateOf<String?>(null) }
    var scannedPrice by remember { mutableStateOf<Float?>(null) }
    var scannedAisleId by remember { mutableStateOf<Long?>(null) }
    
    // Color actual
    val currentColor by viewModel.primaryColor.collectAsState(initial = 0)

    // Leer datos del scanner al volver
    LaunchedEffect(navController?.currentBackStackEntry?.savedStateHandle) {
        navController?.currentBackStackEntry?.savedStateHandle
            ?.get<String>("scannedName")?.let { name ->
                scannedName = name
                showAddProductDialog = true
            }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.get<String>("scannedImageUrl")?.let { /* manejar imagen */ }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.get<String>("scannedQuantity")?.let { qty ->
                scannedPrice = qty.toFloatOrNull()
            }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.get<String>("scannedCategoryId")?.let { /* manejar categoría */ }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { newMode ->
                    onToggleDarkMode(newMode)
                    scope.launch { drawerState.close() }
                },
                onNavigateToOffers = {
                    scope.launch { drawerState.close() }
                    onNavigateToOffers()
                },
                onNavigateToSupermarkets = {
                    scope.launch { drawerState.close() }
                    onNavigateToSupermarkets()
                },
                onNavigateToCatalogo = {
                    scope.launch { drawerState.close() }
                    onNavigateToCatalogo()
                },
                onNavigateToCategories = {
                    scope.launch { drawerState.close() }
                    onNavigateToCategories()
                },
                onChangeColor = {
                    scope.launch { drawerState.close() }
                    showColorDialog = true
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = "Mi Lista de la Compra",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onMicrophoneClick = { showVoiceDialog = true },
                    onChangeColor = { showColorDialog = true }
                )
            },
            bottomBar = {
                SupermarketBottomBar(
                    supermarkets = uiState.supermarkets,
                    selectedSupermarketId = uiState.selectedSupermarketId ?: 0L,
                    onSupermarketSelected = { viewModel.selectSupermarket(it) }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                    text = { Text("Añadir producto") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.productsByAisle.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🛒",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "Lista vacía",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Pulsa + para añadir productos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.productsByAisle.forEach { (aisle, products) ->
                        item(
                            key = "header_${aisle.id}",
                            contentType = "header",
                            span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }
                        ) {
                            AisleHeader(
                                aisleName = aisle.name,
                                aisleIcon = aisle.emoji,
                                productCount = products.size,
                                purchasedCount = products.count { it.isPurchased },
                                isCollapsed = aisle.id in uiState.collapsedAisles,
                                onToggle = { viewModel.toggleAisleCollapse(aisle.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (aisle.id !in uiState.collapsedAisles) {
                            items(
                                items = products,
                                key = { "product_${it.id}" },
                                contentType = { "product" }
                            ) { product ->
                                val offer = uiState.offers.find { it.id == product.offerId }
                                if (product.offerId != null && product.offerId > 0) {
                                    Log.d("ProductListScreen", "🔍 ${product.name}: offerId=${product.offerId}, found offer=${offer?.name}, total offers=${uiState.offers.size}")
                                }
                                SwipeableProductCard(
                                    product = product,
                                    offer = offer,
                                    onClick = { productToEdit = product },
                                    onTogglePurchased = { viewModel.toggleProductPurchased(product) },
                                    onRemove = { viewModel.removeProduct(product) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogo añadir
        if (showAddProductDialog) {
            AddProductToListDialog(
                aisles = uiState.aisles,
                offers = uiState.offers,
                suggestions = uiState.articleSuggestions,
                historySuggestions = uiState.historySuggestions,
                initialName = scannedName,
                onSearch = { query -> viewModel.searchArticles(query) },
                onOpenScanner = onNavigateToScanner,
                onDismiss = { 
                    showAddProductDialog = false
                    scannedName = null
                    scannedPrice = null
                    // Limpiar savedStateHandle
                    navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedName")
                },
                onAdd = { name, quantity, aisleId, price, offerId, notes, photoUri ->
                    viewModel.addProduct(name, quantity, aisleId, price, offerId, notes, photoUri)
                    showAddProductDialog = false
                    scannedName = null
                    scannedPrice = null
                }
            )
        }

        // Diálogo editar
        productToEdit?.let { product ->
            EditProductDialog(
                product = product,
                aisles = uiState.aisles,
                offers = uiState.offers,
                onDismiss = { productToEdit = null },
                onSave = { viewModel.updateProduct(it); productToEdit = null }
            )
        }

        // Diálogo cambiar color
        if (showColorDialog) {
            ColorSettingsDialog(
                currentColor = currentColor,
                onDismiss = { showColorDialog = false },
                onColorSelected = { color -> viewModel.setPrimaryColor(color) }
            )
        }
        
        // Diálogo de voz
        if (showVoiceDialog) {
            AlertDialog(
                onDismissRequest = { showVoiceDialog = false },
                title = { Text("Añadir por voz") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Di algo como:", style = MaterialTheme.typography.bodyMedium)
                        Text("\"3 litros de leche\"", style = MaterialTheme.typography.bodySmall)
                        Text("\"dos kilos de patatas\"", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        VoiceInputButton(
                            onVoiceCommand = { command ->
                                viewModel.addProduct(
                                    name = command.productName,
                                    quantity = command.quantity,
                                    aisleId = null,
                                    price = null,
                                    offerId = null,
                                    notes = null,
                                    photoUri = null
                                )
                                showVoiceDialog = false
                            },
                            modifier = Modifier.size(64.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVoiceDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableProductCard(
    product: Product,
    offer: com.jose.listacompra.domain.model.Offer?,
    onClick: () -> Unit,
    onTogglePurchased: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = { newValue ->
            if (newValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else false
        },
        positionalThreshold = { it * 0.5f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        content = {
            ProductCard(product, offer, onClick, onTogglePurchased, Modifier.fillMaxWidth())
        }
    )
}  product: Product,
    offer: com.jose.listacompra.domain.model.Offer?,
    onClick: () -> Unit,
    onTogglePurchased: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = { newValue ->
            if (newValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else false
        },
        positionalThreshold = { it * 0.5f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        content = {
            ProductCard(product, offer, onClick, onTogglePurchased, Modifier.fillMaxWidth())
        }
    )
}