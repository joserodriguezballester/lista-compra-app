package com.jose.listacompra.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

// SwipeToDismiss imports
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.theme.ThemeMode
import com.jose.listacompra.ui.viewmodel.ShoppingListViewModel

/**
 * Función helper para realizar vibración de feedback táctil
 * @param context Contexto de Android
 * @param milliseconds Duración de la vibración (por defecto 60ms para feedback sutil)
 * @param isCompletion true para vibración de éxito (más larga y con patrón)
 */
private fun vibrateFeedback(context: Context, milliseconds: Long = 60L, isCompletion: Boolean = false) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Vibración suave para feedback táctil
        val effect = if (isCompletion) {
            // Patrón de éxito: dos vibraciones cortas
            VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 80), -1)
        } else {
            // Vibración simple y sutil
            VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        if (isCompletion) {
            vibrator.vibrate(longArrayOf(0, 50, 100, 80), -1)
        } else {
            vibrator.vibrate(milliseconds)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ShoppingListViewModel = viewModel(),
    currentPrimaryColor: Int = 0xFF4CAF50.toInt(),
    onColorChanged: (Int) -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddProduct by remember { mutableStateOf(false) }
    var showManageAisles by remember { mutableStateOf(false) }
    var showEditProduct by remember { mutableStateOf<Product?>(null) }
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showColorSettings by remember { mutableStateOf(false) }
    
    // Estado previo para detectar cuando se completa toda la lista
    var wasListComplete by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Detectar cuando se completa toda la lista para vibración especial
    LaunchedEffect(uiState.purchasedCount, uiState.totalCount) {
        val isNowComplete = uiState.totalCount > 0 && uiState.purchasedCount == uiState.totalCount
        if (isNowComplete && !wasListComplete) {
            // Lista completada - vibración de éxito
            vibrateFeedback(context, isCompletion = true)
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
            TopAppBar(
                title = { Text("🛒 Lista de Compra") },
                actions = {
                    // Botón de cambio de tema
                    IconButton(onClick = { showThemeMenu = true }) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                else -> Icons.Default.LightMode
                            },
                            contentDescription = "Cambiar tema"
                        )
                    }
                    
                    // Menú de tema
                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🌙 Modo Oscuro") },
                            onClick = {
                                onThemeModeChange(ThemeMode.DARK)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("☀️ Modo Claro") },
                            onClick = {
                                onThemeModeChange(ThemeMode.LIGHT)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚙️ Seguir Sistema") },
                            onClick = {
                                onThemeModeChange(ThemeMode.SYSTEM)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                        
                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("🎨 Cambiar Color") },
                            onClick = {
                                showColorSettings = true
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Palette, contentDescription = null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("🗂️ Gestionar Pasillos") },
                            onClick = {
                                showManageAisles = true
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddProduct = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir producto")
            }
        },
        bottomBar = {
            TotalsBar(
                totalWithOffers = uiState.totalEstimate,
                totalWithoutOffers = uiState.totalWithoutOffers,
                savings = uiState.savings,
                purchasedCount = uiState.purchasedCount,
                totalCount = uiState.totalCount
            )
        }
    ) { padding ->
        // Agrupar productos por pasillo
        val productsByAisle = uiState.products.groupBy { it.aisleId }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
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
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        AisleHeader(
                            aisle = aisle,
                            productCount = aisleProducts.size,
                            purchasedCount = aisleProducts.count { it.isPurchased }
                        )
                    }
                    
                    // Productos del pasillo
                    items(aisleProducts, key = { it.id }) { product ->
                        val offer = uiState.offers.find { it.id == product.offerId }
                        
                        SwipeableProductCard(
                            product = product,
                            offer = offer,
                            onTogglePurchased = {
                                // Feedback táctil al marcar/desmarcar producto
                                vibrateFeedback(context, milliseconds = 60L)
                                viewModel.togglePurchased(product)
                            },
                            onDelete = {
                                viewModel.deleteProduct(product)
                                showSnackbar = "${product.name} eliminado"
                            },
                            onEdit = { showEditProduct = product }
                        )
                    }
                }
            }
            
            // Espacio al final
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) { 
                Spacer(modifier = Modifier.height(80.dp)) 
            }
        }
        
        // Diálogo para añadir producto
        if (showAddProduct) {
            AddProductDialog(
                aisles = uiState.aisles,
                offers = uiState.offers,
                suggestions = uiState.productSuggestions,
                onDismiss = {
                    showAddProduct = false
                    viewModel.clearSuggestions()
                },
                onAdd = { name, aisleId, quantity, price, offerId ->
                    viewModel.addProductWithHistory(name, aisleId, quantity, price, offerId)
                    viewModel.clearSuggestions()
                    showAddProduct = false
                },
                onSearchSuggestions = { query ->
                    viewModel.searchProductSuggestions(query)
                },
                onCalculateOffer = { quantity, price, offerId ->
                    viewModel.calculateOfferPreview(quantity, price, offerId)
                }
            )
        }
        
        // Diálogo para editar producto
        showEditProduct?.let { product ->
            EditProductDialog(
                product = product,
                aisles = uiState.aisles,
                offers = uiState.offers,
                onDismiss = { showEditProduct = null },
                onSave = { name, aisleId, quantity, price, offerId ->
                    viewModel.updateProduct(
                        productId = product.id,
                        name = name,
                        aisleId = aisleId,
                        quantity = quantity,
                        price = price,
                        offerId = offerId
                    )
                    showEditProduct = null
                },
                onCalculateOffer = { quantity, price, offerId ->
                    viewModel.calculateOfferPreview(quantity, price, offerId)
                }
            )
        }
        
        // Diálogo para gestionar pasillos
        if (showManageAisles) {
            ManageAislesDialog(
                aisles = uiState.aisles,
                onDismiss = { showManageAisles = false },
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
        }

        // Diálogo para cambiar color del tema
        if (showColorSettings) {
            ColorSettingsDialog(
                currentColor = currentPrimaryColor,
                onDismiss = { showColorSettings = false },
                onColorSelected = { color ->
                    onColorChanged(color)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableProductCard(
    product: Product,
    offer: Offer?,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.Transparent
                    DismissValue.DismissedToEnd -> Color.Green.copy(alpha = 0.3f)
                    DismissValue.DismissedToStart -> Color.Red.copy(alpha = 0.3f)
                },
                label = "background color"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissContent = {
            ProductCard(
                product = product,
                offer = offer,
                onTogglePurchased = onTogglePurchased,
                onEdit = onEdit
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductCard(
    product: Product,
    offer: Offer?,
    onTogglePurchased: () -> Unit,
    onEdit: () -> Unit
) {
    // Card "fantasma" si está comprado (40% opacidad)
    val alpha = if (product.isPurchased) 0.4f else 1f
    val backgroundColor = if (product.isPurchased) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .combinedClickable(
                onClick = onTogglePurchased,
                onLongClick = onEdit
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (product.isPurchased) 0.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Checkbox + Nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = { onTogglePurchased() },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!product.isPurchased) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                    textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                    color = if (product.isPurchased) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Producto comprado: mostrar solo "(comprado)" sin datos de precio
            if (product.isPurchased) {
                Text(
                    text = "(comprado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 28.dp, top = 4.dp)
                )
            } else {
                // Producto no comprado: mostrar oferta, cantidad, precio, total
                
                // Indicador de oferta (si existe)
                if (offer != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                    ) {
                        Text(
                            text = "🏷️ ",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = offer.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
                
                // 3 columnas: Cantidad | Precio | Total
                if (product.estimatedPrice != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cantidad
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Cant",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${product.quantity.toInt()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Precio unitario
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Precio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "%.2f€".format(product.estimatedPrice),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Total (con oferta aplicada si existe)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "%.2f€".format(product.finalPriceToPay()),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (product.hasOffer()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (product.hasOffer()) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                } else {
                    // Solo cantidad si no hay precio
                    Text(
                        text = "${product.quantity.toInt()} uds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 28.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AisleHeader(
    aisle: Aisle,
    productCount: Int,
    purchasedCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${aisle.emoji} ${aisle.name}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$purchasedCount/$productCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TotalsBar(
    totalWithOffers: Float,
    totalWithoutOffers: Float,
    savings: Float,
    purchasedCount: Int,
    totalCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Fila superior: contador y total principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$purchasedCount de $totalCount productos",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: %.2f€".format(totalWithOffers),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Fila inferior: ahorro (si hay)
            if (savings > 0.01f) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ahorrado: %.2f€ 🎉".format(savings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
