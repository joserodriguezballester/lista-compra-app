package com.jose.listacompra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.ui.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    aisles: List<Aisle>,
    offers: List<Offer>,
    suggestions: List<ProductSuggestion>,
    onDismiss: () -> Unit,
    onAdd: (name: String, aisleId: Long, quantity: Float, price: Float?, offerId: Long?) -> Unit,
    onSearchSuggestions: (query: String) -> Unit,
    onCalculateOffer: (quantity: Float, price: Float?, offerId: Long?) -> ShoppingListViewModel.OfferPreviewResult?
) {
    var name by remember { mutableStateOf("") }
    var selectedAisle by remember { mutableStateOf(aisles.firstOrNull()) }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var selectedOffer by remember { mutableStateOf<Offer?>(null) }
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }
    
    // Calcular preview de oferta en tiempo real
    val quantityFloat = quantity.toFloatOrNull() ?: 1f
    val priceFloat = price.toFloatOrNull()
    val offerPreview = onCalculateOffer(quantityFloat, priceFloat, selectedOffer?.id)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del producto con sugerencias
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.length >= 2) {
                                onSearchSuggestions(it)
                            }
                        },
                        label = { Text("Nombre del producto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mostrar sugerencias si hay
                    if (suggestions.isNotEmpty() && name.length >= 2) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "Sugerencias:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                suggestions.forEach { suggestion ->
                                    SuggestionItem(
                                        suggestion = suggestion,
                                        aisles = aisles,
                                        onClick = {
                                            name = suggestion.name
                                            selectedAisle = aisles.find { it.id == suggestion.aisleId }
                                            quantity = suggestion.suggestedQuantity.toInt().toString()
                                            price = suggestion.suggestedPrice?.toString() ?: ""
                                            onSearchSuggestions("") // Limpiar sugerencias
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Selector de pasillo
                ExposedDropdownMenuBox(
                    expanded = aisleExpanded,
                    onExpandedChange = { aisleExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedAisle?.let { "${it.emoji} ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aisleExpanded) },
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
                                    selectedAisle = aisle
                                    aisleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Cantidad y precio en la misma fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.4f)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it.filter { c -> c.isDigit() || c == '.' }
                        },
                        label = { Text("Precio ud") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                
                // Selector de ofertas
                ExposedDropdownMenuBox(
                    expanded = offerExpanded,
                    onExpandedChange = { offerExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOffer?.name ?: "Sin oferta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Oferta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = offerExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = offerExpanded,
                        onDismissRequest = { offerExpanded = false }
                    ) {
                        // Opción "Sin oferta"
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = {
                                selectedOffer = null
                                offerExpanded = false
                            }
                        )
                        Divider()
                        // Lista de ofertas
                        offers.forEach { offer ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(offer.name)
                                        Text(
                                            text = offer.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedOffer = offer
                                    offerExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Preview de oferta aplicada
                if (offerPreview != null && priceFloat != null && priceFloat > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (offerPreview.hasOffer && offerPreview.savings > 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = if (offerPreview.hasOffer) "🏷️ Oferta aplicada" else "Precio calculado",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Sin oferta:",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "%.2f€".format(quantityFloat * priceFloat),
                                        style = MaterialTheme.typography.bodySmall,
                                        textDecoration = if (offerPreview.hasOffer) TextDecoration.LineThrough else null
                                    )
                                }
                                
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    Text(
                                        text = "A pagar:",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "%.2f€".format(offerPreview.finalPrice),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Mostrar ahorro si aplica
                            if (offerPreview.hasOffer && offerPreview.savings > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "¡Ahorras %.2f€! ✅".format(offerPreview.savings),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedAisle != null) {
                        val qty = quantity.toFloatOrNull() ?: 1f
                        val prc = price.toFloatOrNull()
                        onAdd(name.trim(), selectedAisle!!.id, qty, prc, selectedOffer?.id)
                    }
                },
                enabled = name.isNotBlank() && selectedAisle != null
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

@Composable
private fun SuggestionItem(
    suggestion: ProductSuggestion,
    aisles: List<Aisle>,
    onClick: () -> Unit
) {
    val aisleName = aisles.find { it.id == suggestion.aisleId }?.let { "${it.emoji} ${it.name}" } ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = buildString {
                    append(aisleName)
                    if (suggestion.suggestedPrice != null) {
                        append(" · ")
                        append("%.2f€".format(suggestion.suggestedPrice))
                    }
                    append(" (usado ${suggestion.usageCount}x)")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Usar sugerencia",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
