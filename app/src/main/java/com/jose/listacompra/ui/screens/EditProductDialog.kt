package com.jose.listacompra.ui.screens

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.viewmodel.ShoppingListViewModel
import java.io.File

//import android.icu.text.SimpleDateFormat
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    aisles: List<Aisle>,
    offers: List<Offer>,
    onDismiss: () -> Unit,
    onSave: (name: String, aisleId: Long, quantity: Float, price: Float?, offerId: Long?, photoUri: String?) -> Unit,
    onCalculateOffer: (quantity: Float, price: Float?, offerId: Long?) -> ShoppingListViewModel.OfferPreviewResult?
) {
    // --- ESTADOS ---
    var name by remember { mutableStateOf(product.name) }
    var selectedAisle by remember { mutableStateOf(aisles.find { it.id == product.aisleId }) }
    var quantity by remember { mutableStateOf(product.quantity.toInt().toString()) }
    var price by remember { mutableStateOf(product.estimatedPrice?.toString() ?: "") }
    var selectedOffer by remember { mutableStateOf(offers.find { it.id == product.offerId }) }
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }

    // ESTADOS DE FOTO
    var selectedPhotoUri by remember { mutableStateOf<String?>(product.photoUri) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current


    // --- LAUNCHERS ---

    // 1. Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedPhotoUri = it.toString() }
    }
    // 2. Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) selectedPhotoUri = cameraTempUri?.toString()
    }


    // 3: Permiso cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Re-ejecutar lógica de cámara tras conceder permiso
            val file = File.createTempFile(
                "IMG_${System.currentTimeMillis()}", ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            cameraTempUri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraTempUri?.let { cameraLauncher.launch(it) }
        }
    }

    fun launchCamera() {
        val file = File.createTempFile(
            "IMG_${System.currentTimeMillis()}", ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        cameraTempUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraTempUri?.let { cameraLauncher.launch(it) }
    }

    // Calcular preview de oferta en tiempo real
    val quantityFloat = quantity.toFloatOrNull() ?: 1f
    val priceFloat = price.toFloatOrNull()
    val offerPreview = onCalculateOffer(quantityFloat, priceFloat, selectedOffer?.id)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // 1. SECCIÓN DE FOTO
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedButton(onClick = { showPhotoOptions = true }) {
                            Icon(Icons.Default.AddAPhoto, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedPhotoUri == null) "Añadir foto" else "Cambiar foto")
                        }

                        // Menú de opciones de foto
                        DropdownMenu(
                            expanded = showPhotoOptions,
                            onDismissRequest = { showPhotoOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📁 Galería") },
                                onClick = {
                                    showPhotoOptions = false
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📷 Cámara") },
                                onClick = {
                                    showPhotoOptions = false
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        launchCamera()
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    }
                                }
                            )
                            if (selectedPhotoUri != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "🗑️ Eliminar foto",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        selectedPhotoUri = null
                                        showPhotoOptions = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Nombre del producto
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mostrar EAN si existe
                if (product.ean != null) {
                    Text(
                        "EAN: ${product.ean}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Selector de pasillo
                ExposedDropdownMenuBox(
                    expanded = aisleExpanded,
                    onExpandedChange = { aisleExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedAisle?.let { "${it.emoji} ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aisleExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = aisleExpanded,
                        onDismissRequest = { aisleExpanded = false }
                    ) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text("${aisle.emoji} ${aisle.name}") },
                                onClick = {
                                    selectedAisle = aisle
                                    aisleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Cantidad y precio en la misma fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.4f)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it.filter { c -> c.isDigit() || c == '.' }
                        },
                        label = { Text("Precio ud") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                // Selector de ofertas
                ExposedDropdownMenuBox(
                    expanded = offerExpanded,
                    onExpandedChange = { offerExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOffer?.name ?: "Sin oferta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Oferta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = offerExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = offerExpanded,
                        onDismissRequest = { offerExpanded = false }
                    ) {
                        // Opción "Sin oferta"
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = {
                                selectedOffer = null
                                offerExpanded = false
                            }
                        )
                        Divider()
                        // Lista de ofertas
                        offers.forEach { offer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(offer.name)
                                        Text(
                                            text = offer.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedOffer = offer
                                    offerExpanded = false
                                }
                            )
                        }
                    }
                }

                // Preview de oferta aplicada
                if (offerPreview != null && priceFloat != null && priceFloat > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (offerPreview.hasOffer && offerPreview.savings > 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = if (offerPreview.hasOffer) "🏷️ Oferta aplicada" else "Precio calculado",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Sin oferta:",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "%.2f€".format(quantityFloat * priceFloat),
                                        style = MaterialTheme.typography.bodySmall,
                                        textDecoration = if (offerPreview.hasOffer) TextDecoration.LineThrough else null
                                    )
                                }

                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    Text(
                                        text = "A pagar:",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "%.2f€".format(offerPreview.finalPrice),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                            }

                            // Mostrar ahorro si aplica
                            if (offerPreview.hasOffer && offerPreview.savings > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "¡Ahorras %.2f€! ✅".format(offerPreview.savings),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedAisle != null) {
                        val qty = quantity.toFloatOrNull() ?: 1f
                        val prc = price.toFloatOrNull()
                        onSave(
                            name.trim(),
                            selectedAisle!!.id,
                            qty,
                            prc,
                            selectedOffer?.id,
                            selectedPhotoUri
                        )
                    }
                },
                enabled = name.isNotBlank() && selectedAisle != null
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
