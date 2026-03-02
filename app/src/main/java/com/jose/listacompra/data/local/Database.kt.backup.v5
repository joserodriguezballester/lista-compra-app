package com.jose.listacompra.data.local

import androidx.room.*
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

// ==================== ENTIDADES ====================

@Entity(tableName = "purchase_history")
data class PurchaseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val total: Float,
    val tienda: String = "Carrefour",
    val numProductos: Int,
    val ahorroTotal: Float = 0f,
    val ticketUrl: String? = null // Ruta al PDF si se guarda
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,           // Nombre normalizado del producto
    val timesPurchased: Int = 1,       // Veces comprado
    val averageDaysBetween: Float? = null, // Días medios entre compras
    val lastPurchaseDate: Long,        // Última compra
    val estimatedNextDate: Long? = null, // Próxima compra estimada
    val category: String? = null       // Categoría/pasillo
)

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // Nombre de la lista (ej: "Carrefour Mislata")
    val fechaCreacion: Long = System.currentTimeMillis(),  // Timestamp automático
    val estado: String = "ACTIVA"  // "ACTIVA" o "ARCHIVADA"
)

@Entity(tableName = "aisles")
data class AisleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val isDefault: Boolean
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,        // Código corto: "3x2", "2nd_50", "custom"
    val name: String,        // Nombre visible: "3x2", "2ª unidad -50%"
    val description: String, // Descripción larga
    val isDefault: Boolean,  // true = predefinida, false = custom del usuario
    val formula: String      // Fórmula de cálculo (para referencia)
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shoppingListId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val shoppingListId: Long,    // FK a shopping_lists
    val quantity: Float,
    val estimatedPrice: Float?,  // Precio unitario normal
    val offerId: Long?,          // FK a offers (nullable)
    val finalPrice: Float?,      // Precio calculado con oferta aplicada
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int
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
    @Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getAllProducts(listId: Long): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND aisleId = :aisleId ORDER BY orderIndex ASC")
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<ProductEntity>
    
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
    version = 5
)
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
    }
}

// ==================== CONVERTIDORES ====================

fun ShoppingListEntity.toDomain(): com.jose.listacompra.domain.model.ShoppingList = com.jose.listacompra.domain.model.ShoppingList(
    id = this.id,
    name = this.name,
    fechaCreacion = this.fechaCreacion,
    estado = this.estado
)

fun com.jose.listacompra.domain.model.ShoppingList.toEntity(): ShoppingListEntity = ShoppingListEntity(
    id = this.id,
    name = this.name,
    fechaCreacion = this.fechaCreacion,
    estado = this.estado
)

fun AisleEntity.toDomain(): Aisle = Aisle(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    orderIndex = this.orderIndex,
    isDefault = this.isDefault
)

fun Aisle.toEntity(): AisleEntity = AisleEntity(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    orderIndex = this.orderIndex,
    isDefault = this.isDefault
)

fun OfferEntity.toDomain(): Offer = Offer(
    id = this.id,
    code = this.code,
    name = this.name,
    description = this.description,
    isDefault = this.isDefault,
    formula = this.formula
)

fun Offer.toEntity(): OfferEntity = OfferEntity(
    id = this.id,
    code = this.code,
    name = this.name,
    description = this.description,
    isDefault = this.isDefault,
    formula = this.formula
)

fun ProductEntity.toDomain(): Product = Product(
    id = this.id,
    name = this.name,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = this.id,
    name = this.name,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex
)
