package com.jose.listacompra.ui.screens

import android.R.attr.enabled
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.ui.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    aisles: List<Aisle>,
    offers: List<Offer>,
    onDismiss: () -> Unit,
    onSave: (name: String, aisleId: Long, quantity: Float, price: Float?, offerId: Long?,photoUri: String?) -> Unit,
    onCalculateOffer: (quantity: Float, price: Float?, offerId: Long?) -> ShoppingListViewModel.OfferPreviewResult?
) {
    var name by remember { mutableStateOf(product.name) }
    var selectedAisle by remember { mutableStateOf(aisles.find { it.id == product.aisleId }) }
    var quantity by remember { mutableStateOf(product.quantity.toInt().toString()) }
    var price by remember { mutableStateOf(product.estimatedPrice?.toString() ?: "") }
    var selectedOffer by remember { mutableStateOf(offers.find { it.id == product.offerId }) }
    var aisleExpanded by remember { mutableStateOf(false) }
    var offerExpanded by remember { mutableStateOf(false) }

    // NUEVO: Estado para foto seleccionada (temporal, no guardada aún)
    var selectedPhotoUri by remember { mutableStateOf<String?>(product.photoUri) }

    // Calcular preview de oferta en tiempo real
    val quantityFloat = quantity.toFloatOrNull() ?: 1f
    val priceFloat = price.toFloatOrNull()
    val offerPreview = onCalculateOffer(quantityFloat, priceFloat, selectedOffer?.id)


    val context = LocalContext.current

    // NUEVO: PhotoPicker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedPhotoUri = uri.toString()
                Toast.makeText(
                    context,
                    "Foto: $uri", Toast.LENGTH_SHORT,
                ).show()
            }
        }
    )



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del producto
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // NUEVO: Preview de foto seleccionada
                if (selectedPhotoUri != null) {
                    AsyncImage(
                        model = selectedPhotoUri,
                        contentDescription = "Foto del producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                // PLACEHOLDER: Botón foto (sin acción aún)
                OutlinedButton(
                    onClick = {  // Abrir PhotoPicker
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            ) )},
                    enabled = true
                ) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir foto ")
                }

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
                        onSave(name.trim(),
                            selectedAisle!!.id,
                            qty,
                            prc,
                            selectedOffer?.id,
                            selectedPhotoUri)
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
