# Instrucciones para Implementar Fotos - Lista de Compra

## Archivos Subidos

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `Database_CON_FOTOS.kt` | Parte 1: Entidades, DAOs principales, ProductEntity con foto | 266 |
| `Database_CON_FOTOS_FINAL.kt` | Parte 2: Resto de DAOs, @Database con versión 7, Migración | ~180 |

## Cómo Combinar los Archivos

### Opción 1: Copiar y Pegar Manualmente

1. En tu Android Studio, abre `Database.kt` original
2. Busca donde termina `data class ProductEntity` (línea con `val orderIndex: Int`)
3. Añade **antes del cierre `)`** estos campos:

```kotlin
    ,
    // CAMPOS DE FOTO
    val photoUri: String? = null,
    val photoTimestamp: Long? = null,
    val isPhotoUserSelected: Boolean = false
)
```

4. Incrementa la versión de la BD: cambia `version = 6` a `version = 7`
5. En el companion object de ShoppingListDatabase, añade:

```kotlin
companion object {
    const val DATABASE_NAME = "shopping_list_db"

    /* Migración 6 → 7: Añadir campos de foto */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE products ADD COLUMN photoUri TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE products ADD COLUMN photoTimestamp INTEGER DEFAULT NULL")
            database.execSQL("ALTER TABLE products ADD COLUMN isPhotoUserSelected INTEGER DEFAULT 0")
        }
    }
}
```

### Opción 2: Reemplazar Archivo Completo

Copia el contenido de `Database_CON_FOTOS.kt` + `Database_CON_FOTOS_FINAL.kt` a un nuevo archivo `Database.kt`.

Pero atención: hay que unirlos correctamente. El punto de unión es después de `val orderIndex: Int` en ProductEntity.

---

## Cambios en Otros Archivos

### 1. Product.kt (Modelo de Dominio)

Añadir a `data class Product`:

```kotlin
data class Product(
    // ... campos existentes ...
    val orderIndex: Int = 0,
    // NUEVOS CAMPOS DE FOTO
    val photoUri: String? = null,
    val photoTimestamp: Long? = null,
    val isPhotoUserSelected: Boolean = false
) {
    // ... fun existing ...
    
    fun hasPhoto(): Boolean = photoUri != null
    
    fun getDefaultEmoji(): String {
        return when (categoryId?.toInt()) {
            1 -> "🥛"  // Lácteos
            2 -> "🥤"  // Bebidas
            3 -> "🍪"  // Galletas
            4 -> "🥩"  // Carnes
            5 -> "🐟"  // Pescados
            6 -> "🍎"  // Frutas/Verduras
            7 -> "🥖"  // Panadería
            8 -> "❄️"  // Congelados
            9 -> "🧼"  // Limpieza
            10 -> "🥫" // Despensa
            else -> "🛒"
        }
    }
}
```

### 2. Converters.kt

En `ProductEntity.toDomain()`:

```kotlin
fun ProductEntity.toDomain(): Product = Product(
    id = this.id,
    name = this.name,
    categoryId = this.categoryId,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex,
    // NUEVOS CAMPOS DE FOTO
    photoUri = this.photoUri,
    photoTimestamp = this.photoTimestamp,
    isPhotoUserSelected = this.isPhotoUserSelected
)
```

En `Product.toEntity()`:

```kotlin
fun Product.toEntity(): ProductEntity = ProductEntity(
    id = this.id,
    name = this.name,
    categoryId = this.categoryId,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex,
    // NUEVOS CAMPOS DE FOTO
    photoUri = this.photoUri,
    photoTimestamp = this.photoTimestamp,
    isPhotoUserSelected = this.isPhotoUserSelected
)
```

### 3. ShoppingListRepository.kt - Métodos para Fotos

```kotlin
/**
 * Actualiza la foto de un producto
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

/**
 * Elimina la foto de un producto
 */
suspend fun deleteProductPhoto(productId: Long) {
    updateProductPhoto(productId, null)
}
```

### 4. ViewModel - Estados para PhotoPicker

```kotlin
// ShoppingListViewModel.kt

private val _showPhotoPicker = MutableStateFlow(false)
val showPhotoPicker: StateFlow<Boolean> = _showPhotoPicker.asStateFlow()

private var _productForPhoto: Long? = null

fun showPhotoPickerForProduct(productId: Long) {
    _productForPhoto = productId
    _showPhotoPicker.value = true
}

fun dismissPhotoPicker() {
    _showPhotoPicker.value = false
    _productForPhoto = null
}

fun onPhotoSelected(uri: String) {
    viewModelScope.launch {
        _productForPhoto?.let { productId ->
            repository.updateProductPhoto(productId, uri)
            loadData()
        }
        dismissPhotoPicker()
    }
}

fun onPhotoDeleted(productId: Long) {
    viewModelScope.launch {
        repository.deleteProductPhoto(productId)
        loadData()
    }
}
```

### 5. AndroidManifest.xml - Permisos

```xml
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Android 9-12 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

---

## Resumen de Archivos en el Repo

| Archivo | Propósito |
|---------|-----------|
| `Database_CON_FOTOS.kt` | Parte 1 del Database.kt |
| `Database_CON_FOTOS_FINAL.kt` | Parte 2 (final) del Database.kt |
| `PASOS_A_SEGUIR_2.md` | Guía original de fotos |
| `PASOS_A_SEGUIR_3.md` | UI de fotos en tarjetas |
| `INSTRUCCIONES_FOTOS.md` | Este archivo - instrucciones combinadas |

## Próximos Pasos Sugeridos

1. **Juntar Database** → Usar `Database_CON_FOTOS.kt` + `Database_CON_FOTOS_FINAL.kt`
2. **Modificar Product.kt** → Añadir campos de foto
3. **Actualizar Converters.kt** → Mapear campos de foto
4. **Modificar Repository** → Métodos save/update foto
5. **Añadir PhotoPicker** → En ViewModel y UI
6. **Mostrar fotos** → En tarjetas de producto

¿Necesitas algo más o hay algún paso que no esté claro?