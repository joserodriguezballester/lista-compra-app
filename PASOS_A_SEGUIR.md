# Pasos a Seguir - Implementación de Pasillos por Supermercado

## Objetivo
Cambiar el sistema de pasillos para que cada producto pueda tener diferentes ubicaciones según el supermercado (Carrefour, Mercadona, Lidl...) usando un campo JSON.

---

## 1. Modificar Database.kt (Room Entity)

Localización: `app/src/main/java/com/jose/listacompra/data/local/Database.kt`

### Cambiar ProductEntity:

```kotlin
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long?,  // FK a categories (nullable)
    val aisleId: Long?,     // ← HACER NULLABLE (mantener para compatibilidad)
    val shoppingListId: Long,
    val quantity: Float,
    val estimatedPrice: Float?,
    val offerId: Long?,
    val finalPrice: Float?,
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int,
    val aisleMap: String? = null  // ← NUEVO: JSON {"carrefour":"Pasillo 3","mercadona":"Pasillo 2"}
)
```

### Incrementar versión de la base de datos:

```kotlin
@Database(
    entities = [ ... ],
    version = 7,  // ← Cambiar de 6 a 7
    exportSchema = false
)
```

---

## 2. Modificar Product.kt (Modelo de Dominio)

Localización: `app/src/main/java/com/jose/listacompra/domain/model/Product.kt`

```kotlin
data class Product(
    val id: Long = 0,
    val name: String,
    val categoryId: Long?,
    val aisleId: Long? = null,  // Deprecated
    val shoppingListId: Long = 1,
    val quantity: Float = 1f,
    val estimatedPrice: Float? = null,
    val offerId: Long? = null,
    val finalPrice: Float? = null,
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0,
    val aisleMap: Map<String, String>? = null  // ← NUEVO
) { ... }
```

---

## 3. Añadir Converters JSON

Localización: `app/src/main/java/com/jose/listacompra/data/local/Converters.kt`

Añadir al final del archivo:

```kotlin
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Conversor de Map<String, String> ↔ JSON String
private val gson = Gson()

fun Map<String, String>?.toJsonString(): String? {
    return this?.let { gson.toJson(it) }
}

fun String?.toAisleMap(): Map<String, String>? {
    return this?.let {
        val type = object : TypeToken<Map<String, String>>() {}.type
        gson.fromJson(it, type)
    }
}
```

---

## 4. Actualizar Conversores ProductEntity ↔ Product

En `Converters.kt`, modificar:

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
    aisleMap = this.aisleMap.toAisleMap()  // ← AÑADIR
)

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
    aisleMap = this.aisleMap.toJsonString()  // ← AÑADIR
)
```

---

## 5. Modificar ShoppingListEntity

Añadir campo para guardar el supermercado seleccionado:

```kotlin
@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: String = "ACTIVA",
    val supermarket: String? = null  // ← NUEVO: "carrefour", "mercadona", etc.
)
```

Igualmente en `ShoppingList.kt` del dominio.

---

## 6. Modificar Queries en ProductDao

Añadir métodos para ordenar según el supermercado:

```kotlin
@Dao
interface ProductDao {
    
    // Ordenar por categoría (modo por defecto)
    @Query("""
        SELECT * FROM products 
        WHERE shoppingListId = :listId 
        ORDER BY categoryId ASC, orderIndex ASC
    """)
    suspend fun getAllProductsByCategory(listId: Long): List<ProductEntity>
    
    // Ordenar por pasillo según supermercado
    @Query("""
        SELECT * FROM products 
        WHERE shoppingListId = :listId 
        ORDER BY 
            CASE 
                WHEN json_extract(aisleMap, :supermarket) IS NOT NULL 
                THEN json_extract(aisleMap, :supermarket)
                ELSE 'ZZZZ'  -- Sin asignar va al final
            END ASC,
            orderIndex ASC
    """)
    suspend fun getAllProductsByAisle(listId: Long, supermarket: String): List<ProductEntity>
    
    ...
}
```

**Nota:** SQLite/Room permite usar `json_extract()` si tienes SQLite 3.38.0+. Si no, usar ordenamiento en memoria (Kotlin).

---

## 7. Actualizar Repository

En `ShoppingListRepository.kt`, modificar el método de obtención de productos:

```kotlin
suspend fun getAllProducts(listId: Long, sortMode: SortMode = SortMode.CATEGORY): List<Product> {
    val list = shoppingListDao.getListById(listId)?.toDomain()
    
    return when (sortMode) {
        SortMode.CATEGORY -> {
            productDao.getAllProductsByCategory(listId).map { it.toDomain() }
        }
        SortMode.AISLE -> {
            val superName = list?.supermarket ?: return getAllProducts(listId, SortMode.CATEGORY)
            // Si no hay supermercado asignado, fallback a categoría
            productDao.getAllProductsByAisle(listId, "$.${superName}")
                .map { it.toDomain() }
        }
    }
}

enum class SortMode { CATEGORY, AISLE }
```

---

## 8. Migración de Base de Datos (V6 → V7)

Crear migración para preservar datos existentes:

```kotlin
// En Database.kt
companion object {
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Añadir columna aisleMap
            database.execSQL(
                "ALTER TABLE products ADD COLUMN aisleMap TEXT DEFAULT NULL"
            )
            
            // 2. Hacer aisleId nullable (SQLite ya lo permite si hay datos)
            // No hace falta ALTER TABLE, Room lo maneja
            
            // 3. Añadir columna supermarket a shopping_lists
            database.execSQL(
                "ALTER TABLE shopping_lists ADD COLUMN supermarket TEXT DEFAULT NULL"
            )
            
            // 4. Opcional: Migrar datos antiguos
            // Si tiene aisleId, mover a aisleMap vacío para compatibilidad
            // (mejor hacerlo manualmente en la UI de edición de pasillo)
        }
    }
}
```

Y en el builder:

```kotlin
.addMigrations(MIGRATION_6_7)
```

---

## 9. Actualizar UI (MainScreen)

Añadir toggle o botón para cambiar entre modos:

```kotlin
// En la toolbar o floating action button
DropdownMenu(
    expanded = menuExpanded,
    onDismissRequest = { menuExpanded = false }
) {
    DropdownMenuItem(
        text = { Text("🗂️ Por Categoría") },
        onClick = { viewModel.setSortMode(SortMode.CATEGORY) }
    )
    DropdownMenuItem(
        text = { Text("🛒 Por Pasillo") },
        onClick = { 
            // Si hay supermercado asignado → ordenar por pasillo
            // Si no → mostrar diálogo de selección de super
            viewModel.onSortByAisleClicked()
        }
    )
}
```

---

## 10. Diálogo de Selección de Supermercado

Crear diálogo para elegir supermercado cuando se pulsa "Por Pasillo":

```kotlin
@Composable
fun SelectSupermarketDialog(
    supermarkets: List<String> = listOf("