package com.jose.listacompra.data.local

import androidx.room.*
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

/*
 * VERSIÓN CON CATEGORÍAS Y FOTOS
 * - Nueva tabla CategoryEntity
 * - ProductEntity con categoryId, aisleId y CAMPOS DE FOTO
 * - Versión 7 de la base de datos
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
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: String = "ACTIVA"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val description: String = "",
    val orderIndex: Int = 0
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
    val categoryId: Long?,
    val aisleId: Long,
    val shoppingListId: Long,
    val quantity: Float,
    val estimatedPrice: Float?,
    val offerId: Long?,
    val finalPrice: Float?,
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int,
    // NUEVOS CAMPOS DE FOTO
    val photoUri: String? = null,
    val photoTimestamp: Long? = null,
    val isPhotoUserSelected: Boolean = false
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
    @Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getAllProducts(listId: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND aisleId = :aisleId ORDER BY orderIndex ASC")
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<ProductEntity>

    @Query