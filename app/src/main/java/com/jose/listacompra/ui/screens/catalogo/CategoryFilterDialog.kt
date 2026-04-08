package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterDialog(
    categories: List<Category> = emptyList(),
    selectedCategoryId: String? = null,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(selectedCategoryId) }
    
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
                
                // Chip "Todas"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    FilterChip(
                        selected = selected == null,
                        onClick = { selected = null },
                        label = { Text("Todas") },
                        leadingIcon = {
                            if (selected == null) {
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
                            FilterChip(
                                selected = selected == category.id.toString(),
                                onClick = { selected = category.id.toString() },
                                label = { 
                                    Text(
                                        text = "${category.emoji} ${category.name}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                leadingIcon = {
                                    if (selected == category.id.toString()) {
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