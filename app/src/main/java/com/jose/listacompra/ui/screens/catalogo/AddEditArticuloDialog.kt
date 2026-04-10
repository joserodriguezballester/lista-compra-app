package com.jose.listacompra.ui.screens.catalogo

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun AddEditArticuloDialog(
    articulo: Articulo? = null,
    ean: String? = null,
    selectedImageUri: String? = null,
    categories: List<Category> = emptyList(),
    prefillName: String? = null,
    prefillQuantity: String? = null,
    prefillCategoryId: String? = null,
    onDismiss: () -> Unit,
    onSave: (Articulo) -> Unit,
    onScanBarcode: () -> Unit = {},
    onSelectImage: () -> Unit = {}
) {
    var name by remember { mutableStateOf(prefillName ?: articulo?.name ?: "") }
    var size by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(articulo?.unit ?: "") }
    var price by remember { mutableStateOf(articulo?.finalPrice?.toString() ?: "") }
    var eanValue by remember { mutableStateOf(articulo?.ean ?: ean ?: "") }
    var selectedCategory by remember { mutableStateOf(
        categories.find { it.id.toString() == prefillCategoryId }
            ?: categories.find { it.id == articulo?.categoryId }
            ?: categories.firstOrNull()
    ) }
    var photoUri by remember { mutableStateOf(selectedImageUri ?: articulo?.photoUri ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    // Actualizar photoUri cuando selectedImageUri cambia desde fuera
    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            photoUri = selectedImageUri
        }
    }
    
    // Parsear cantidad si viene del scanner
    val quantityParts = prefillQuantity?.split(" ")
    val parsedSize = quantityParts?.firstOrNull()?.toFloatOrNull()
    val parsedUnit = quantityParts?.drop(1)?.firstOrNull() ?: ""
    
    var sizeValue by remember { mutableStateOf(parsedSize?.toString() ?: articulo?.size?.toString() ?: "") }
    var unitValue by remember { mutableStateOf(parsedUnit.ifBlank { articulo?.unit ?: "" }) }

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
                // Imagen clicable para cambiar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectImage() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!photoUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = name,
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                contentScale = ContentScale.Crop
                            )
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
                        value = sizeValue,
                        onValueChange = { sizeValue = it },
                        label = { Text("Tamaño") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitValue,
                        onValueChange = { unitValue = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newArticulo = Articulo(
                        id = articulo?.id ?: 0,
                        name = name,
                        size = sizeValue.toFloatOrNull() ?: 1.0F,
                        unit = unitValue.ifBlank { "ud" },
                        finalPrice = price.toFloatOrNull(),
                        ean = eanValue.ifBlank { null },
                        categoryId = selectedCategory?.id ?: 0L,
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
