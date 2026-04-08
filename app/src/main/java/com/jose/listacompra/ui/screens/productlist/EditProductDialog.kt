package com.jose.listacompra.ui.screens.productlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    aisles: List<com.jose.listacompra.domain.model.Aisle>,
    offers: List<Offer>,
    supermarkets: List<com.jose.listacompra.domain.model.Supermarket> = emptyList(), // T4
    onSave: (Product, Uri?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToScanner: () -> Unit = {}
) {
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var price by remember { mutableStateOf(product.estimatedPrice?.toString() ?: "") }
    var notes by remember { mutableStateOf(product.notes ?: "") }
    var selectedAisleId by remember { mutableStateOf(product.aisleId) }
    var selectedOfferId by remember { mutableStateOf(product.offerId) }
    var selectedSupermarketId by remember { mutableStateOf(product.supermarketId) } // T4
    var photoUri by remember { mutableStateOf<Uri?>(product.photoUri?.let { Uri.parse(it) }) }
    
    var showAisleDropdown by remember { mutableStateOf(false) }
    var showOfferDropdown by remember { mutableStateOf(false) }
    var showSupermarketDropdown by remember { mutableStateOf(false) } // T4
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar imagen desde galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { photoUri = it }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre + Scanner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Botón Scanner (B4)
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(Icons.Default.QrCodeScanner, "Escanear código")
                    }
                }
                
                // Cantidad y precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio €") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                
                // Pasillo
                ExposedDropdownMenuBox(
                    expanded = showAisleDropdown,
                    onExpandedChange = { showAisleDropdown = it }
                ) {
                    OutlinedTextField(
                        value = aisles.find { it.id == selectedAisleId }?.name ?: "Sin pasillo",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAisleDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showAisleDropdown,
                        onDismissRequest = { showAisleDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin pasillo") }, /////// selectedAisleId = 1 NONULL
                            onClick = { selectedAisleId = 1; showAisleDropdown = false }
                        )
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = { selectedAisleId = aisle.id; showAisleDropdown = false }
                            )
                        }
                    }
                }
                
                // T4: Supermercado
                ExposedDropdownMenuBox(
                    expanded = showSupermarketDropdown,
                    onExpandedChange = { showSupermarketDropdown = it }
                ) {
                    val selectedSupermarket = supermarkets.find { it.id == selectedSupermarketId }
                    val displayValue = when {
                        selectedSupermarketId == 0L -> "📦 Cualquiera"
                        selectedSupermarket != null -> "${selectedSupermarket.emoji} ${selectedSupermarket.name}"
                        else -> "📦 Cualquiera"
                    }
                    OutlinedTextField(
                        value = displayValue,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Supermercado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSupermarketDropdown) },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showSupermarketDropdown,
                        onDismissRequest = { showSupermarketDropdown = false }
                    ) {
                        // Opción "Cualquiera"
                        DropdownMenuItem(
                            text = { Text("📦 Cualquiera") },
                            onClick = { selectedSupermarketId = 0L; showSupermarketDropdown = false }
                        )
                        // Supermercados específicos
                        supermarkets.filter { it.id > 0 }.forEach { supermarket ->
                            DropdownMenuItem(
                                text = { Text("${supermarket.emoji} ${supermarket.name}") },
                                onClick = { selectedSupermarketId = supermarket.id; showSupermarketDropdown = false }
                            )
                        }
                    }
                }
                
                // Oferta
                ExposedDropdownMenuBox(
                    expanded = showOfferDropdown,
                    onExpandedChange = { showOfferDropdown = it }
                ) {
                    OutlinedTextField(
                        value = offers.find { it.id == selectedOfferId }?.let { "${it.code} - ${it.name}" } ?: "Sin oferta",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Oferta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOfferDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showOfferDropdown,
                        onDismissRequest = { showOfferDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = { selectedOfferId = null; showOfferDropdown = false }
                        )
                        offers.forEach { offer ->
                            DropdownMenuItem(
                                text = { Text("${offer.code} - ${offer.name}") },
                                onClick = { selectedOfferId = offer.id; showOfferDropdown = false }
                            )
                        }
                    }
                }
                
                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Imagen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Imagen:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showImageSourceDialog = true }) {
                            Icon(Icons.Default.Image, "Seleccionar imagen")
                        }
                        if (photoUri != null) {
                            IconButton(onClick = { photoUri = null }) {
                                Icon(Icons.Default.Close, "Quitar imagen")
                            }
                        }
                    }
                }
                
                // Preview de imagen
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Imagen del producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón Eliminar
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
                
                // Botón Guardar
                Button(
                    onClick = {
                        val updatedProduct = product.copy(
                            name = name,
                            quantity = quantity.toFloatOrNull() ?: 1f,
                            aisleId = selectedAisleId,
                            supermarketId = selectedSupermarketId, // T4
                            estimatedPrice = price.toFloatOrNull(),
                            offerId = selectedOfferId,
                            notes = notes.ifBlank { "" } // O "Sin notas", etc.  notes = notes.ifBlank { null }
                        )
                        onSave(updatedProduct, photoUri)
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Diálogo para seleccionar origen de imagen
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿Desde dónde quieres obtener la imagen?") },
            confirmButton = {
                Button(onClick = {
                    showImageSourceDialog = false
                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Galería")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        onNavigateToScanner()
                    }) {
                        Text("Scanner")
                    }
                    TextButton(onClick = { showImageSourceDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
    
    // Confirmación de eliminar
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar \"${product.name}\" de la lista?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
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
