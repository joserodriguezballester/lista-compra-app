package com.jose.listacompra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.ui.viewmodel.AddProductViewModel

/**
 * Diálogo para añadir productos a la lista de la compra
 * Con sugerencias de artículos del catálogo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    listId: Long,
    supermarketId: Long? = null,
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Float, aisleId: Long?, price: Float?) -> Unit,
    viewModel: AddProductViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var selectedAisleId by remember { mutableStateOf<Long?>(null) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val suggestions by viewModel.suggestions.collectAsState()
    val aisles by viewModel.aisles.collectAsState()
    
    // Buscar sugerencias cuando cambia el texto
    LaunchedEffect(name) {
        if (name.length >= 2) {
            viewModel.searchArticulos(name)
            showSuggestions = true
        } else {
            viewModel.clearSuggestions()
            showSuggestions = false
        }
    }
    
    // Cargar pasillos al iniciar
    LaunchedEffect(supermarketId) {
        viewModel.loadAisles(supermarketId ?: 1L)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo de nombre con sugerencias
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
                            if (name.isNotEmpty()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(Icons.Default.Close, "Limpiar")
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
                                    selectedAisleId = null // TODO: sugerir pasillo según categoría
                                    showSuggestions = false
                                },
                                modifier = Modifier.fillMaxWidth()
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

                // Precio (opcional)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (€, opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Pasillo (opcional)
                if (aisles.isNotEmpty()) {
                    var aisleExpanded by remember { mutableStateOf(false) }
                    val selectedAisle = aisles.find { it.id == selectedAisleId }
                    
                    ExposedDropdownMenuBox(
                        expanded = aisleExpanded,
                        onExpandedChange = { aisleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAisle?.let { "${it.emoji} ${it.name}" } ?: "Sin pasillo",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pasillo (opcional)") },
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
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        name,
                        quantity.toFloatOrNull() ?: 1f,
                        selectedAisleId,
                        price.toFloatOrNull()
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
