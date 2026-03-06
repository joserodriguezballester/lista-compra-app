package com.jose.listacompra.data.local

import android.icu.text.ListFormatter
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.CategoryDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductAisleMappingDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.ProductSupermarketAisleDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.dao.SupermarketAisleDao
import com.jose.listacompra.data.local.dao.SupermarketDao
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

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
    val supermarketId: Long? = null,       // ← FK a supermarket (nullable)
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



/* Database actualizada con versión 6 (para migración) */
@Database(
    entities = [
        ShoppingListEntity::class,
        CategoryEntity::class,
        AisleEntity::class,
        OfferEntity::class,
        ProductEntity::class,
        PurchaseHistoryEntity::class,
        ProductPriceHistoryEntity::class,
        ProductFrequencyEntity::class,
        SupermarketEntity::class,
        SupermarketAisleEntity::class,
        ProductAisleMappingEntity::class,
        ProductSupermarketAisleEntity::class
    ],
    version = 9,  // Incrementado para incluir nuevas tablas
    exportSchema = false
)

abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun categoryDao(): CategoryDao
    abstract fun aisleDao(): AisleDao
    abstract fun offerDao(): OfferDao
    abstract fun productDao(): ProductDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
    abstract fun productPriceHistoryDao(): ProductPriceHistoryDao
    abstract fun productFrequencyDao(): ProductFrequencyDao
    abstract fun supermarketDao(): SupermarketDao
    abstract fun supermarketAisleDao(): SupermarketAisleDao
    abstract fun productAisleMappingDao(): ProductAisleMappingDao
    abstract fun productSupermarketAisleDao(): ProductSupermarketAisleDao

    companion object {
        const val DATABASE_NAME = "shopping_list_db"

        val MIGRATION_6_8 = object : Migration(6, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migración de Categorías + AisleMap

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
