package com.jose.listacompra.ui.screens.main


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.components.VoiceInputButton
import com.jose.listacompra.ui.navigation.DialogType
import com.jose.listacompra.ui.screens.AddProductDialog
import com.jose.listacompra.ui.screens.BarcodeScannerScreen
import com.jose.listacompra.ui.screens.ColorSettingsDialog
import com.jose.listacompra.ui.screens.EditProductDialog
import com.jose.listacompra.ui.screens.ImportTicketScreen
import com.jose.listacompra.ui.screens.ManageAislesDialog
import com.jose.listacompra.ui.screens.ProductHistoryScreen
import com.jose.listacompra.ui.screens.main.components.AisleHeader
import com.jose.listacompra.ui.screens.main.components.SwipeableProductCard
import com.jose.listacompra.ui.screens.main.components.TotalsBar
import com.jose.listacompra.ui.viewmodel.ShoppingListViewModel
import com.jose.listacompra.utils.vibrateFeedback

enum class Screen {
    SHOPPING_LIST,
    ARTICULOS_CATALOGO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
    currentPrimaryColor: Int = 0xFF4CAF50.toInt(),
    onColorChanged: (Int) -> Unit = {},
    onNavigateToLists: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onClearList: (Boolean) -> Unit = {}
) {
    // 1. Estado de la pestaña activa
    var currentScreen by remember { mutableStateOf(Screen.SHOPPING_LIST) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<DialogType?>(null) }
    // Estado previo para detectar cuando se completa toda la lista
    var wasListComplete by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Detectar cuando se completa toda la lista para vibración especial
    LaunchedEffect(uiState.purchasedCount, uiState.totalCount) {
        val isNowComplete = uiState.totalCount > 0 && uiState.purchasedCount == uiState.totalCount
        if (isNowComplete && !wasListComplete) {
            // Lista completada - vibración de éxito
            context.vibrateFeedback(context, isCompletion = true)
        }
        wasListComplete = isNowComplete
    }

    // Mostrar snackbar cuando hay mensaje
    LaunchedEffect(showSnackbar) {
        showSnackbar?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "DESHACER"
            )
            if (result == SnackbarResult.ActionPerformed) {
                // TODO: Implementar deshacer
            }
            showSnackbar = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Solo mostramos la TopBar de la lista si estamos en esa pestaña
            if (currentScreen == Screen.SHOPPING_LIST) {
                TopAppBar(
                    title = {
                        Column {
                            Text("🛒 ${uiState.currentList?.name ?: "Lista de Compra"}")
                            if (uiState.totalCount > 0) {
                                Text(
                                    text = "${uiState.purchasedCount}/${uiState.totalCount} productos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Botón de entrada por voz
                        VoiceInputButton(
                            onVoiceCommand = { command ->
                                // Usar el primer pasillo como default, o 1L si no hay pasillos
                                val defaultAisleId = uiState.aisles.firstOrNull()?.id ?: 1L
                                // Crear producto desde comando de voz
                                viewModel.addProduct(
                                    name = "${command.productName} (${command.quantity.toInt()} ${command.unit})",
                                    quantity = command.quantity,
                                    price = null
                                )
                            }
                        )

                        // Botón de menú de opciones
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Menú opciones"
                            )
                        }

                        // Menú de tema
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { activeDialog = null }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📋 Mis Listas") },
                                onClick = {
                                    showThemeMenu = false
                                    activeDialog = null
                                    onNavigateToLists()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.List, contentDescription = null)
                                }
                            )
                            Divider()
                            // Botón para alternar tema oscuro/claro
                            DropdownMenuItem(
                                text = { Text("🌙☀️ Cambiar Modo Oscuro/Claro") },
                                onClick = {
                                    onToggleTheme()
                                    showThemeMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🎨 Cambiar Color") },
                                onClick = {
                                    activeDialog = DialogType.ShowColorSettings
                                    showThemeMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Palette, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗂️ Gestionar Pasillos") },
                                onClick = {
                                    activeDialog = DialogType.ManageAisles
                                    showThemeMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.MoreVert, contentDescription = null)
                                }
                            )
                            Divider()
                            // Importar ticket PDF
                            DropdownMenuItem(
                                text = { Text("📄 Importar Ticket PDF") },
                                onClick = {
                                    activeDialog = DialogType.ImportTicket
                                    showThemeMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.List, contentDescription = null)
                                }
                            )
                            Divider()
                            // Opciones para limpiar lista
                            DropdownMenuItem(
                                text = { Text("🧹 Quitar Comprados") },
                                onClick = {
                                    onClearList(false) // false = solo comprados
                                    showThemeMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Vaciar Lista") },
                                onClick = {
                                    onClearList(true) // true = todo
                                    showThemeMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            )
                        }
                    }
                )
            } else {
                TopAppBar(title = { Text("📦 Mi Catálogo de Artículos") })
            }
        },
// 2. LA BARRA INFERIOR
        bottomBar = {
            Column {
                // Si estamos en la lista, mostramos la barra de totales encima de la nav
                if (currentScreen == Screen.SHOPPING_LIST) {

                    TotalsBar(
                        totalWithOffers = uiState.totalEstimate,
                        totalWithoutOffers = uiState.totalWithoutOffers,
                        savings = uiState.savings,
                        purchasedCount = uiState.purchasedCount,
                        totalCount = uiState.totalCount
                    )
                }


                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == Screen.SHOPPING_LIST,
                        onClick = { currentScreen = Screen.SHOPPING_LIST },
                        label = { Text("Mi Lista") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.ARTICULOS_CATALOGO,
                        onClick = { currentScreen = Screen.ARTICULOS_CATALOGO },
                        label = { Text("Artículos") },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Solo mostramos el FAB de añadir si estamos en la lista
            if (currentScreen == Screen.SHOPPING_LIST) {
                // Menú desplegable de opciones para añadir producto
                var showAddMenu by remember { mutableStateOf(false) }
                Box {
                    // Botón principal FAB
                    FloatingActionButton(
                        onClick = { showAddMenu = !showAddMenu }
                    ) {
                        Icon(
                            imageVector = if (showAddMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showAddMenu) "Cerrar menú" else "Añadir producto"
                        )
                    }

                    // Menú desplegable con 3 opciones
                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                        modifier = Modifier.Companion.padding(bottom = 8.dp)
                    ) {
                        // Opción 1: Por Voz
                        DropdownMenuItem(
                            text = { Text("🎙️ Por Voz") },
                            onClick = {
                                showAddMenu = false
                                // TODO: Abrir diálogo de voz
                                activeDialog =
                                    DialogType.AddProduct  // Por ahora, usamos el diálogo actual
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Mic, contentDescription = null)
                            }
                        )

                        // Opción 2: Escribir Nombre
                        DropdownMenuItem(
                            text = { Text("⌨️ Escribir Nombre") },
                            onClick = {
                                activeDialog = DialogType.AddProduct
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )

                        // Opción 3: Desde Historial
                        DropdownMenuItem(
                            text = { Text("📋 Desde Historial") },
                            onClick = {
                                //   showAddMenu = false
                                activeDialog =
                                    DialogType.ShowProductHistory // Abrir pantalla de historial
                            },
                            leadingIcon = {
                                Icon(Icons.Default.List, contentDescription = null)
                            }
                        )

                        // Opción 4: Escanear Código de Barras
                        DropdownMenuItem(
                            text = { Text("📷 Escanear Código") },
                            onClick = {
                                // showAddMenu = false
                                activeDialog =
                                    DialogType.ShowBarcodeScanner // Abrir pantalla de escáner
                            },
                            leadingIcon = {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            }
                        )
                    }
                }
            }
        },
//
    ) { padding ->
        // Agrupar productos por pasillo
        val productsByAisle = uiState.products.groupBy { it.aisleId }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.aisles.forEach { aisle ->
                val aisleProducts = productsByAisle[aisle.id] ?: emptyList()

                if (aisleProducts.isNotEmpty()) {
                    // Header del pasillo (ocupa 2 columnas)
                    item(span = { GridItemSpan(2) }) {
                        AisleHeader(
                            aisle = aisle,
                            productCount = aisleProducts.size,
                            purchasedCount = aisleProducts.count { it.isPurchased }
                        )
                    }

                    // Productos del pasillo
                    items(
                        aisleProducts.size,
                        key = { index -> aisleProducts[index].id }) { index ->
                        val product = aisleProducts[index]
                        val offer = uiState.offers.find { it.id == product.offerId }

                        SwipeableProductCard(
                            product = product,
                            offer = offer,
                            onTogglePurchased = {
                                // Feedback táctil al marcar/desmarcar producto
                                context.vibrateFeedback(context, milliseconds = 60L)
                                viewModel.togglePurchased(product)
                            },
                            onDelete = {
                                viewModel.deleteProduct(product)
                                showSnackbar = "${product.name} eliminado"
                            },
                            onEdit = { activeDialog = DialogType.EditProduct(product) }
                        )
                    }
                }
            }

            // Espacio al final
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.Companion.height(80.dp))
            }
        }


        when (activeDialog) {
            // Diálogo para añadir producto
            is DialogType.AddProduct ->
                AddProductDialog(
                    aisles = uiState.aisles,
                    offers = uiState.offers,
                    suggestions = uiState.productSuggestions,
                    onDismiss = {
                        activeDialog = null
                        viewModel.clearSuggestions()
                    },
                    onAdd = { name, aisleId, quantity, price, offerId ->
                        viewModel.addProductWithHistory(name, aisleId, quantity, price, offerId)
                        //     viewModel.addProductFromHistory(name,  quantity,)
                        viewModel.clearSuggestions()
                        activeDialog = null
                    },
                    onSearchSuggestions = { query ->
                        viewModel.searchProductSuggestions(query)
                    },
                    onCalculateOffer = { quantity, price, offerId ->
                        viewModel.calculateOfferPreview(quantity, price, offerId)
                    }
                )

            // Diálogo para editar producto
            is DialogType.EditProduct ->
                EditProductDialog(
                    product = (activeDialog as DialogType.EditProduct).product,
                    aisles = uiState.aisles,
                    offers = uiState.offers,
                    onDismiss = { activeDialog = null },
                    onSave = { name, aisleId, quantity, price, offerId, photoUri ->
                        viewModel.updateProduct(
                            productId = (activeDialog as DialogType.EditProduct).product.id,
                            name = name,
                            aisleId = aisleId,
                            quantity = quantity,
                            price = price,
                            offerId = offerId,
                            photoUri = photoUri
                        )
                        activeDialog = null
                    },
                    onCalculateOffer = { quantity, price, offerId ->
                        viewModel.calculateOfferPreview(quantity, price, offerId)
                    }
                )

            // Diálogo para gestionar pasillos
            is DialogType.ManageAisles ->
                ManageAislesDialog(
                    aisles = uiState.aisles,
                    onDismiss = { activeDialog = null },
                    onAddAisle = { name, emoji ->
                        viewModel.addAisle(name, emoji)
                    },
                    onDeleteAisle = { aisle ->
                        viewModel.deleteAisle(aisle)
                    },
                    onReorderAisles = { reorderedAisles ->
                        viewModel.reorderAisles(reorderedAisles)
                    }
                )

            // Diálogo para cambiar color del tema
            is DialogType.ShowColorSettings ->
                ColorSettingsDialog(
                    currentColor = currentPrimaryColor,
                    onDismiss = { activeDialog = null },
                    onColorSelected = { color ->
                        onColorChanged(color)
                    }
                )

            // Pantalla de historial de productos
            is DialogType.ShowProductHistory ->
                ProductHistoryScreen(
                    onProductSelected = { historicalProduct ->
                        // Añadir producto desde historial
                        val defaultAisleId = uiState.aisles.firstOrNull()?.id ?: 1L
                        viewModel.addProduct(
                            name = historicalProduct.name,
                            //    aisleId = defaultAisleId,
                            quantity = 1f,
                            price = historicalProduct.price
                        )
                        activeDialog = null
                        showSnackbar = "Añadido: ${historicalProduct.name}"
                    },
                    onNavigateBack = { activeDialog = null }
                )

            // Pantalla de importar ticket PDF
            is DialogType.ImportTicket ->
                ImportTicketScreen(
                    onProductsImported = { products, total, tienda, ahorro ->
                        // Añadir productos del ticket a la lista
                        val defaultAisleId = uiState.aisles.firstOrNull()?.id ?: 1L
                        products.forEach { product ->
                            viewModel.addProduct(
                                name = product.name,
                                //       aisleId = defaultAisleId,
                                quantity = product.quantity.toFloat(),
                                price = product.price
                            )
                        }

                        // Guardar en historial de compras para análisis
                        val productData = products.map {
                            Triple(it.name, it.price, null as String?)
                        }
                        viewModel.savePurchaseFromTicket(
                            total = total,
                            numProductos = products.size,
                            tienda = tienda,
                            ahorro = ahorro,
                            products = productData
                        )

                        activeDialog = null
                        showSnackbar = "Añadidos ${products.size} productos y guardado en historial"
                    },
                    onNavigateBack = { activeDialog = null }
                )
            // Pantalla de escáner de código de barras
            is DialogType.ShowBarcodeScanner ->
                BarcodeScannerScreen(
                    onProductScanned = { scannedProduct ->
                        // Añadir producto escaneado
                        val defaultAisleId = uiState.aisles.firstOrNull()?.id ?: 1L
                        viewModel.addProduct(
                            name = scannedProduct.name
                                ?: "Producto ${scannedProduct.barcode.takeLast(4)}",
                            //     aisleId = defaultAisleId,
                            quantity = 1f,
                            price = null
                        )
                        activeDialog = null
                        showSnackbar = "Añadido: ${scannedProduct.name ?: "Producto escaneado"}"
                    },
                    onNavigateBack = { activeDialog = null }
                )

            null -> {}
        }
    }
}


