package com.jose.listacompra.ui.screens.productlist

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.ui.utils.getCategoryEmoji
import com.jose.listacompra.ui.utils.calculateOfferStatus
import android.util.Log
import android.widget.Toast
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    aisles: List<Aisle>,
    offers: List<Offer> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val TAG = "EditProductDialog"
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var estimatedPrice by remember { mutableStateOf(product.estimatedPrice?.toString() ?: "") }
    var selectedAisleId by remember { mutableStateOf<Long?>(product.aisleId) }
    var selectedOfferId by remember { mutableStateOf(product.offerId) }
    var notes by remember { mutableStateOf(product.notes) }
    var photoUri by remember { mutableStateOf(product.photoUri?.let { Uri.parse(it) }) }
    
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d(TAG, "Foto tomada: $photoUri")
        }
    }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
        Log.d(TAG, "Imagen seleccionada: $uri")
    }

    // Permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File.createTempFile(
                "product_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila: Imagen + Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen del producto (click para cambiar)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = getCategoryEmoji(product.name),
                                    fontSize = 28.sp
                                )
                                Text(
                                    "Cambiar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // Botones de acción
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Cámara
                        OutlinedButton(
                            onClick = {
                                val permission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                    val photoFile = File.createTempFile(
                                        "product_${System.currentTimeMillis()}",
                                        ".jpg",
                                        context.cacheDir
                                    )
                                    photoUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile
                                    )
                                    cameraLauncher.launch(photoUri)
                                } else {
                                    cameraPermissionLauncher.launch(permission)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cámara", fontSize = 12.sp)
                        }
                        
                        // Galería
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galería", fontSize = 12.sp)
                        }
                    }
                }

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    }
                )

                // Cantidad
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Precio unitario
                OutlinedTextField(
                    value = estimatedPrice,
                    onValueChange = { estimatedPrice = it },
                    label = { Text("Precio unitario (€)") },
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aisleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = aisleExpanded,
                        onDismissRequest = { aisleExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin pasillo") },
                            onClick = { selectedAisleId = null; aisleExpanded = false }
                        )
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = { selectedAisleId = aisle.id; aisleExpanded = false }
                            )
                        }
                    }
                }

                // Oferta
                ExposedDropdownMenuBox(
                    expanded = offerExpanded,
                    onExpandedChange = { offerExpanded = it }
                ) {
                    val selectedOffer = offers.find { it.id == selectedOfferId }
                    OutlinedTextField(
                        value = selectedOffer?.let { "🏷️ ${it.name}" } ?: "Sin oferta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Oferta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = offerExpanded) },
                        leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = offerExpanded,
                        onDismissRequest = { offerExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = { selectedOfferId = null; offerExpanded = false }
                        )
                        offers.forEach { offer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🏷️ ${offer.name}", fontWeight = FontWeight.Medium)
                                        Text(
                                            offer.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { selectedOfferId = offer.id; offerExpanded = false }
                            )
                        }
                    }
                }

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    placeholder = { Text("Ej: del Mercadona, marca Hacendado...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toFloatOrNull() ?: product.quantity
                    val price = estimatedPrice.toFloatOrNull()
                    
                    val updatedProduct = product.copy(
                        name = name,
                        quantity = qty,
                        estimatedPrice = price,
                        aisleId = selectedAisleId,
                        notes = notes,
                        offerId = selectedOfferId,
                        photoUri = photoUri?.toString()
                    )
                    Log.d(TAG, "Guardando: name=$name, photoUri=$photoUri")
                    onSave(updatedProduct)
                },
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Diálogo para seleccionar origen de imagen
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Cambiar imagen") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImagePicker = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Galería")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImagePicker = false
                                val permission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                    val photoFile = File.createTempFile(
                                        "product_${System.currentTimeMillis()}",
                                        ".jpg",
                                        context.cacheDir
                                    )
                                    photoUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile
                                    )
                                    cameraLauncher.launch(photoUri)
                                } else {
                                    cameraPermissionLauncher.launch(permission)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cámara")
                    }
                    // Opción para eliminar
                    if (photoUri != null) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImagePicker = false
                                    photoUri = null
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Eliminar foto", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}