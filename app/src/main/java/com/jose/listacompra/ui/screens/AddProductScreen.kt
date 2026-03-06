package com.jose.listacompra.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Category

@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onNavigateBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableFloatStateOf(1f) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedPhotoUri = uri
    }

    Scaffold(
        topBar = { /* ... */ },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveProduct(
                        name = productName,
                        quantity = quantity,
                        categoryId = selectedCategory?.id,
                        photoUri = selectedPhotoUri
                    )
                    onNavigateBack()
                }
            ) {
                Icon(Icons.Default.Save, "Guardar")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ========== PREVIEW DE IMAGEN GRANDE ==========
            ProductPhotoPreview(
                photoUri = selectedPhotoUri,
                categoryId = selectedCategory?.id,
                productName = productName,
                onClick = { photoLauncher.launch(PickVisualMediaRequest()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            )

            // Botones de acción de foto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = { photoLauncher.launch(PickVisualMediaRequest()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = { /* Abrir cámara */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }

                if (selectedPhotoUri != null) {
                    Spacer(Modifier.width(12.dp))
                    IconButton(
                        onClick = { selectedPhotoUri = null },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar foto")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ==========