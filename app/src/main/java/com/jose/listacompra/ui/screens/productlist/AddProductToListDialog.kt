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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.ui.utils.getCategoryEmoji
import kotlinx.coroutines.delay
import android.util.Log
import android.widget.Toast
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListDialog(
    aisles: List<Aisle>,
    offers: List<Offer> = emptyList(),
    suggestions: List<Articulo> = emptyList(),
    historySuggestions: List<ProductFrequencyEntity> = emptyList(),
    initialName: String? = null,
    onSearch: (String) -> Unit = {},
    onOpenScanner: () -> Unit = {},
    onImageSelected: (Uri?) -> Unit = {},
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Float, aisleId: Long?, price: Float?, offerId: Long?, notes: String?, photoUri: String?) -> Unit
) {
    val TAG = "AddProductDialog"
    val context = LocalContext.current
    
    Log.d(TAG, "📊 Dialog recibió ${offers.size} offers: ${offers.map { it.name }}")
    
    var name by remember { mutableStateOf(initialName ?: "") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedAisleId by remember { mutableStateOf<Long?>(null) }
    var selectedOfferId by remember { mutableStateOf<Long?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d(TAG, "Foto tomada: $photoUri")
        } else {
            Log.w(TAG, "Foto cancelada")
            photoUri = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
        Log.d(TAG, "Imagen seleccionada: $uri")
    }

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

    LaunchedEffect(name) {
        if (name.length >= 2) {
            delay(300)
            onSearch(name)
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }
    
    // Auto-seleccionar pasillo del historial si coincide exactamente
    LaunchedEffect(historySuggestions, name) {
        val normalizedName = name.lowercase().trim()
        val matchingHistory = historySuggestions.find { 
            it.productName.equals(normalizedName, ignoreCase = true) 
        }
        
        matchingHistory?.let { h ->
            if (h.lastAisleId > 0 && selectedAisleId == null) {
                selectedAisleId = h.lastAisleId
                Log.d(TAG, "Auto-seleccionado pasillo ${h.lastAisleId} para '$name'")
            }
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
                // Imagen + Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                contentDescription = "Foto",
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

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenScanner,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Escanear", fontSize = 12.sp)
                        }
                        
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
                            val history = historySuggestions.find { 
                                it.productName.equals(articulo.name, ignoreCase = true) 
                            }
                            
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = getCategoryEmoji(articulo.name),
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(articulo.name, fontWeight = FontWeight.Medium)
                                            articulo.finalPrice?.let {
                                                Text(
                                                    "${String.format("%.2f", it)} €",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        
                                        history?.let { h ->
                                            val aisle = aisles.find { it.id == h.lastAisleId }
                                            if (aisle != null && h.lastAisleId > 0) {
                                                Surface(
                                                    shape = MaterialTheme.shapes.small,
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "📍 ${aisle.emoji} ${aisle.name}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            "📊${h.timesPurchased}x",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    name = articulo.name
                                    articulo.finalPrice?.let { price = it.toString() }
                                    
                                    history?.let { h ->
                                        if (h.lastAisleId > 0) {
                                            selectedAisleId = h.lastAisleId
                                            quantity = h.lastQuantity.toString()
                                        }
                                    }
                                    
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

                // Precio
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
                    Log.d(TAG, "Añadiendo: name=$name, photoUri=$photoUri")
                    onAdd(
                        name,
                        quantity.toFloatOrNull() ?: 1f,
                        selectedAisleId,
                        price.toFloatOrNull(),
                        selectedOfferId,
                        notes.ifBlank { null },
                        photoUri?.toString()
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