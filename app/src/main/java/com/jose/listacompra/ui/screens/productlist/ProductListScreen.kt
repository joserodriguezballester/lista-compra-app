package com.jose.listacompra.ui.screens.productlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.components.SupermarketBottomBar
import com.jose.listacompra.ui.screens.main.components.AisleHeader
import com.jose.listacompra.ui.screens.main.components.TotalsBar
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
                // Totales
                TotalsBar(
                    total = uiState.products.sumOf { it.finalPrice?.toDouble() ?: 0.0 }.toFloat(),
                    savings = uiState.products.sumOf { it.savings().toDouble() }.toFloat()
                )
                
                // Barra de supermercados
                SupermarketBottomBar(
                    supermarkets = uiState.supermarkets,
                    selectedSupermarketId = uiState.selectedSupermarketId,
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
        } else if (uiState.products.isEmpty()) {
            // Empty state
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
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.productsByAisle.forEach { (aisle, products) ->
                    // Header del pasillo
                    item {
                        AisleHeader(
                            aisleName = aisle.name,
                            aisleEmoji = aisle.emoji,
                            itemCount = products.size
                        )
                    }
                    
                    // Productos del pasillo
                    items(items = products, key = { it.id }) { product ->
                        ProductListItem(
                            product = product,
                            onTogglePurchased = { viewModel.togglePurchased(product) },
                            onEdit = { productToEdit = product },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                    
                    // Espacio entre pasillos
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Diálogo de añadir producto
    if (showAddProductDialog) {
        AddProductDialog(
            supermarketId = uiState.selectedSupermarketId,
            aisles = uiState.aisles,
            onDismiss = { showAddProductDialog = false },
            onAdd = { product ->
                viewModel.addProduct(product)
                showAddProductDialog = false
            }
        )
    }
    
    // Diálogo de editar producto
    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            aisles = uiState.aisles,
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
            text = { Text("¿Deseas eliminar ${uiState.purchasedProducts.size} productos comprados?") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductListItem(
    product: Product,
    onTogglePurchased: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (product.isPurchased) 0.5f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isPurchased)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = product.isPurchased,
                onCheckedChange = { onTogglePurchased() },
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Info del producto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(animatedAlpha)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                    fontWeight = if (product.isPurchased) FontWeight.Normal else FontWeight.Medium
                )
                
                if (product.notes.isNotEmpty()) {
                    Text(
                        text = product.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Cantidad y precio
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${product.quantity.toInt()} ud",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = if (product.finalPrice != null) {
                        "${String.format("%.2f", product.finalPrice)} €"
                    } else {
                        "-- €"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Diálogos placeholder (se pueden expandir después)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductDialog(
    supermarketId: Long,
    aisles: List<Aisle>,
    onDismiss: () -> Unit,
    onAdd: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var selectedAisleId by remember { mutableStateOf(aisles.firstOrNull()?.id ?: 1L) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de pasillo
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = aisles.find { it.id == selectedAisleId }?.name ?: "Seleccionar pasillo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = {
                                    selectedAisleId = aisle.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            Product(
                                name = name,
                                quantity = quantity.toFloatOrNull() ?: 1f,
                                aisleId = selectedAisleId,
                                supermarketId = supermarketId,
                                shoppingListId = 1
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductDialog(
    product: Product,
    aisles: List<Aisle>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var selectedAisleId by remember { mutableStateOf(product.aisleId) }
    var notes by remember { mutableStateOf(product.notes) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de pasillo
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = aisles.find { it.id == selectedAisleId }?.name ?: "Seleccionar pasillo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = {
                                    selectedAisleId = aisle.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        product.copy(
                            name = name,
                            quantity = quantity.toFloatOrNull() ?: product.quantity,
                            aisleId = selectedAisleId,
                            notes = notes
                        )
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
