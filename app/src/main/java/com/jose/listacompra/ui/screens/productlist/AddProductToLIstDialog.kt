package com.jose.listacompra.ui.screens.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Euro
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListDialog(
    aisles: List<Aisle>,
    suggestions: List<Articulo>,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Float, aisleId: Long?, price: Float?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedAisleId by remember { mutableStateOf<Long?>(null) }
    var aisleExpanded by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

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
    AlertDialog (
            onDismissRequest = onDismiss,
    title = { Text("Añadir producto") },
    text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre con sugerencias
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
                                            text = "${
                                                String.format(
                                                    "%.2f",
                                                    articulo.finalPrice
                                                )
                                            } €",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                name = articulo.name
                                articulo.finalPrice?.let { price = it.toString() }
                                showSuggestions = false
                            }
                        )
                    }
                }
            }

            // Cantidad y Unidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit, onValueChange = { unit = it },
                    label = { Text("Unidad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Precio
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio (€)") },
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
