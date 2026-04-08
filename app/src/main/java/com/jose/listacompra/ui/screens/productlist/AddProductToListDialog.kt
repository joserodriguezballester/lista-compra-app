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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.ui.utils.getCategoryEmoji
import kotlinx.coroutines.delay
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.filled.Store


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListDialog(
    aisles: List<Aisle>,
    offers: List<Offer> = emptyList(),
    supermarkets: List<Supermarket> = emptyList(), // T4
    suggestions: List<Articulo> = emptyList(),
    initialName: String? = null,
    initialImageUrl: String? = null,
    initialCategoryId: Long? = null,
    initialQuantity: String? = null,
    onSearch: (String) -> Unit = {},
    onOpenScanner: () -> Unit = {},
    onImageSelected: (Uri?) -> Unit = {},
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Float, aisleId: Long?, price: Float?, offerId: Long?, notes: String?, photoUri: String?, supermarketId: Long?) -> Unit // T4
) {
    val TAG = "AddProductDialog"
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(initialName ?: "") }
    var quantity by remember { mutableStateOf(initialQuantity ?: "1") }
    var price by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedAisleId by remember { mutableStateOf<Long?>(initialCategoryId) }
    var selectedOfferId by remember { mutableStateOf<Long?>(null) }
    var selectedSupermarketId by remember { mutableStateOf<Long?>(0L) } // T4: Por defecto "Cualquiera"
    var photoUri by remember { mutableStateOf<Uri?>(initialImageUrl?.let { Uri.parse(it) }) }
    
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }
    var supermarketExpanded by remember { mutableStateOf(false) } // T4
    var showSuggestions by remember { mutableStateOf(false) }
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
            val photoFile = java.io.File.createTempFile(
                "product_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            photoUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Debounce para búsqueda
    LaunchedEffect(name) {
        if (name.length >= 2) {
            delay(300)
            onSearch(name)
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
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
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Añadir foto",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    "Añadir foto",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // Botones de acción
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Scanner
                        OutlinedButton(
                            onClick = onOpenScanner,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Escanear", fontSize = 12.sp)
                        }
                        
                        // Cámara
                        OutlinedButton(
                            onClick = {
                                val permission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                    val photoFile = java.io.File.createTempFile(
                                        "product_${System.currentTimeMillis()}",
                                        ".jpg",
                                        context.cacheDir
                                    )
                                    photoUri = androidx.core.content.FileProvider.getUriForFile(
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
                    }
                }

                // Nombre con sugerencias
                ExposedDropdownMenuBox(
                    expanded = showSuggestions && suggestions.isNotEmpty(),
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del producto") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .onFocusChanged { focusState ->
                                showSuggestions = focusState.isFocused && suggestions.isNotEmpty()
                            },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        },
                        trailingIcon = {
                            if (name.isNotEmpty()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(Icons.Default.Close, "Limpiar")
                                }
                            }
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = showSuggestions && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false }
                    ) {
                        suggestions.forEach { articulo ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = getCategoryEmoji(articulo.name),
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(articulo.name, fontWeight = FontWeight.Medium)
                                            articulo.finalPrice?.let {
                                                Text(
                                                    "${String.format("%.2f", it)} €",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    name = articulo.name
                                    articulo.finalPrice?.let { price = it.toString() }
                                    showSuggestions = false
                                }
                            )
                        }
                    }
                }

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
                    value = price,
                    onValueChange = { price = it },
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

                // T4: Supermercado
                ExposedDropdownMenuBox(
                    expanded = supermarketExpanded,
                    onExpandedChange = { supermarketExpanded = it }
                ) {
                    val selectedSupermarket = supermarkets.find { it.id == selectedSupermarketId }
                    val displayValue = when {
                        selectedSupermarketId == 0L -> "📦 Cualquiera"
                        selectedSupermarket != null -> "${selectedSupermarket.emoji} ${selectedSupermarket.name}"
                        else -> "📦 Cualquiera"
                    }
                    OutlinedTextField(
                        value = displayValue,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Supermercado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supermarketExpanded) },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = supermarketExpanded,
                        onDismissRequest = { supermarketExpanded = false }
                    ) {
                        // Opción "Cualquiera"
                        DropdownMenuItem(
                            text = { Text("📦 Cualquiera") },
                            onClick = { selectedSupermarketId = 0L; supermarketExpanded = false }
                        )
                        // Supermercados específicos (sin "Cualquiera" que tiene id=0)
                        supermarkets.filter { it.id > 0 }.forEach { supermarket ->
                            DropdownMenuItem(
                                text = { Text("${supermarket.emoji} ${supermarket.name}") },
                                onClick = { selectedSupermarketId = supermarket.id; supermarketExpanded = false }
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
                    Log.d(TAG, "Añadiendo: name=$name, qty=$quantity, photoUri=$photoUri, supermarket=$selectedSupermarketId")
                    onAdd(
                        name,
                        quantity.toFloatOrNull() ?: 1f,
                        selectedAisleId,
                        price.toFloatOrNull(),
                        selectedOfferId,
                        notes.ifBlank { null },
                        photoUri?.toString(),
                        selectedSupermarketId // T4
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir")
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
            title = { Text("Seleccionar imagen") },
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
                                    val photoFile = java.io.File.createTempFile(
                                        "product_${System.currentTimeMillis()}",
                                        ".jpg",
                                        context.cacheDir
                                    )
                                    photoUri = androidx.core.content.FileProvider.getUriForFile(
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