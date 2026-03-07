package com.jose.listacompra.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
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
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.CategoryEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductAisleMappingEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.ProductSupermarketAisleEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.data.local.entities.SupermarketAisleEntity
import com.jose.listacompra.data.local.entities.SupermarketEntity


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

  //  abstract fun productHistoryDao(): ProductHistoryDao

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
