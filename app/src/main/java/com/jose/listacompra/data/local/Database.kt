package com.jose.listacompra.data.local

import androidx.room.*
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

// ==================== ENTIDADES ====================

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

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val aisleId: Long,
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
    @Query("SELECT * FROM products ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getAllProducts(): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE aisleId = :aisleId ORDER BY orderIndex ASC")
    suspend fun getProductsByAisle(aisleId: Long): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM products WHERE isPurchased = 1")
    suspend fun deletePurchasedProducts()
    
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    @Query("SELECT MAX(orderIndex) FROM products WHERE aisleId = :aisleId")
    suspend fun getMaxOrderIndexInAisle(aisleId: Long): Int?
}

@Database(
    entities = [AisleEntity::class, OfferEntity::class, ProductEntity::class, ProductHistoryEntity::class], 
    version = 3
)
abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun aisleDao(): AisleDao
    abstract fun offerDao(): OfferDao
    abstract fun productDao(): ProductDao
    abstract fun productHistoryDao(): ProductHistoryDao
    
    companion object {
        const val DATABASE_NAME = "shopping_list_db"
    }
}

// ==================== CONVERTIDORES ====================

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
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex
)
