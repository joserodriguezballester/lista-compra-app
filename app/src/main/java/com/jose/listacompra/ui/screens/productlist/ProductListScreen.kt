package com.jose.listacompra.ui.screens.productlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.components.*
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
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
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiState by viewModel.uiState.collectAsState()

    // Estados para diálogos
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showColorDialog by remember { mutableStateOf(false) }
    
    // Color actual
    val currentColor by viewModel.primaryColor.collectAsState()

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
                    onMicrophoneClick = { /* TODO: Voice input */ }
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.productsByAisle.isEmpty()) {
                EmptyState(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    onNavigateToCatalogo = onNavigateToCatalogo
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                initialName = null,
                onSearch = { query -> viewModel.searchArticles(query) },
                onOpenScanner = onNavigateToScanner,
                onDismiss = { showAddProductDialog = false },
                onAdd = { name, quantity, aisleId, price, offerId, notes, photoUri ->
                    viewModel.addProduct(name, quantity, aisleId, price, offerId, notes, photoUri)
                    showAddProductDialog = false
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
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onNavigateToCatalogo: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu lista está vacía",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToCatalogo) {
            Text("Ir al catálogo")
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
}