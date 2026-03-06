# Pasos a Seguir 2 - Añadir Foto al Producto

## Objetivo
Permitir asignar una foto a cada producto (tomar con cámara o elegir de galería).

---

## 1. Permisos en AndroidManifest.xml

```xml
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<!-- Android 9-12 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<!-- Para cámara -->
<uses-permission android:name="android.permission.CAMERA" />
```

---

## 2. Modificar ProductEntity (Database.kt)

Añadir campos a `data class ProductEntity`:

```kotlin
data class ProductEntity(
    // ... campos existentes ...
    val notes: String,
    val orderIndex: Int,
    val aisleMap: String? = null,
    
    // NUEVOS CAMPOS PARA FOTO
    val photoUri: String? = null,      // URI de la imagen seleccionada
    val photoTimestamp: Long? = null,  // Para ordenar por recientes
    val isPhotoUserSelected: Boolean = false  // true = elegida por usuario, false = default
)
```

---

## 3. Modificar Product.kt (Modelo de Dominio)

```kotlin
data class Product(
    // ... campos existentes ...
    val aisleMap: Map<String, String>? = null,
    
    // NUEVOS
    val photoUri: String? = null,
    val photoTimestamp: Long? = null,
    val isPhotoUserSelected: Boolean = false
) {
    // ... funciones existentes ...
    
    /**
     * Indica si el producto tiene foto personalizada
     */
    fun hasPhoto(): Boolean = photoUri != null
    
    /**
     * Genera un emoji por defecto según la categoría
     * (uso si no hay foto)
     */
    fun getDefaultEmoji(): String {
        return when (categoryId) {
            1L -> "🥛"  // Lácteos
            2L -> "🥤"  // Bebidas
            3L -> "🍪"  // Galletas
            4L -> "🥩"  // Carnes
            else -> "🛒"
        }
    }
    
    companion object {
        fun generatePhotoFileName(name: String): String {
            return "product_${System.currentTimeMillis()}_${name.replace(" ", "_")}.jpg"
        }
    }
}
```

---

## 4. Actualizar Converters (Converters.kt)

Añadir en `ProductEntity.toDomain()`:

```kotlin
fun ProductEntity.toDomain(): Product = Product(
    // ... campos existentes ...
    aisleMap = this.aisleMap.toAisleMap(),
    
    // NUEVOS
    photoUri = this.photoUri,
    photoTimestamp = this.photoTimestamp,
    isPhotoUserSelected = this.isPhotoUserSelected
)
```

Añadir en `Product.toEntity()`:

```kotlin
fun Product.toEntity(): ProductEntity = ProductEntity(
    // ... campos existentes ...
    aisleMap = this.aisleMap.toJsonString(),
    
    // NUEVOS
    photoUri = this.photoUri,
    photoTimestamp = this.photoTimestamp,
    isPhotoUserSelected = this.isPhotoUserSelected
)
```

---

## 5. Actualizar Migración (V6 → V8)

Como ahora añadimos campos de foto, mejor saltar directo a versión 8:

```kotlin
// En Database.kt
@Database(
    entities = [ ... ],
    version = 8,  // ← Cambiar de 6 a 8
    exportSchema = false
)

// Migración combinada
companion object {
    val MIGRATION_6_8 = object : Migration(6, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migración de Categorías + AisleMap
            ""
            // 1. Añadir columnas aisleMap
            database.execSQL(
                "ALTER TABLE products ADD COLUMN aisleMap TEXT DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE shopping_lists ADD COLUMN supermarket TEXT DEFAULT NULL"
            )
            
            // 2. Añadir columnas de FOTO
            database.execSQL(
                "ALTER TABLE products ADD COLUMN photoUri TEXT DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE products ADD COLUMN photoTimestamp INTEGER DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE products ADD COLUMN isPhotoUserSelected INTEGER DEFAULT 0"
            )
        }
    }
}
```

---

## 6. PhotoPicker con Activity Result Contracts

### Opción A: Photo Picker Nativo (Android 13+, recomendado)

```kotlin
// En MainActivity.kt o en un Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

// Launcher para seleccionar imagen
def launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    uri?.let {
        // Guardar URI y copiar a almacenamiento app
        viewModel.onPhotoSelected(productId, it)
    }
}

// Uso
Button(onClick = {
    launcher.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )
})
```

---

## 7. Guardar Imagen en Almacenamiento de la App

```kotlin
// En ViewModel o Repository
class ProductRepository(private val context: Context) {
    
    suspend fun saveProductPhoto(productId: Long, sourceUri: Uri): String {
        val filename = Product.generatePhotoFileName("product_$productId")
        
        // Directorio de la app
        val imagesDir = File(context.filesDir, "product_images").apply { 
            if (!exists()) mkdirs() 
        }
        
        val destFile = File(imagesDir, filename)
        
        // Copiar y redimensionar
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Redimensionar con BitmapFactory si es necesario
        // ...
        
        return destFile.absolutePath
    }
    
    suspend fun deleteProductPhoto(photoPath: String?) {
        photoPath?.let { File(it).delete() }
    }
}
```

---

## 8. Mostrar Imagen con Coil

```kotlin
// build.gradle (app)
implementation("io.coil-kt:coil-compose:2.5.0")

// En Composable
import coil.compose.AsyncImage
import coil.request.CachePolicy

@Composable
fun ProductPhoto(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        if (product.hasPhoto()) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(product.photoUri)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Emoji por categoría
            Text(
                text = product.getDefaultEmoji(),
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

---

## 9. Menú de Opciones (Foto)

```kotlin
@Composable
fun PhotoOptionsMenu(
    product: Product,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.PhotoCamera, "Foto")
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("📸 Hacer foto") },
                onClick