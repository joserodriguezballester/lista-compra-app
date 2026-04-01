package com.jose.listacompra.ui.screens.productlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Offer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    aisles: List<Aisle>,
    offers: List<Offer> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var estimatedPrice by remember { mutableStateOf(product.estimatedPrice?.toString() ?: "") }
    var selectedAisleId by remember { mutableStateOf(product.aisleId) }
    var selectedOfferId by remember { mutableStateOf(product.offerId) }
    var notes by remember { mutableStateOf(product.notes) }
    
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                    value = estimatedPrice,
                    onValueChange = { estimatedPrice = it },
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
                        // Sin oferta
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = {
                                selectedOfferId = null
                                offerExpanded = false
                            }
                        )
                        // Lista de ofertas
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toFloatOrNull() ?: product.quantity
                    val price = estimatedPrice.toFloatOrNull()
                    
                    // Calcular precio final según oferta
                    val finalPrice = calculateFinalPrice(qty, price, selectedOfferId, offers)
                    
                    val updatedProduct = product.copy(
                        name = name,
                        quantity = qty,
                        estimatedPrice = price,
                        finalPrice = finalPrice,
                        aisleId = selectedAisleId,
                        notes = notes,
                        offerId = selectedOfferId
                    )
                    onSave(updatedProduct)
                },
                enabled = name.isNotBlank()
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

/**
 * Calcula el precio final según la oferta aplicada
 */
private fun calculateFinalPrice(
    quantity: Float,
    unitPrice: Float?,
    offerId: Long?,
    offers: List<Offer>
): Float? {
    if (unitPrice == null || offerId == null) return unitPrice?.let { it * quantity }
    
    val offer = offers.find { it.id == offerId } ?: return unitPrice * quantity
    val qty = quantity.toInt()
    
    return when (offer.code) {
        "3x2" -> {
            // Lleva 3, paga 2
            val groups = qty / 3
            val remainder = qty % 3
            (groups * 2 + remainder) * unitPrice
        }
        "2x1" -> {
            // Lleva 2, paga 1
            val groups = qty / 2
            val remainder = qty % 2
            (groups + remainder) * unitPrice
        }
        "2nd_50" -> {
            // 2ª unidad al 50%
            val pairs = qty / 2
            val remainder = qty % 2
            pairs * (unitPrice * 1.5f) + remainder * unitPrice
        }
        "2nd_70" -> {
            // 2ª unidad al 70% descuento (paga 30%)
            val pairs = qty / 2
            val remainder = qty % 2
            pairs * (unitPrice * 1.3f) + remainder * unitPrice
        }
        "4x3" -> {
            // Lleva 4, paga 3
            val groups = qty / 4
            val remainder = qty % 4
            (groups * 3 + remainder) * unitPrice
        }
        else -> unitPrice * quantity
    }
}
