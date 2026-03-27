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
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!articulo.photoUri.isNullOrEmpty()) {
                            AsyncImage(articulo.photoUri, articulo.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Image, null, Modifier.size(64.dp), MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isEditing) {
                    OutlinedTextField(name, { name = it }, { Text("Nombre") }, Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(size, { size = it }, { Text("Cantidad") }, Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(unit, { unit = it }, { Text("Unidad") }, Modifier.weight(1f), singleLine = true)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(price, { price = it }, { Text("Precio (€)") }, Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(ean, { ean = it }, { Text("Código de barras") }, Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(categoryId, { categoryId = it }, { Text("Categoría") }, Modifier.fillMaxWidth(), singleLine = true)
                } else {
                    Text(articulo.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text("${articulo.size} ${articulo.unit}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (articulo.finalPrice != null) "${String.format("%.2f", articulo.finalPrice)} €" else "Precio no disponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!articulo.ean.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("EAN: ${articulo.ean}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (articulo.categoryId != null) {
                        Spacer(Modifier.height(4.dp))
                        Text("Categoría: ${articulo.categoryId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Row(Arrangement.spacedBy(8.dp)) {
                    TextButton({ isEditing = false }) { Text("Cancelar") }
                    Button({
                        onSave(articulo.copy(
                            name = name,
                            size = size.toDoubleOrNull() ?: articulo.size,
                            unit = unit,
                            finalPrice = price.toFloatOrNull(),
                            ean = ean.ifBlank { null },
                            categoryId = categoryId.toLongOrNull() ?: articulo.categoryId
                        ))
                    }) { Text("Guardar") }
                }
            } else {
                Row(Arrangement.spacedBy(8.dp)) {
                    TextButton(onDelete, ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") }
                    Button({ isEditing = true }) { Text("Editar") }
                }
            }
        },
        dismissButton = { if (!isEditing) TextButton(onDismiss) { Text("Cerrar") } }
    )
}
