package com.jose.listacompra.ui.screens.productlist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.components.AisleHeader
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.components.ListBottomBar
import com.jose.listacompra.ui.components.ProductCard
import com.jose.listacompra.ui.components.VoiceInputButton
import com.jose.listacompra.ui.screens.ColorSettingsDialog
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
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
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Datos del scanner
    var scannedName by remember { mutableStateOf<String?>(null) }
    var scannedPrice by remember { mutableStateOf<Float?>(null) }
    var scannedAisleId by remember { mutableStateOf<Long?>(null) }
    var scannedImageUrl by remember { mutableStateOf<String?>(null) }
    var scannedCategoryId by remember { mutableStateOf<String?>(null) }

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
            ?.get<String>("scannedImageUrl")?.let { url ->
                scannedImageUrl = url
            }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.get<String>("scannedQuantity")?.let { qty ->
                scannedPrice = qty.toFloatOrNull()
            }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.get<String>("scannedCategoryId")?.let { catId ->
                scannedCategoryId = catId
            }
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
                    // Ya estamos en Mi Lista, solo cerrar drawer
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
                onNavigateToHistory = {
                    scope.launch { drawerState.close() }
                    onNavigateToHistory()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = "Mi lista",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onMicrophoneClick = { showVoiceDialog = true },
                    onAddClick = { showAddProductDialog = true },
                    onChangeColor = { showColorDialog = true },
                    overflowActions = { expanded, onDismiss ->
                        // 📁 Añadir productos
                        DropdownMenuItem(
                            text = { Text("📁 Añadir productos") },
                            onClick = { /* Header, no action */ },
                            enabled = false,
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.primary,
                                disabledTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        DropdownMenuItem(
                            text = { Text("    Manual") },
                            onClick = {
                                showAddProductDialog = true
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("    Scanner") },
                            onClick = {
                                onNavigateToScanner()
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("    Desde historial") },
                            onClick = {
                                // TODO: Placeholder
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.History, contentDescription = null)
                            },
                            enabled = false
                        )

                        HorizontalDivider()

                        // 📁 Lista
                        DropdownMenuItem(
                            text = { Text("📁 Lista") },
                            onClick = { /* Header, no action */ },
                            enabled = false,
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.primary,
                                disabledTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        DropdownMenuItem(
                            text = { Text("    Vaciar") },
                            onClick = {
                                showClearConfirmDialog = true
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                )
            },
            bottomBar = {
                ListBottomBar(
                    supermarkets = uiState.supermarkets,
                    selectedSupermarketId = uiState.selectedSupermarketId,
                    onSupermarketSelected = { viewModel.selectSupermarket(it) },
                    onHomeClick = onNavigateToHome
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
                                    Log.d(
                                        "ProductListScreen",
                                        "🔍 ${product.name}: offerId=${product.offerId}, found offer=${offer?.name}, total offers=${uiState.offers.size}"
                                    )
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

        // Resumen de totales
        if (uiState.productsByAisle.isNotEmpty()) {
            val allProducts = uiState.productsByAisle.flatMap { it.value }
            val purchasedProducts = allProducts.filter { it.isPurchased }
            val totalProducts = allProducts.size
            val purchasedCount = purchasedProducts.size

            // Calcular totales
            val purchasedTotal = purchasedProducts.sumOf { product ->
                val offer = uiState.offers.find { it.id == product.offerId }
                val unitPrice = product.finalPrice ?: product.estimatedPrice ?: 0f
                val finalPrice = when {
                    offer != null -> calculateOfferPrice(unitPrice, product.quantity, offer)
                    else -> unitPrice * product.quantity
                }
                finalPrice.toDouble()
            }

            val listTotal = allProducts.sumOf { product ->
                val offer = uiState.offers.find { it.id == product.offerId }
                val unitPrice = product.finalPrice ?: product.estimatedPrice ?: 0f
                val finalPrice = when {
                    offer != null -> calculateOfferPrice(unitPrice, product.quantity, offer)
                    else -> unitPrice * product.quantity
                }
                finalPrice.toDouble()
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Comprados: $purchasedCount/$totalProducts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (purchasedCount > 0) {
                            Text(
                                text = "Llevas: €${String.format("%.2f", purchasedTotal)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Total: €${String.format("%.2f", listTotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Diálogo añadir
    if (showAddProductDialog) {
        AddProductToListDialog(
            aisles = uiState.aisles,
            offers = uiState.offers,
            supermarkets = uiState.supermarkets, // T4
            suggestions = uiState.articleSuggestions,
            initialName = scannedName,
            initialImageUrl = scannedImageUrl,
            initialCategoryId = scannedCategoryId?.toLongOrNull(),
            initialQuantity = scannedPrice?.toString(),
            onSearch = { query -> viewModel.searchArticles(query) },
            onOpenScanner = onNavigateToScanner,
            onDismiss = {
                showAddProductDialog = false
                scannedName = null
                scannedPrice = null
                scannedImageUrl = null
                scannedCategoryId = null
                // Limpiar savedStateHandle
                navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedName")
                navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedImageUrl")
                navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedQuantity")
                navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedCategoryId")
            },
            onAdd = { name, quantity, aisleId, price, offerId, notes, photoUri, supermarketId -> // T4
                viewModel.addProduct(
                    name,
                    quantity,
                    aisleId,
                    price,
                    offerId,
                    notes,
                    photoUri,
                    supermarketId
                )
                showAddProductDialog = false
                scannedName = null
                scannedPrice = null
                scannedImageUrl = null
                scannedCategoryId = null
            }
        )
    }

    // Diálogo editar
    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            aisles = uiState.aisles,
            offers = uiState.offers,
            supermarkets = uiState.supermarkets,
            onDismiss = { productToEdit = null },
            onSave = { updatedProduct, _ ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            },
            onDelete = {
                //        viewModel.deleteProduct(product)
                productToEdit = null
            }
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

    // Diálogo confirmar vaciar lista
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Vaciar lista") },
            text = { Text("¿Estás seguro de que quieres eliminar todos los productos de la lista?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllProducts()
                        showClearConfirmDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = {
            ProductCard(product, offer, onClick, onTogglePurchased, Modifier.fillMaxWidth())
        }
    )
}

/**
 * Calcula el precio final aplicando una oferta
 */
private fun calculateOfferPrice(
    unitPrice: Float,
    quantity: Float,
    offer: com.jose.listacompra.domain.model.Offer
): Float {
    return when (offer.code) {
        "3x2" -> unitPrice * (quantity.toInt() / 3 * 2 + quantity.toInt() % 3)
        "2x1" -> unitPrice * (quantity.toInt() / 2 + quantity.toInt() % 2)
        "2nd_50" -> {
            val fullPrice = quantity.toInt() / 2 * unitPrice
            val halfPrice = quantity.toInt() / 2 * unitPrice * 0.5f
            fullPrice + halfPrice + (quantity.toInt() % 2) * unitPrice
        }

        "2nd_70" -> {
            val fullPrice = quantity.toInt() / 2 * unitPrice
            val discountedPrice = quantity.toInt() / 2 * unitPrice * 0.3f
            fullPrice + discountedPrice + (quantity.toInt() % 2) * unitPrice
        }

        "4x3" -> unitPrice * (quantity.toInt() / 4 * 3 + quantity.toInt() % 4)
        else -> unitPrice * quantity
    }
}