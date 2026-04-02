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
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.components.SupermarketBottomBar
import com.jose.listacompra.ui.components.ProductCard
import com.jose.listacompra.ui.components.AisleHeader
import com.jose.listacompra.ui.viewmodel.ProductListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenLists: () -> Unit = {},
    onChangeColor: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estados para diálogos
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Mi Lista de la Compra",
                onNavigateBack = onNavigateBack,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onOpenLists = onOpenLists,
                onChangeColor = onChangeColor,
                onOpenImport = onOpenImport
            )
        },
        bottomBar = {
            Column {
                // Barra de supermercados
                SupermarketBottomBar(
                    supermarkets = uiState.supermarkets,
                    selectedSupermarketId = uiState.selectedSupermarketId ?: 0L,
                    onSupermarketSelected = { viewModel.selectSupermarket(it) }
                )
            }
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
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onNavigateToCatalogo = onNavigateToCatalogo
            )
        } else {
            // Grid de productos agrupados por pasillo
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
                    // Header del pasillo (ocupa 2 columnas)
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

                    // Productos del pasillo (solo si NO está colapsado)
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

    // Diálogo de añadir producto
    if (showAddProductDialog) {
        AddProductToListDialog(
            aisles = uiState.aisles,
            offers = uiState.offers,
            suggestions = uiState.articleSuggestions,
            onSearch = { query -> viewModel.searchArticles(query) },
            onDismiss = { showAddProductDialog = false },
            onAdd = { name, quantity, aisleId, price, offerId, notes ->
                viewModel.addProduct(
                    name = name,
                    quantity = quantity,
                    aisleId = aisleId,
                    price = price,
                    offerId = offerId,
                    notes = notes
                )
                showAddProductDialog = false
            }
        )
    }

    // Diálogo de editar producto
    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            aisles = uiState.aisles,
            offers = uiState.offers,
            onDismiss = { productToEdit = null },
            onSave = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            }
        )
    }

    // Confirmación de eliminar comprados
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar comprados") },
            text = { Text("¿Deseas eliminar ${uiState.purchasedItems} productos comprados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePurchasedProducts()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
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

/**
 * Card de producto con swipe para eliminar
 */
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
            } else {
                false
            }
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = {
            ProductCard(
                product = product,
                offer = offer,
                onClick = onClick,
                onTogglePurchased = onTogglePurchased,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}