(@Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getAllProducts(listId: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND aisleId = :aisleId ORDER BY orderIndex ASC")
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<ProductEntity>

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

/* Database actualizada con versión 7 (incluye campos de foto) */
@Database(
    entities = [
        ShoppingListEntity::class,
        CategoryEntity::class,
        AisleEntity::class,
        OfferEntity::class,
        ProductEntity::class,
        PurchaseHistoryEntity::class,
        ProductPriceHistoryEntity::class,
        ProductFrequencyEntity::class
    ],
    version = 7 // ← Incrementado: añadidos campos photoUri, photoTimestamp, isPhotoUserSelected
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

    companion object {
        const val DATABASE_NAME = "shopping_list_db"

        /* Migración 6 → 7: Añadir campos de foto */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Añadir columna photoUri
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN photoUri TEXT DEFAULT NULL"
                )
                // Añadir columna photoTimestamp
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN photoTimestamp INTEGER DEFAULT NULL"
                )
                // Añadir columna isPhotoUserSelected
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN isPhotoUserSelected INTEGER DEFAULT 0"
                )
            }
        }
    }
}
