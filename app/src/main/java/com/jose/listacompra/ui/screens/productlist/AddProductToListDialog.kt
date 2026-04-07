package com.jose.listacompra.ui.screens.productlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.ui.viewmodel.ProductListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListDialog(
    onDismiss: () -> Unit,
    onAddProduct: (
        name: String,
        quantity: Float,
        aisleId: Long?,
        price: Float?,
        offerId: Long?,
        notes: String?,
        photoUri: Uri?
    ) -> Unit,
    shoppingListId: Long,
    articulos: List<Articulo> = emptyList(),
    selectedArticulo: Articulo? = null,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var productName by remember { mutableStateOf(selectedArticulo?.name ?: "") }
    var quantity by remember { mutableFloatStateOf(1f) }
    var price by remember { mutableStateOf(selectedArticulo?.finalPrice?.toString() ?: "") }
    var notes by remember { mutableStateOf("") }
    var selectedAisle by remember { mutableStateOf<Long?>(null) }
    var selectedOffer by remember { mutableStateOf<Long?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(selectedArticulo?.photoUri?.let { Uri.parse(it) }) }
    
    val aisles = uiState.aisles
    val offers = uiState.offers
    
    var showAisleDropdown by remember { mutableStateOf(false) }
    var showOfferDropdown by remember { mutableStateOf(false) }
    var showArticuloSheet by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        photoUri = uri
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de artículo existente
                if (articulos.isNotEmpty()) {
                    OutlinedTextField(
                        value = if (selectedArticulo != null) "📝 ${selectedArticulo.name}" else "Seleccionar del catálogo",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Del catálogo") },
                        trailingIcon = {
                            IconButton(onClick = { showArticuloSheet = true }) {
                                Icon(Icons.Default.Search, "Buscar")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showArticuloSheet = true }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Nombre del producto
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Cantidad y precio en fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity.toString(),
                        onValueChange = { quantity = it.toFloatOrNull() ?: 1f },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio €") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                // Pasillo
                ExposedDropdownMenuBox(
                    expanded = showAisleDropdown,
                    onExpandedChange = { showAisleDropdown = it }
                ) {
                    OutlinedTextField(
                        value = aisles.find { it.id == selectedAisle }?.name ?: "Sin pasillo",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pasillo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAisleDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showAisleDropdown,
                        onDismissRequest = { showAisleDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin pasillo") },
                            onClick = { selectedAisle = null; showAisleDropdown = false }
                        )
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text(aisle.name) },
                                onClick = { selectedAisle = aisle.id; showAisleDropdown = false }
                            )
                        }
                    }
                }
                
                // Oferta
                ExposedDropdownMenuBox(
                    expanded = showOfferDropdown,
                    onExpandedChange = { showOfferDropdown = it }
                ) {
                    OutlinedTextField(
                        value = offers.find { it.id == selectedOffer }?.let { "${it.code} - ${it.name}" } ?: "Sin oferta",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Oferta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOfferDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showOfferDropdown,
                        onDismissRequest = { showOfferDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin oferta") },
                            onClick = { selectedOffer = null; showOfferDropdown = false }
                        )
                        offers.forEach { offer ->
                            DropdownMenuItem(
                                text = { Text("${offer.code} - ${offer.name}") },
                                onClick = { selectedOffer = offer.id; showOfferDropdown = false }
                            )
                        }
                    }
                }
                
                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Imagen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Imagen:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Icon(Icons.Default.Image, "Seleccionar imagen")
                        }
                        if (photoUri != null) {
                            IconButton(onClick = { photoUri = null }) {
                                Icon(Icons.Default.Close, "Quitar imagen")
                            }
                        }
                    }
                }
                
                // Preview de imagen
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Imagen del producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddProduct(
                        productName,
                        quantity,
                        selectedAisle,
                        price.toFloatOrNull(),
                        selectedOffer,
                        notes.ifBlank { null },
                        photoUri
                    )
                },
                enabled = productName.isNotBlank() && quantity > 0
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Sheet para seleccionar artículo del catálogo
    if (showArticuloSheet) {
        ModalBottomSheet(
            onDismissRequest = { showArticuloSheet = false }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(articulos.sortedBy { it.name }) { articulo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            productName = articulo.name
                            quantity = 1f // Articulo no tiene quantity
                            price = articulo.finalPrice?.toString() ?: ""
                            photoUri = articulo.photoUri?.let { Uri.parse(it) }
                            showArticuloSheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            articulo.photoUri?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = articulo.name,
                                    modifier = Modifier.size(48.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column {
                                Text(articulo.name, style = MaterialTheme.typography.bodyLarge)
                                articulo.finalPrice?.let {
                                    Text("€${String.format("%.2f", it)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
