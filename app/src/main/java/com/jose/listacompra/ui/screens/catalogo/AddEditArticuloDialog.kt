package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Articulo

@OptIn(ExperimentalMaterial3Api::class)
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
        title = {
            Text(if (articulo == null) "Nuevo artículo" else "Editar artículo")
        },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Tamaño") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = eanValue,
                    onValueChange = { eanValue = it },
                    label = { Text("Código de barras") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onScanBarcode) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Escanear código"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = categoryId,
                    onValueChange = { categoryId = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newArticulo = Articulo(
                        id = articulo?.id ?: 0,
                        name = name,
                        size = size.toDoubleOrNull()?.toFloat() ?: 1.0F,
                        unit = unit.ifBlank { "ud" },
                        finalPrice = price.toFloatOrNull(),
                        ean = eanValue.ifBlank { null },
                        categoryId = categoryId.toLongOrNull() ?: articulo?.categoryId ?: 0L,
                        photoUri = photoUri.ifBlank { null }
                    )
                    onSave(newArticulo)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
