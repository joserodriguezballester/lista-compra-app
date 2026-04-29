package com.jose.listacompra.ui.screens.productlist

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.components.AisleHeader
import com.jose.listacompra.ui.components.AppDrawerScaffold
import com.jose.listacompra.ui.components.ListBottomBar
import com.jose.listacompra.ui.components.ProductCard
import com.jose.listacompra.ui.components.VoiceInputButton
import com.jose.listacompra.ui.components.startDirectVoiceRecognition
import com.jose.listacompra.ui.navigation.AppNavigator
import com.jose.listacompra.ui.navigation.DrawerDestination
import com.jose.listacompra.ui.screens.ColorSettingsDialog
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
import com.jose.listacompra.utils.calculateOfferPrice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    navigator: AppNavigator,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    navController: NavController? = null,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estados para diálogos
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) } // T7: Reset a producción

    // Datos del scanner
    var scannedName by remember { mutableStateOf<String?>(null) }
    var scannedPrice by remember { mutableStateOf<Float?>(null) }
    var scannedImageUrl by remember { mutableStateOf<String?>(null) }
    var scannedCategoryId by remember { mutableStateOf<String?>(null) }

    // Color actual
    val currentColor by viewModel.primaryColor.collectAsState(initial = 0)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val message = viewModel.exportBackupToUri(context, it)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportConfirmDialog = true
        }
    }

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

    AppDrawerScaffold(
        title = "Mi lista",
        navigator = navigator,
        currentDestination = DrawerDestination.ShoppingList,
        onMicrophoneClick = { context, scope -> startDirectVoiceRecognition(context, viewModel, scope) },
        onAddClick = { showAddProductDialog = true },
        onChangeColor = { showColorDialog = true },
        overflowActions = { _, onDismiss ->
            DropdownMenuItem(text = { Text("📁 Añadir productos") }, onClick = { }, enabled = false, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.primary, disabledTextColor = MaterialTheme.colorScheme.primary))
            DropdownMenuItem(text = { Text("    Manual") }, onClick = { showAddProductDialog = true; onDismiss() }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) })
            DropdownMenuItem(text = { Text("    Scanner") }, onClick = { onNavigateToScanner(); onDismiss() }, leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) })
            DropdownMenuItem(text = { Text("    Desde historial") }, onClick = { onDismiss() }, leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }, enabled = false)
            HorizontalDivider()
            DropdownMenuItem(text = { Text("📁 Lista") }, onClick = { }, enabled = false, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.primary, disabledTextColor = MaterialTheme.colorScheme.primary))
            DropdownMenuItem(text = { Text("    Vaciar") }, onClick = { showClearConfirmDialog = true; onDismiss() }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error))
            HorizontalDivider()
            DropdownMenuItem(text = { Text("📁 Datos") }, onClick = { }, enabled = false, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.primary, disabledTextColor = MaterialTheme.colorScheme.primary))
            DropdownMenuItem(text = { Text("    Exportar datos") }, onClick = { exportBackupLauncher.launch("lista-compra-backup.json"); onDismiss() })
            DropdownMenuItem(text = { Text("    Importar datos") }, onClick = { importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")); onDismiss() })
            DropdownMenuItem(text = { Text("    Limpiar datos") }, onClick = { showResetConfirmDialog = true; onDismiss() }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error))
        },
        bottomBar = {
            if (uiState.productsByAisle.isNotEmpty()) {
                val allProducts = uiState.productsByAisle.flatMap { it.value }
                val purchasedProducts = allProducts.filter { it.isPurchased }
                val totalProducts = allProducts.size
                val purchasedCount = purchasedProducts.size
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
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = "Comprados: $purchasedCount/$totalProducts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            if (purchasedCount > 0) {
                                Text(text = "Llevas: €${String.format("%.2f", purchasedTotal)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(text = "Total: €${String.format("%.2f", listTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            ListBottomBar(supermarkets = uiState.supermarkets, selectedSupermarketId = uiState.selectedSupermarketId, onSupermarketSelected = viewModel::selectSupermarket, onHomeClick = onNavigateToHome)
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.productsByAisle.forEach { (aisle, products) ->
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        AisleHeader(
                            aisleName = aisle.name,
                            aisleIcon = aisle.emoji,
                            productCount = products.size,
                            purchasedCount = products.count { it.isPurchased },
                            isCollapsed = false,
                            onToggle = {}
                        )
                    }
                    items(products, key = { it.id }) { product ->
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

    // Diálogo añadir
    if (showAddProductDialog) {
        AddProductToListDialog(
            aisles = uiState.aisles,
            offers = uiState.offers,
            supermarkets = uiState.supermarkets, // T4
            suggestions = uiState.articleSuggestions,
            articleDefaultAisleIds = uiState.articleDefaultAisleIds,
            initialName = scannedName,
            initialImageUrl = scannedImageUrl,
            initialCategoryId = scannedCategoryId?.toLongOrNull(),
            initialQuantity = scannedPrice?.toString(),
            initialSupermarketId = uiState.selectedSupermarketId
                ?: uiState.supermarkets.firstOrNull { it.isDefault }?.id
                ?: uiState.supermarkets.firstOrNull { it.id > 0 }?.id
                ?: 0L,
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
            onAdd = { name, quantity, aisleId, price, offerId, notes, photoUri, supermarketId, articuloId -> // T4
                viewModel.addProduct(
                    name,
                    quantity,
                    aisleId,
                    price,
                    offerId,
                    notes,
                    photoUri,
                    supermarketId,
                    articuloId
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

    // T7: Diálogo de confirmación para reset a producción
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Limpiar datos") },
            text = {
                Text("¿Eliminar todos los datos de usuario?\n\nSe mantendrán:\n• Supermercados\n• Categorías\n• Pasillos Carrefour\n• Ofertas por defecto\n\nSe eliminarán:\n• Artículos creados\n• Productos en listas\n• Historial de precios\n• Compras anteriores")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDataToProduction {
                            showResetConfirmDialog = false
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Limpiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showImportConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportUri = null
            },
            title = { Text("Importar datos") },
            text = {
                Text("Se reemplazarán los datos exportados/importados por el contenido del backup seleccionado. ¿Quieres continuar?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingImportUri
                        showImportConfirmDialog = false
                        pendingImportUri = null
                        if (uri != null) {
                            scope.launch {
                                val message = viewModel.importBackupFromUri(context, uri)
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
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
