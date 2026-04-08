package com.jose.listacompra.ui.screens.productlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var photoUri by remember { mutableStateOf<Uri?>(product.photoUri?.let { Uri.parse(it) }) }
    
    var showAisleDropdown by remember { mutableStateOf(false) }
    var showOfferDropdown by remember { mutableStateOf(false) }
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
                            text = { Text("Sin pasillo") },
                            onClick = { selectedAisleId = null; showAisleDropdown = false }
                        )
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = { selectedAisleId = aisle.id; showAisleDropdown = false }
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
                            estimatedPrice = price.toFloatOrNull(),
                            offerId = selectedOfferId,
                            notes = notes.ifBlank { null }
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
