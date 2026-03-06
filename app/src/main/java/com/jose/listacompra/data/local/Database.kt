package com.jose.listacompra.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

/*
 * VERSIÓN CON CATEGORÍAS
 * - Nueva tabla CategoryEntity
 * - ProductEntity ahora tiene categoryId (nullable)
 * - Nuevas queries para agrupar por categoría
 */






// ==================== ENTIDADES ====================

@Entity(tableName = "purchase_history")
data class PurchaseHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val total: Float,
    val tienda: String = "Carrefour",
    val numProductos: Int,
    val ahorroTotal: Float = 0f,
    val ticketUrl: String? = null
)

@Entity(
    tableName = "product_price_history",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("purchaseId"), Index("productName")]
)
data class ProductPriceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val productName: String,
    val price: Float,
    val quantity: Int = 1,
    val aisle: String? = null,
    val fecha: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_frequency",
    indices = [Index("productName")]
)
data class ProductFrequencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val timesPurchased: Int = 1,
    val averageDaysBetween: Float? = null,
    val lastPurchaseDate: Long,
    val estimatedNextDate: Long? = null,
    val category: String? = null
)

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val supermarketId: Long?,       // ← FK a supermarket (nullable)
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: String = "ACTIVA"
)


/* NUEVO: Tabla de categorías */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,          // "Lácteos", "Bebidas", "Galletas"
    val emoji: String,         // "🥛", "🥤", "🍪"
    val description: String = "",
    val orderIndex: Int = 0    // Para ordenarlas
)

@Entity(tableName = "aisles")
data class AisleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val isDefault: Boolean
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val description: String,
    val isDefault: Boolean,
    val formula: String
)

/* MODIFICADO: ProductEntity ahora tiene categoryId */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("shoppingListId"), Index("categoryId")]
)
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
    val aisleMap: String? = null , // ← NUEVO: JSON {"carrefour":"Pasillo 3","mercadona":"Pasillo 2"}
    // NUEVOS CAMPOS PARA FOTO
    val photoUri: String? = null,      // URI de la imagen seleccionada
    val photoTimestamp: Long? = null,  // Para ordenar por recientes
    val isPhotoUserSelected: Boolean = false  // true = elegida por usuario, false = default



)

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
// ==================== DAOs ====================

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists WHERE estado = 'ACTIVA' ORDER BY fechaCreacion DESC")
    suspend fun getActiveLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists WHERE estado = 'ARCHIVADA' ORDER BY fechaCreacion DESC")
    suspend fun getArchivedLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists ORDER BY fechaCreacion DESC")
    suspend fun getAllLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: Long): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity): Long

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("UPDATE shopping_lists SET estado = 'ARCHIVADA' WHERE id = :listId")
    suspend fun archiveList(listId: Long)

    @Query("UPDATE shopping_lists SET estado = 'ACTIVA' WHERE id = :listId")
    suspend fun unarchiveList(listId: Long)
}

/* NUEVO: DAO de categorías */
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY orderIndex ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT MAX(orderIndex) FROM categories")
    suspend fun getMaxOrderIndex(): Int?

    @Update
    suspend fun updateCategories(categories: List<CategoryEntity>)
}

@Dao
interface AisleDao {
    @Query("SELECT * FROM aisles ORDER BY orderIndex ASC")
    suspend fun getAllAisles(): List<AisleEntity>

    @Query("SELECT * FROM aisles WHERE id = :id")
    suspend fun getAisleById(id: Long): AisleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAisle(aisle: AisleEntity): Long

    @Update
    suspend fun updateAisle(aisle: AisleEntity)

    @Delete
    suspend fun deleteAisle(aisle: AisleEntity)

    @Query("DELETE FROM aisles WHERE isDefault = 0")
    suspend fun deleteCustomAisles()

    @Query("SELECT MAX(orderIndex) FROM aisles")
    suspend fun getMaxOrderIndex(): Int?

    @Update
    suspend fun updateAisles(aisles: List<AisleEntity>)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers ORDER BY id ASC")
    suspend fun getAllOffers(): List<OfferEntity>

    @Query("SELECT * FROM offers WHERE id = :id")
    suspend fun getOfferById(id: Long): OfferEntity?

    @Query("SELECT * FROM offers WHERE isDefault = 1 ORDER BY id ASC")
    suspend fun getDefaultOffers(): List<OfferEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<OfferEntity>)

    @Update
    suspend fun updateOffer(offer: OfferEntity)

    @Delete
    suspend fun deleteOffer(offer: OfferEntity)

    @Query("DELETE FROM offers WHERE isDefault = 0")
    suspend fun deleteCustomOffers()

    @Query("SELECT COUNT(*) FROM offers")
    suspend fun getOfferCount(): Int
}

@Dao
interface ProductDao {
    /* Por pasillo (orden original) */
    @Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getAllProducts(listId: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND aisleId = :aisleId ORDER BY orderIndex ASC")
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<ProductEntity>

    /* NUEVO: Por categoría */
    @Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY categoryId ASC, orderIndex ASC")
    suspend fun getAllProductsByCategory(listId: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND categoryId = :categoryId ORDER BY orderIndex ASC")
    suspend fun getProductsByCategory(listId: Long, categoryId: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE shoppingListId = :listId AND isPurchased = 1")
    suspend fun deletePurchasedProducts(listId: Long)

    @Query("DELETE FROM products WHERE shoppingListId = :listId")
    suspend fun deleteAllProducts(listId: Long)

    @Query("SELECT MAX(orderIndex) FROM products WHERE shoppingListId = :listId AND aisleId = :aisleId")
    suspend fun getMaxOrderIndexInAisle(listId: Long, aisleId: Long): Int?

    @Query("SELECT MAX(orderIndex) FROM products WHERE shoppingListId = :listId AND categoryId = :categoryId")
    suspend fun getMaxOrderIndexInCategory(listId: Long, categoryId: Long?): Int?
}
@Dao
interface PurchaseHistoryDao {
    @Query("SELECT * FROM purchase_history ORDER BY fecha DESC")
    suspend fun getAllPurchases(): List<PurchaseHistoryEntity>

    @Query("SELECT * FROM purchase_history WHERE fecha >= :since ORDER BY fecha DESC")
    suspend fun getPurchasesSince(since: Long): List<PurchaseHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseHistoryEntity): Long

    @Query("SELECT * FROM purchase_history ORDER BY fecha DESC LIMIT 1")
    suspend fun getLastPurchase(): PurchaseHistoryEntity?

    @Query("SELECT AVG(total) FROM purchase_history")
    suspend fun getAveragePurchaseAmount(): Float?

    @Query("SELECT SUM(total) FROM purchase_history WHERE fecha >= :since")
    suspend fun getTotalSpentSince(since: Long): Float?
}
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
@Dao
interface ProductPriceHistoryDao {
    @Query("SELECT * FROM product_price_history WHERE productName = :name ORDER BY fecha DESC")
    suspend fun getPriceHistoryForProduct(name: String): List<ProductPriceHistoryEntity>

    @Query("SELECT * FROM product_price_history ORDER BY fecha DESC LIMIT 100")
    suspend fun getRecentPriceHistory(): List<ProductPriceHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceRecord(record: ProductPriceHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPriceRecords(records: List<ProductPriceHistoryEntity>)

    @Query("SELECT AVG(price) FROM product_price_history WHERE productName = :name")
    suspend fun getAveragePriceForProduct(name: String): Float?

    @Query("SELECT MIN(price) FROM product_price_history WHERE productName = :name")
    suspend fun getLowestPriceForProduct(name: String): Float?

    @Query("SELECT MAX(price) FROM product_price_history WHERE productName = :name")
    suspend fun getHighestPriceForProduct(name: String): Float?

    @Query("SELECT price FROM product_price_history WHERE productName = :name ORDER BY fecha DESC LIMIT 1")
    suspend fun getLastPriceForProduct(name: String): Float?
}

@Dao
interface ProductFrequencyDao {
    @Query("SELECT * FROM product_frequency ORDER BY timesPurchased DESC")
    suspend fun getAllFrequencies(): List<ProductFrequencyEntity>

    @Query("SELECT * FROM product_frequency WHERE productName = :name")
    suspend fun getFrequencyForProduct(name: String): ProductFrequencyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFrequency(frequency: ProductFrequencyEntity): Long

    @Query("UPDATE product_frequency SET timesPurchased = timesPurchased + 1, lastPurchaseDate = :date WHERE productName = :name")
    suspend fun incrementPurchaseCount(name: String, date: Long = System.currentTimeMillis())

    @Query("SELECT * FROM product_frequency WHERE estimatedNextDate <= :date AND estimatedNextDate > 0")
    suspend fun getProductsDueForPurchase(date: Long): List<ProductFrequencyEntity>

    @Query("SELECT * FROM product_frequency WHERE averageDaysBetween IS NOT NULL ORDER BY timesPurchased DESC LIMIT 20")
    suspend fun getMostFrequentProducts(): List<ProductFrequencyEntity>

    @Query("DELETE FROM product_frequency WHERE productName = :name")
    suspend fun deleteFrequency(name: String)
}

/* Database actualizada con versión 6 (para migración) */
@Database(
    entities = [
        ShoppingListEntity::class,
        CategoryEntity::class,        // ← NUEVO
        AisleEntity::class,
        OfferEntity::class,
        ProductEntity::class,
        PurchaseHistoryEntity::class,
        ProductPriceHistoryEntity::class,
        ProductFrequencyEntity::class
    ],
    version = 8,  // ← Incrementado para migración
    exportSchema = false
)
// Migración combinada

abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun categoryDao(): CategoryDao    // ← NUEVO
    abstract fun aisleDao(): AisleDao
    abstract fun offerDao(): OfferDao
    abstract fun productDao(): ProductDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
    abstract fun productPriceHistoryDao(): ProductPriceHistoryDao
    abstract fun productFrequencyDao(): ProductFrequencyDao

    companion object {
        const val DATABASE_NAME = "shopping_list_db"
  //  }
   // companion object {
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
}
