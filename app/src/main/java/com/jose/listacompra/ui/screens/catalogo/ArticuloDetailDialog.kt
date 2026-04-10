package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.R
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticuloDetailDialog(
    articulo: Articulo,
    categories: List<Category> = emptyList(),
    selectedImageUri: String? = null,
    onDismiss: () -> Unit,
    onSave: (Articulo) -> Unit,
    onDelete: () -> Unit,
    onSelectImage: () -> Unit = {}
) {
    var name by remember { mutableStateOf(articulo.name) }
    var size by remember { mutableStateOf(articulo.size.toString()) }
    var unit by remember { mutableStateOf(articulo.unit) }
    var price by remember { mutableStateOf(articulo.finalPrice?.toString() ?: "") }
    var ean by remember { mutableStateOf(articulo.ean ?: "") }
    var selectedCategory by remember { mutableStateOf(
        categories.find { it.id == articulo.categoryId } ?: categories.firstOrNull()
    ) }
    var photoUri by remember { mutableStateOf(selectedImageUri ?: articulo.photoUri ?: "") }
    
    // Actualizar photoUri cuando selectedImageUri cambia desde fuera
    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            photoUri = selectedImageUri
        }
    }
    
    var isEditing by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen clicable en modo edición
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isEditing) {
                                Modifier.clickable { onSelectImage() }
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!photoUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = articulo.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            if (isEditing) {
                                // Overlay con icono de cámara
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        contentDescription = "Cambiar foto",
                                        modifier = Modifier.padding(8.dp).size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_photo_loading),
                                    contentDescription = "Sin foto",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                                if (isEditing) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Toca para añadir foto",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newValue -> name = newValue },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { newValue -> size = newValue },
                            label = { Text("Cantidad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { newValue -> unit = newValue },
                            label = { Text("Unidad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { newValue -> price = newValue },
                        label = { Text("Precio (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = ean,
                        onValueChange = { newValue -> ean = newValue },
                        label = { Text("Código de barras") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Dropdown de categorías con nombres
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.let { "${it.icon} ${it.name}" } ?: "Sin categoría",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text("${category.icon} ${category.name}") },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = articulo.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${articulo.size} ${articulo.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (articulo.finalPrice != null) {
                            "${String.format("%.2f", articulo.finalPrice)} €"
                        } else {
                            "Precio no disponible"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (!articulo.ean.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "EAN: ${articulo.ean}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Mostrar nombre de categoría en lugar de ID
                    val categoryName = categories.find { it.id == articulo.categoryId }
                    if (categoryName != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Categoría: ${categoryName.icon} ${categoryName.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { isEditing = false }) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val updatedArticulo = articulo.copy(
                                name = name,
                                size = size.toDoubleOrNull()?.toFloat()?: articulo.size,
                                unit = unit,
                                finalPrice = price.toDoubleOrNull()?.toFloat(),
                                ean = ean.ifBlank { null },
                                categoryId = selectedCategory?.id ?: articulo.categoryId,
                                photoUri = photoUri.ifBlank { null }
                            )
                            onSave(updatedArticulo)
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                    Button(onClick = { isEditing = true }) {
                        Text("Editar")
                    }
                }
            }
        },
        dismissButton = {
            if (!isEditing) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
}
