package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Articulo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticuloDetailDialog(
    articulo: Articulo,
    onDismiss: () -> Unit,
    onSave: (Articulo) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(articulo.name) }
    var size by remember { mutableStateOf(articulo.size.toString()) }
    var unit by remember { mutableStateOf(articulo.unit) }
    var price by remember { mutableStateOf(articulo.finalPrice?.toString() ?: "") }
    var ean by remember { mutableStateOf(articulo.ean ?: "") }
    var categoryId by remember { mutableStateOf(articulo.categoryId?.toString() ?: "") }
    
    var isEditing by remember { mutableStateOf(false) }
    
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!articulo.photoUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = articulo.photoUri,
                                contentDescription = articulo.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
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
                    
                    OutlinedTextField(
                        value = categoryId,
                        onValueChange = { newValue -> categoryId = newValue },
                        label = { Text("Categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
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
                    
                    if (!articulo.categoryId.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Categoría: ${articulo.categoryId}",
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
                                size = size.toDoubleOrNull() ?: articulo.size,
                                unit = unit,
                                finalPrice = price.toDoubleOrNull(),
                                ean = ean.ifBlank { null },
                                categoryId = categoryId.toLongOrNull()
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
