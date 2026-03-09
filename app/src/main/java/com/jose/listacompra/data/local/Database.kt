package com.jose.listacompra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jose.listacompra.data.local.converters.Converters
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductHistoryEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        AisleEntity::class,
        OfferEntity::class,
        ProductEntity::class,
        ProductHistoryEntity::class,
        PurchaseHistoryEntity::class,
        ProductPriceHistoryEntity::class,
        ProductFrequencyEntity::class
    ],
    version = 7
)


@TypeConverters(Converters::class)
abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun aisleDao(): AisleDao
    abstract fun offerDao(): OfferDao
    abstract fun productDao(): ProductDao
    abstract fun productHistoryDao(): ProductHistoryDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
    abstract fun productPriceHistoryDao(): ProductPriceHistoryDao
    abstract fun productFrequencyDao(): ProductFrequencyDao

    companion object {
        const val DATABASE_NAME = "shopping_list_db"

        // Agregar migración:
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE shopping_lists ADD COLUMN supermarketId INTEGER DEFAULT NULL"
                )
            }
        }
    }


}

