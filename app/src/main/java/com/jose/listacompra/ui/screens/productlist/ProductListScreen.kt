package com.jose.listacompra.ui.screens.productlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                TotalsBar(
                    total = uiState.products.sumOf { it.finalPrice?.toDouble() ?: 0.0 }.toFloat(),
                    savings = uiState.products.sumOf { it.savings().toDouble() }.toFloat()
                )
                
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
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Añadir producto") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.products.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ShoppingCart, null, Modifier.size(80.dp), MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                Text("Tu lista está vacía", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                TextButton(onNavigateToCatalogo) { Text("Ir al catálogo") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.productsByAisle.forEach { (aisle, products) ->
                    item {
                        AisleHeader(
                            aisle = aisle,
                            productCount = products.size,
                            purchasedCount = products.count { it.isPurchased }
                        )
                    }
                    
                    items(products, { it.id }) { product ->
                        ProductListItem(
                            product = product,
                            onTogglePurchased = { viewModel.togglePurchased(product) },
                            onEdit = { productToEdit = product },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                    
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
    
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
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar comprados") },
            text = { Text("¿Deseas eliminar ${uiState.purchasedProducts.size} productos comprados?") },
            confirmButton = {
                TextButton({
                    viewModel.deletePurchasedProducts()
                    showDeleteConfirm = false
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton({ showDeleteConfirm = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProductListItem(
    product: Product,
    onTogglePurchased: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = if (product.isPurchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(product.isPurchased, { onTogglePurchased() }, Modifier.padding(end = 12.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                    fontWeight = if (product.isPurchased) FontWeight.Normal else FontWeight.Medium
                )
                if (product.notes.isNotEmpty()) {
                    Text(product.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${product.quantity.toInt()} ud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (product.finalPrice != null) String.format("%.2f €", product.finalPrice) else "-- €",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

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
                OutlinedTextField(name, { name = it }, { Text("Nombre") }, Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(quantity, { quantity = it }, { Text("Cantidad") }, Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded, { expanded = it }, Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        aisles.find { it.id == selectedAisleId }?.name ?: "Seleccionar pasillo",
                        {}, { Text("Pasillo") },
                        Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem({ Text("${aisle.emoji} ${aisle.name}") }, { selectedAisleId = aisle.id; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button({
                if (name.isNotBlank()) {
                    onAdd(Product(name = name, quantity = quantity.toFloatOrNull() ?: 1f, aisleId = selectedAisleId, supermarketId = supermarketId, shoppingListId = 1))
                }
            }, enabled = name.isNotBlank()) { Text("Añadir") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
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
                OutlinedTextField(name, { name = it }, { Text("Nombre") }, Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(quantity, { quantity = it }, { Text("Cantidad") }, Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, { Text("Notas") }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded, { expanded = it }, Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        aisles.find { it.id == selectedAisleId }?.name ?: "Seleccionar pasillo",
                        {}, { Text("Pasillo") },
                        Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem({ Text("${aisle.emoji} ${aisle.name}") }, { selectedAisleId = aisle.id; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { Button({ onSave(product.copy(name = name, quantity = quantity.toFloatOrNull() ?: product.quantity, aisleId = selectedAisleId, notes = notes)) }) { Text("Guardar") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}
