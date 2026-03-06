# Pasos a Seguir 6 - CRUD de Pasillos por Supermercado

## Problema Actual
Los pasillos son genéricos. Deberían ser específicos de cada supermercado.

## Nuevo Modelo

```
Supermercado (Carrefour, Mercadona...)
    ↓ (1:N)
Pasillos del Supermercado (Pasillo 1, Pasillo 2...)
    ↓ (N:M vía categorías)
Categorías que contiene cada pasillo
```

---

## 1. Nuevas Entidades en Database.kt

```kotlin
// ==================== SUPERMERCADO ====================

@Entity(tableName = "supermarkets")
data class SupermarketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,              // "Carrefour"
    val displayName: String,       // "Carrefour La Alberca"
    val emoji: String = "🏪",
    val isDefault: Boolean = false, // Uno por defecto
    val orderIndex: Int = 0
)

// ==================== PASILLO (AHORA CON SUPER) ====================

@Entity(
    tableName = "supermarket_aisles",
    foreignKeys = [
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supermarketId")]
)
data class SupermarketAisleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supermarketId: Long,       // FK a supermarket
    val name: String,               // "Pasillo 3 - Lácteos y yogures"
    val emoji: String = "🥛",
    val orderIndex: Int = 0,        // Orden dentro de ese super
    val categoryIds: String?,        // JSON [1, 2, 3] - categorías que contiene
    val isDefault: Boolean = true    // ¿Es pasillo del sistema o creado por user?
)

// ==================== LISTA AHORA APUNTA A SUPER ====================

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val supermarketId: Long?,       // ← FK a supermarket (nullable)
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: String = "ACTIVA"
)

// ==================== MAPEO PRODUCTO-PASILLO-POR-SUPER ====================

/**
 * Guarda dónde se encuentra cada producto en cada supermercado
 * Ej: "Leche" en "Carrefour" = Pasillo 3 (id: 15)
 */
@Entity(
    tableName = "product_aisle_mappings",
    primaryKeys = ["productNameNormalized", "supermarketId"],
    indices = [Index("supermarketId"), Index("productNameNormalized")]
)
data class ProductAisleMappingEntity(
    val productNameNormalized: String,  // "LECHE ENTERA" (uppercase para búsqueda)
    val supermarketId: Long,
    val aisleId: Long,                   // FK a supermarket_aisles
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 1                // Para ordenar por frecuencia
)
```

---

## 2. DAOs

```kotlin
@Dao
interface SupermarketDao {
    @Query("SELECT * FROM supermarkets ORDER BY orderIndex ASC")
    suspend fun getAll(): List<SupermarketEntity>
    
    @Query("SELECT * FROM supermarkets WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): SupermarketEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supermarket: SupermarketEntity): Long
    
    @Update
    suspend fun update(supermarket: SupermarketEntity)
    
    @Delete
    suspend fun delete(supermarket: SupermarketEntity)
}

@Dao
interface SupermarketAisleDao {
    @Query("SELECT * FROM supermarket_aisles WHERE supermarketId = :superId ORDER BY orderIndex ASC")
    suspend fun getBySupermarket(superId: Long): List<SupermarketAisleEntity>
    
    @Query("SELECT * FROM supermarket_aisles WHERE id = :id")
    suspend fun getById(id: Long): SupermarketAisleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aisle: SupermarketAisleEntity): Long
    
    @Update
    suspend fun update(aisle: SupermarketAisleEntity)
    
    @Delete
    suspend fun delete(aisle: SupermarketAisleEntity)
    
    @Update
    suspend fun updateOrder(aisles: List<SupermarketAisleEntity>)
    
    // Buscar pasillo que contenga una categoría específica
    @Query("""
        SELECT * FROM supermarket_aisles 
        WHERE supermarketId = :superId 
        AND categoryIds LIKE '%' || :categoryId || '%'
        LIMIT 1
    """)
    suspend fun findByCategory(superId: Long, categoryId: String): SupermarketAisleEntity?
}

@Dao
interface ProductAisleMappingDao {
    @Query("""
        SELECT * FROM product_aisle_mappings 
        WHERE productNameNormalized = :name AND supermarketId = :superId
    """)
    suspend fun getMapping(name: String, superId: Long): ProductAisleMappingEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: ProductAisleMappingEntity)
    
    @Query("""
        UPDATE product_aisle_mappings 
        SET useCount = useCount + 1, lastUsed = :now 
        WHERE productNameNormalized = :name AND supermarketId = :superId
    """)
    suspend fun incrementUseCount(name: String, superId: Long, now: Long = System.currentTimeMillis())
    
    // Buscar similares
    @Query("""
        SELECT * FROM product_aisle_mappings 
        WHERE productNameNormalized LIKE '%' || :search || '%' 
        AND supermarketId = :superId
        ORDER BY useCount DESC LIMIT 1
    """)
    suspend fun findSimilar(search: String, superId: Long): ProductAisleMappingEntity?
}
```

---

## 3. Modelos de Dominio

```kotlin
// Supermarket.kt
data class Supermarket(
    val id: Long = 0,
    val name: String,
    val displayName: String,
    val emoji: String = "🏪",
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
) {
    companion object {
        fun getDefaults(): List<Supermarket> = listOf(
            Supermarket(1, "carrefour", "Carrefour", "🛒", true),
            Supermarket(2, "mercadona", "Mercadona", "🍅"),
            Supermarket(3, "lidl", "Lidl", "🥨"),
            Supermarket(4, "aldi", "Aldi", "🥐")
        )
    }
}

// SupermarketAisle.kt
data class SupermarketAisle(
    val id: Long = 0,
    val supermarketId: Long,
    val name: String,
    val emoji: String = "🛒",
    val orderIndex: Int = 0,
    val categoryIds: List<Long> = emptyList(),  // Categorías que contiene
    val isDefault: Boolean = true
)

// ProductAisleMapping.kt
data class ProductAisleMapping(
    val productNameNormalized: String,
    val supermarketId: Long,
    val aisleId: Long,
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 1
)
```

---

## 4. Repository - Lógica de Asignación Inteligente

```kotlin
class ShoppingListRepository(context: Context) {
    
    private val supermarketDao = db.supermarketDao()
    private val supermarketAisleDao = db.supermarketAisleDao()
    private val productAisleMappingDao = db.productAisleMappingDao()
    
    // ==================== CRUD SUPERMERCADOS ====================
    
    suspend fun getAllSupermarkets(): List<Supermarket> = 
        supermarketDao.getAll().map { it.toDomain() }
    
    suspend fun addSupermarket(name: String, displayName: String): Long {
        val maxOrder = supermarketDao.getAll().maxOfOrNull { it.orderIndex } ?: -1
        return supermarketDao.insert(
            SupermarketEntity(
                name = name.lowercase(),
                displayName = displayName,
                orderIndex = maxOrder + 1
            )
        )
    }
    
    suspend fun deleteSupermarket(id: Long): Boolean {
        val supermarket = supermarketDao.getById(id) ?: return false
        if (supermarket.isDefault) return false  // No borrar defaults
        supermarketDao.delete(supermarket)
        return true
