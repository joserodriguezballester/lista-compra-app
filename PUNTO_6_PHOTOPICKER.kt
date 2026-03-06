// ============================================================================
// PUNTO 6: PHOTO PICKER - Código para copiar y pegar
// ============================================================================

// ============================================================================
// PARTE 1: MAINSCREEN.KT - Añadir al INICIO de la función MainScreen()
// ============================================================================

// Después de: val uiState by viewModel.uiState.collectAsState()
// Después de: val context = LocalContext.current

val context = LocalContext.current

// Estados para PhotoPicker
var showPhotoPicker by remember { mutableStateOf(false) }
var productIdForPhoto by remember { mutableLongStateOf(0L) }

// Launcher para seleccionar imagen de la galería
val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    uri?.let {
        viewModel.onPhotoSelected(productIdForPhoto, it.toString())
        showPhotoPicker = false
    }
}

// Lanzar PhotoPicker cuando showPhotoPicker sea true
LaunchedEffect(showPhotoPicker) {
    if (showPhotoPicker) {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
}

// ============================================================================
// PARTE 2: MAINSCREEN.KT - Botón para abrir PhotoPicker (en cada producto)
// ============================================================================

// Dentro de LazyColumn donde muestras los productos, añadir esto:

// Item del producto con botón de foto
IconButton(
    onClick = {
        productIdForPhoto = product.id  // ID del producto actual
        showPhotoPicker = true
    }
) {
    if (product.hasPhoto()) {
        // Si ya tiene foto, mostrar miniatura
        AsyncImage(
            model = product.photoUri,
            contentDescription = "Foto del producto",
            modifier = Modifier.size(40.dp)
        )
    } else {
        // Si no tiene foto, mostrar icono de cámara
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = "Añadir foto"
        )
    }
}

// ============================================================================
// PARTE 3: VIEWMODEl.KT - Añadir estas funciones
// ============================================================================

// Después de clearAllProducts() u otras funciones existentes

/**
 * Guarda la foto seleccionada para un producto
 * @param productId ID del producto
 * @param photoUri URI de la imagen seleccionada
 */
fun onPhotoSelected(productId: Long, photoUri: String) {
    viewModelScope.launch {
        repository.updateProductPhoto(productId, photoUri)
        loadData()  // Recargar para mostrar la foto
    }
}

/**
 * Elimina la foto de un producto
 */
fun deleteProductPhoto(productId: Long) {
    viewModelScope.launch {
        repository.updateProductPhoto(productId, null)
        loadData()
    }
}

// ============================================================================
// PARTE 4: REPOSITORY.KT - Añadir esta función (si no existe)
// ============================================================================

/**
 * Actualiza la foto de un producto en la base de datos
 */
suspend fun updateProductPhoto(productId: Long, photoUri: String?) {
    val product = productDao.getProductById(productId) ?: return
    
    val updated = product.copy(
        photoUri = photoUri,
        photoTimestamp = if (photoUri != null) System.currentTimeMillis() else null,
        isPhotoUserSelected = photoUri != null
    )
    
    productDao.updateProduct(updated)
}

// ============================================================================
// IMPORTS NECESARIOS (añadir si faltan)
// ============================================================================

// En MainScreen.kt:
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage  // Si usas Coil para mostrar la imagen

// En ViewModel.kt:
import kotlinx.coroutines.launch

// ============================================================================
// RESUMEN DE DÓNDE VA CADA COSA:
// ============================================================================
//
// 1. PARTE 1 → MainScreen.kt: Al INICIO, con los otros 'val' y 'remember'
// 2. PARTE 2 → MainScreen.kt: Dentro del LazyColumn de productos
// 3. PARTE 3 → ShoppingListViewModel.kt: Al final de la clase
// 4. PARTE 4 → ShoppingListRepository.kt: Como función más
//
// ============================================================================
