package com.jose.listacompra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jose.listacompra.data.local.converters.Converters
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.ArticuloDao
import com.jose.listacompra.data.local.dao.ArticuloSupermarketDefaultDao
import com.jose.listacompra.data.local.dao.CategoryDao
import com.jose.listacompra.data.local.dao.CategorySupermarketOrderDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.dao.SupermarketDao
import com.jose.listacompra.data.local.dao.TicketDao
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.ArticuloEntity
import com.jose.listacompra.data.local.entities.ArticuloSupermarketDefaultEntity
import com.jose.listacompra.data.local.entities.CategoryEntity
import com.jose.listacompra.data.local.entities.CategorySupermarketOrderEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductHistoryEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.data.local.entities.SupermarketEntity
import com.jose.listacompra.data.local.entities.TicketEntity
import com.jose.listacompra.data.local.entities.TicketLineEntity

@Database(
    entities = [
        ArticuloEntity::class,
        ShoppingListEntity::class,
        AisleEntity::class,
        OfferEntity::class,
        ProductEntity::class,
        ProductHistoryEntity::class,
        PurchaseHistoryEntity::class,
        ProductPriceHistoryEntity::class,
        ProductFrequencyEntity::class,
        SupermarketEntity::class,
        CategoryEntity::class,
        ArticuloSupermarketDefaultEntity::class,
        CategorySupermarketOrderEntity::class,
        TicketEntity::class,
        TicketLineEntity::class
    ],
    version = 14,
    exportSchema = false
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
    abstract fun articuloDao(): ArticuloDao
    abstract fun supermarketDao(): SupermarketDao
    abstract fun categoryDao(): CategoryDao
    abstract fun articuloSupermarketDefaultDao(): ArticuloSupermarketDefaultDao
    abstract fun categorySupermarketOrderDao(): CategorySupermarketOrderDao
    abstract fun ticketDao(): TicketDao

    companion object {
        const val DATABASE_NAME = "shopping_list_db"

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN photoUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE products ADD COLUMN ean TEXT DEFAULT NULL")
            }
        }
        
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS supermarkets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        emoji TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL
                    )
                """)
                
                db.execSQL("ALTER TABLE aisles ADD COLUMN supermarketId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE products ADD COLUMN articuloId INTEGER")
                db.execSQL("ALTER TABLE products ADD COLUMN supermarketId INTEGER NOT NULL DEFAULT 1")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS articulo_supermarket_defaults (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        articuloId INTEGER NOT NULL,
                        supermarketId INTEGER NOT NULL,
                        aisleId INTEGER NOT NULL,
                        FOREIGN KEY(articuloId) REFERENCES articulos(id) ON DELETE CASCADE,
                        FOREIGN KEY(supermarketId) REFERENCES supermarkets(id) ON DELETE CASCADE,
                        FOREIGN KEY(aisleId) REFERENCES aisles(id) ON DELETE CASCADE
                    )
                """)
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_aisles_supermarketId ON aisles(supermarketId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_products_articuloId ON products(articuloId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_products_supermarketId ON products(supermarketId)")
            }
        }
        
        // Migración 11→12: Añadir tabla de orden de categorías por supermercado
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS category_supermarket_orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        supermarketId INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE,
                        FOREIGN KEY(supermarketId) REFERENCES supermarkets(id) ON DELETE CASCADE
                    )
                """)
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_category_supermarket_orders_categoryId ON category_supermarket_orders(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_category_supermarket_orders_supermarketId ON category_supermarket_orders(supermarketId)")
            }
        }
        
        // Migración 13→14: Añadir tablas de tickets
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tabla de tickets
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tickets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fecha INTEGER NOT NULL,
                        supermarketId INTEGER,
                        supermarketName TEXT,
                        total REAL NOT NULL,
                        subtotal REAL,
                        descuentos REAL,
                        numProductos INTEGER NOT NULL,
                        socioClub TEXT,
                        formaPago TEXT,
                        pdfPath TEXT,
                        importado INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(supermarketId) REFERENCES supermarkets(id) ON DELETE SET NULL
                    )
                """)
                
                // Tabla de líneas de ticket
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ticket_lines (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ticketId INTEGER NOT NULL,
                        nombreOriginal TEXT NOT NULL,
                        nombreNormalizado TEXT NOT NULL,
                        cantidad INTEGER NOT NULL,
                        precioUnitario REAL NOT NULL,
                        precioTotal REAL NOT NULL,
                        articuloId INTEGER,
                        categoriaId INTEGER,
                        esDescuento INTEGER NOT NULL DEFAULT 0,
                        codigoPromocion TEXT,
                        notas TEXT,
                        confirmado INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(ticketId) REFERENCES tickets(id) ON DELETE CASCADE,
                        FOREIGN KEY(articuloId) REFERENCES articulos(id) ON DELETE SET NULL
                    )
                """)
                
                // Índices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tickets_supermarketId ON tickets(supermarketId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tickets_fecha ON tickets(fecha)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ticket_lines_ticketId ON ticket_lines(ticketId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ticket_lines_articuloId ON ticket_lines(articuloId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ticket_lines_nombreNormalizado ON ticket_lines(nombreNormalizado)")
            }
        }
    }
}
