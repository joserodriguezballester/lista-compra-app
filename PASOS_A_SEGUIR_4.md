# Pasos a Seguir 4 - Auto-Asignar Pasillo al Añadir Producto

## Problema
Al añadir producto (voz, historial, manual), no se asigna el pasillo correspondiente al supermercado de la lista.

## Solución
Lógica automática que asigne el pasillo correcto según:
1. **Supermercado asignado** a la lista
2. **Historial** de ese producto en ese supermercado
3. **Categoría** → pasillo por defecto

---

## 1. Añadir Tabla de Mapeo Historial (Opcional pero recomendado)

Si quieres recordar "leche siempre la pongo en Pasillo 3 del Carrefour":

### Database.kt - Nueva Entidad

```kotlin
/**
 * Guarda qué pasillo se usó para cada producto en cada supermercado
 * Ej: "Leche" + "Carrefour" = "Pasillo 3"
 */
@Entity(
    tableName = "product_supermarket_aisle",
    primaryKeys = ["productName", "supermarket"],
    indices = [Index("supermarket")]
)
data class ProductSupermarketAisleEntity(
    val productName: String,     // "Leche Hacendado" (normalized)
    val supermarket: String,     // "carrefour", "mercadona"
    val aisleName: String,       // "Pasillo 3 - Lácteos"
    val aisleId: Long?,          // Si tienes IDs de pasillos
    val lastUsed: Long = System.currentTimeMillis()  // Para ordenar por frecuencia
)

@Dao
interface ProductSupermarketAisleDao {
    
    @Query("""
        SELECT * FROM product_supermarket_aisle 
        WHERE productName = :name AND supermarket = :supermarket
    """)
    suspend fun getAisleForProduct(
        name: String, 
        supermarket: String
    ): ProductSupermarketAisleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAisle(mapping: ProductSupermarketAisleEntity)
    
    @Query("""
        SELECT aisleName FROM product_supermarket_aisle 
        WHERE productName LIKE '%' || :search || '%' 
        AND supermarket = :supermarket
        ORDER BY lastUsed DESC 
        LIMIT 1
    """)
    suspend fun findSimilarProductAisle(
        search: String, 
        supermarket: String
    ): String?
}
```

---

## 2. Repository - Método de Inserción Inteligente

```kotlin
class ShoppingListRepository(context: Context) {
    
    private val db = ...
    private val productDao = db.productDao()
    private val productSupermarketDao = db.productSupermarketAisleDao()
    private val categoryDao = db.categoryDao()
    
    private val gson = Gson()
    
    /**
     * Añade producto asignando AUTOMÁTICAMENTE el pasillo correcto
     */
    suspend fun addProductWithAutoAisle(
        productName: String,
        quantity: Float = 1f,
        listId: Long,
        categoryId: Long? = null,
        estimatedPrice: Float? = null
    ): Long {
        
        // 1. Obtener información de la lista
        val shoppingList = shoppingListDao.getListById(listId)?.toDomain()
            ?: throw IllegalStateException("Lista no encontrada")
        
        val supermarket = shoppingList.supermarket  // "carrefour", "mercadona", o null
        
        // 2. Normalizar nombre del producto (para búsquedas)
        val normalizedName = productName.trim().uppercase()
        
        // 3. Buscar pasillo automático
        val aisleAssignment = findBestAisle(
            productName = normalizedName,
            supermarket = supermarket,
            categoryId = categoryId
        )
        
        // 4. Construir el aisleMap
        val aisleMap = buildAisleMap(
            supermarket = supermarket,
            aisleName = aisleAssignment?.aisleName,
            existingMap = null
        )
        
        // 5. Crear producto
        val product = ProductEntity(
            id = 0,
            name = productName.trim(),
            categoryId = categoryId,
            aisleId = null,  // Deprecated
            shoppingListId = listId,
            quantity = quantity,
            estimatedPrice = estimatedPrice,
            offerId = null,
            finalPrice = null,
            isPurchased = false,
            notes = "",
            orderIndex = getNextOrderIndex(listId),
            aisleMap = aisleMap
        )
        
        val productId = productDao.insertProduct(product)
        
        // 6. Guardar asignación para futuro (si tenemos supermercado)
        if (supermarket != null && aisleAssignment != null) {
            productSupermarketDao.saveAisle(
                ProductSupermarketAisleEntity(
                    productName = normalizedName,
                    supermarket = supermarket,
                    aisleName = aisleAssignment.aisleName,
                    aisleId = aisleAssignment.aisleId
                )
            )
        }
        
        return productId
    }
    
    /**
     * Lógica principal de búsqueda de pasillo
     */
    private suspend fun findBestAisle(
        productName: String,
        supermarket: String?,
        categoryId: Long?
    ): AisleAssignment? {
        
        // Si no hay supermercado asignado a la lista → usar solo categoría
        if (supermarket == null) {
            return categoryId?.let { getDefaultAisleForCategory(it) }
        }
        
        // PRIORIDAD 1: Producto exacto ya usado en este super → mismo pasillo
        val exactMatch = productSupermarketDao.getAisleForProduct(productName, supermarket)
        if (exactMatch != null) {
            return AisleAssignment(
                aisleName = exactMatch.aisleName,
                aisleId = exactMatch.aisleId,
                source = "history_exact"
            )
        }
        
        // PRIORIDAD 2: Producto similar (búsqueda parcial)
        // Ej: busca "Leche" y encuentra "Leche Hacendado" → mismo pasillo
        val productWords = productName.split(" ")
        for (word in productWords) {
            if (word.length >= 3) {  // Palabras de 3+ letras
                val similar = productSupermarketDao.findSimilarProductAisle(word, supermarket)
                if (similar != null) {
                    return AisleAssignment(
                        aisleName = similar,
                        aisleId = null,
                        source = "history_similar"
                    )
                }
            }
        }
        
        // PRIORIDAD 3: Usar categoría → pasillo por defecto de ese super
        return categoryId?.let { 
            getAisleForCategoryInSupermarket(it, supermarket)
        }
    }
    
    /**
     * Construye el JSON del aisleMap
     */
    private fun buildAisleMap(
        supermarket: String?,
        aisleName: String?,
        existingMap: String?
    ): String {
        val map = existingMap?.let { 
            gson.fromJson(it, Map::class.java) as MutableMap<String, String>
        } ?: mutableMapOf()
        
        // Añadir o actualizar el pasillo para este supermercado
        if (supermarket != null && aisleName != null) {
            map[supermarket] = aisleName
        }
        
        return gson.toJson(map)
    }
    
    data class AisleAssignment(
        val aisleName: String,
        val aisleId: Long?,
        val source: String  // "history_exact", "history_similar", "category_default"
    )
    
    private suspend fun getDefaultAisleForCategory(categoryId: Long): AisleAssignment? {
        // Ej: Lácteos → "Pasillo de Lácteos"
        val category = categoryDao.getCategoryById(categoryId)
        return category?.let {
            AisleAssignment(
                aisleName = "Sección ${it.name}",
                aisleId = null,
                source = "category_default"
            )
        }
    }
    
    private suspend fun getAisleForCategoryInSupermarket(
        categoryId: Long, 
        supermarket: String
    ): AisleAssignment? {
        // Aquí necesitas una tabla de mapeo Categoría → Pasillo por Supermer