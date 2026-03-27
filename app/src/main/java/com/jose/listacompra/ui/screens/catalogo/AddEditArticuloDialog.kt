package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Articulo

@Composable
fun AddEditArticuloDialog(
    articulo: Articulo? = null,
    ean: String? = null,
    onDismiss: () -> Unit,
    onSave: (Articulo) -> Unit,
    onScanBarcode: () -> Unit = {}
) {
    var name by remember { mutableStateOf(articulo?.name ?: "") }
    var size by remember { mutableStateOf(articulo?.size?.toString() ?: "") }
    var unit by remember { mutableStateOf(articulo?.unit ?: "") }
    var price by remember { mutableStateOf(articulo?.finalPrice?.toString() ?: "") }
    var eanValue by remember { mutableStateOf(articulo?.ean ?: ean ?: "") }
    var categoryId by remember { mutableStateOf(articulo?.categoryId?.toString() ?: "") }
    var photoUri by remember { mutableStateOf(articulo?.photoUri ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = { Text(if (articulo == null) "Nuevo artículo" else "Editar artículo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!photoUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = name,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, null, Modifier.size(48.dp), MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                OutlinedTextField(name, { name = it }, { Text("Nombre *") }, Modifier.fillMaxWidth(), singleLine = true)

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(size, { size = it }, { Text("Tamaño") }, Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(unit, { unit = it }, { Text("Unidad") }, Modifier.weight(1f), singleLine = true)
                }

                OutlinedTextField(price, { price = it }, { Text("Precio (€)") }, Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(
                    eanValue, 
                    { eanValue = it }, 
                    { Text("Código de barras") },
                    Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onScanBarcode) {
                            Icon(Icons.Default.QrCodeScanner, "Escanear")
                        }
                    }
                )

                OutlinedTextField(categoryId, { categoryId = it }, { Text("Categoría") }, Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button({
                val newArticulo = Articulo(
                    id = articulo?.id ?: 0,
                    name = name,
                    size = size.toDoubleOrNull() ?: 1.0,
                    unit = unit.ifBlank { "ud" },
                    finalPrice = price.toFloatOrNull(),
                    ean = eanValue.ifBlank { null },
                    categoryId = categoryId.toLongOrNull() ?: articulo?.categoryId,
                    photoUri = photoUri.ifBlank { null }
                )
                onSave(newArticulo)
            }, enabled = name.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}
