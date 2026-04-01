package com.jose.listacompra.ui.screens.productlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            // Lista de productos agrupados por pasillo
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.productsByAisle.forEach { (aisle, products) ->
                    // Header del pasillo
                    item {
                        AisleHeader(
                            aisleName = aisle.name,
                            aisleIcon = aisle.emoji,
                            productCount = products.size,
                            purchasedCount = products.count { it.isPurchased }
                        )
                    }

                    // Productos del pasillo con ProductCard
                    items(items = products, key = { it.id }) { product ->
                        val offer = uiState.offers.find { it.id == product.offerId }
                        ProductCard(
                            product = product,
                            offer = offer,
                            onClick = { productToEdit = product },
                            onTogglePurchased = { viewModel.toggleProductPurchased(product) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Espacio entre pasillos
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
            onAdd = { name, quantity, aisleId, price, offerId ->
                viewModel.addProduct(
                    name = name,
                    quantity = quantity,
                    aisleId = aisleId,
                    price = price,
                    offerId = offerId
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
