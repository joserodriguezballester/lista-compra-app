package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterDialog(
    categories: List<Category> = emptyList(),
    selectedCategoryIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onCategorySelected: (Set<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(selectedCategoryIds) }
    
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { 
            it.name.contains(searchQuery, ignoreCase = true) 
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Filtrar por categoría") 
        },
        text = {
            Column {
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar categoría...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Chip "Todas" (limpia selección)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    FilterChip(
                        selected = selected.isEmpty(),
                        onClick = { selected = emptySet() },
                        label = { Text("Todas") },
                        leadingIcon = {
                            if (selected.isEmpty()) {
                                Icon(Icons.Default.Check, "Seleccionado", modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (filteredCategories.isEmpty() && searchQuery.isNotBlank()) {
                    Text(
                        text = "No se encontraron categorías",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else if (categories.isEmpty()) {
                    Text(
                        text = "No hay categorías disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Grid de categorías (compacto)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(filteredCategories) { category ->
                            val categoryId = category.id.toString()
                            val isSelected = categoryId in selected
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    selected = if (isSelected) {
                                        selected - categoryId
                                    } else {
                                        selected + categoryId
                                    }
                                },
                                label = { 
                                    Text(
                                        text = "${category.icon} ${category.name}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, "Seleccionado", modifier = Modifier.size(16.dp))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Text(
                    text = "${filteredCategories.size} categorías",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
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