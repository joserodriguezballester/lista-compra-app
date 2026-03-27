package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment

@Composable
fun CategoryFilterDialog(
    categories: List<String> = emptyList(),
    selectedCategory: String? = null,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    // Si no hay categorías, mostrar mensaje
    val hasCategories = categories.isNotEmpty()
    
    var selected by remember { mutableStateOf(selectedCategory) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar por categoría") },
        text = {
            if (hasCategories) {
                Column {
                    // Opción "Todas"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == null,
                                onClick = { selected = null }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == null,
                            onClick = { selected = null }
                        )
                        Text(
                            text = "Todas",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Lista de categorías
                    LazyColumn {
                        items(categories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selected == category,
                                        onClick = { selected = category }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected == category,
                                    onClick = { selected = category }
                                )
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay categorías disponibles.\n\nLas categorías se mostrarán cuando haya artículos con categoría asignada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCategorySelected(selected)
                    onDismiss()
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
