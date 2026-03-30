package com.jose.listacompra.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Gestiona la selección de imagen desde cámara o galería
 * Devuelve la URI de la imagen seleccionada o capturada
 */
@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit
): ImagePicker {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionToRequest by remember { mutableStateOf<String?>(null) }
    
    // Launcher para galería (PhotoPicker moderno Android 13+)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }
    
    // Launcher para galería (fallback Android anterior)
    val galleryLegacyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }
    
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { onImageSelected(it) }
        }
        tempCameraUri = null
    }
    
    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && permissionToRequest != null) {
            // Reintentar después de obtener permiso
            when (permissionToRequest) {
                Manifest.permission.CAMERA -> openCamera(context) { tempCameraUri = it; cameraLauncher.launch(it) }
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES -> openGallery(context, galleryLauncher, galleryLegacyLauncher)
            }
        }
        permissionToRequest = null
    }
    
    val imagePicker = remember {
        ImagePicker(
            openGallery = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                } else {
                    null
                }
                
                if (permission != null && !checkPermission(context, permission)) {
                    permissionToRequest = permission
                    permissionLauncher.launch(permission)
                } else {
                    openGallery(context, galleryLauncher, galleryLegacyLauncher)
                }
            },
            openCamera = {
                if (!checkPermission(context, Manifest.permission.CAMERA)) {
                    permissionToRequest = Manifest.permission.CAMERA
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    openCamera(context) { tempCameraUri = it; cameraLauncher.launch(it) }
                }
            }
        )
    }
    
    return imagePicker
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar imagen") },
        text = { Text("¿Desde dónde quieres obtener la imagen?") },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onCameraSelected()
            }) {
                Text("Cámara")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                onGallerySelected()
            }) {
                Text("Galería")
            }
        }
    )
}

data class ImagePicker(
    val openGallery: () -> Unit,
    val openCamera: () -> Unit
)

private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun openGallery(
    context: Context,
    galleryLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    galleryLegacyLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    // Usar PhotoPicker moderno si está disponible (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    } else {
        galleryLegacyLauncher.launch("image/*")
    }
}

private fun openCamera(
    context: Context,
    onUriReady: (Uri) -> Unit
) {
    // Crear archivo temporal para la foto
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) imagesDir.mkdirs()
    
    val imageFile = File(imagesDir, "photo_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
    
    onUriReady(uri)
}
