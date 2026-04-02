package com.jose.listacompra.ui.screens.productlist

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Offer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListDialog(
    aisles: List<Aisle>,
    offers: List<Offer> = emptyList(),
    suggestions: List<Articulo> = emptyList(),
    scannedName: String? = null,
    scannedPrice: String? = null,
    scannedImageUrl: String? = null,
    onSearch: (String) -> Unit = {},
    onOpenScanner: () -> Unit = {},
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Float, aisleId: Long?, price: Float?, offerId: Long?, notes: String?) -> Unit
) {
    val TAG = "AddProductDialog"
    
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedAisleId by remember { mutableStateOf<Long?>(null) }
    var selectedOfferId by remember { mutableStateOf<Long?>(null) }
    
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    // Rellenar con datos del scanner
    LaunchedEffect(scannedName, scannedPrice) {
        if (scannedName != null) {
            name = scannedName
            Log.d(TAG, "Scanner - Nombre: $scannedName")
        }
        if (scannedPrice != null) {
            price = scannedPrice
            Log.d(TAG, "Scanner - Precio: $scannedPrice")
        }
        if (scannedImageUrl != null) {
            Log.d(TAG, "Scanner - Imagen: $scannedImageUrl")
        }
    }

    // Debounce para búsqueda
    LaunchedEffect(name) {
        if (name.length >= 2) {
            delay(300)
            onSearch(name)
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre con sugerencias y botón scanner
                ExposedDropdownMenuBox(
                    expanded = showSuggestions && suggestions.isNotEmpty(),
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del producto") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .onFocusChanged { focusState ->
                                showSuggestions = focusState.isFocused && suggestions.isNotEmpty()
                            },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        },
                        trailingIcon = {
                            Row {
                                // Botón scanner
                                IconButton(onClick = {
                                    Log.d(TAG, "Abriendo scanner...")
                                    onOpenScanner()
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, "Escanear código")
                                }
                                // Botón limpiar
                                if (name.isNotEmpty()) {
                                    IconButton(onClick = { name = "" }) {
                                        Icon(Icons.Default.Close, "Limpiar")
                                    }
                                }
                            }
                        }
                    )

                    // Lista de sugerencias
                    ExposedDropdownMenu(
                        expanded = showSuggestions && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        suggestions.forEach { articulo ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = articulo.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        if (articulo.finalPrice != null) {
                                            Text(
                                                text = "${String.format("%.2f", articulo.finalPrice)} €",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    name = articulo.name
                                    articulo.finalPrice?.let { price = it.toString() }
                                    Log.d(TAG, "Sugerencia seleccionada: ${articulo.name}")
                                    showSuggestions = false
                                }
                            )
                        }
                    }
                }

                // Cantidad
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Precio unitario
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio unitario (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Euro, contentDescription = null)
                    }
                )

                // Pasillo
                ExposedDropdownMenuBox(
                    expanded = aisleExpanded,
                    onExpandedChange = { aisleExpanded = it }
                ) {
                    val selectedAisle = aisles.find { it.id == selectedAisleId }
                    OutlinedTextField(
                        value = selectedAisle?.let { "${it.emoji} ${it.name}" } ?: "Sin pasillo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = aisleExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = aisleExpanded,
                        onDismissRequest = { aisleExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin pasillo") },
                            onClick = {
                                selectedAisleId = null
                                aisleExpanded = false
                            }
                        )
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = {
                                    selectedAisleId = aisle.id
                                    aisleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Oferta
                ExposedDropdownMenuBox(
                    expanded = offerExpanded,
                    onExpandedChange = { offerExpanded = it }
                ) {
                    val selectedOffer = offers.find { it.id == selectedOfferId }
                    OutlinedTextField(
                        value = selectedOffer?.let { "🏷️ ${it.name}" } ?: "Sin oferta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Oferta") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = offerExpanded)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.LocalOffer, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = offerExpanded,
                        onDismissRequest = { offerExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = {
                                selectedOfferId = null
                                offerExpanded = false
                            }
                        )
                        offers.forEach { offer ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text("🏷️ ${offer.name}")
                                        Text(
                                            text = offer.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedOfferId = offer.id
                                    offerExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    placeholder = { Text("Ej: del Mercadona, marca Hacendado...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 2,
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d(TAG, "Añadiendo producto: name=$name, qty=$quantity, aisle=$selectedAisleId, price=$price, offer=$selectedOfferId, notes=$notes")
                    onAdd(
                        name,
                        quantity.toFloatOrNull() ?: 1f,
                        selectedAisleId,
                        price.toFloatOrNull(),
                        selectedOfferId,
                        notes.ifBlank { null }
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
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