package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getAllProducts(listId: Long): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND supermarketId = :supermarketId ORDER BY aisleId ASC, orderIndex ASC")
    suspend fun getProductsBySupermarket(listId: Long, supermarketId: Long): List<ProductEntity>

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

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Query("SELECT MAX(orderIndex) FROM products WHERE shoppingListId = :listId AND aisleId = :aisleId")
    suspend fun getMaxOrderIndexInAisle(listId: Long, aisleId: Long): Int?
    
    @Query("SELECT MAX(orderIndex) FROM products WHERE shoppingListId = :listId")
    suspend fun getMaxOrderIndex(listId: Long): Int?

    @Query("UPDATE products SET photoUri = :photoUri WHERE id = :productId")
    suspend fun updatePhotoUri(productId: Long, photoUri: String?)

    @Query("UPDATE products SET ean = :ean WHERE id = :productId")
    suspend fun updateEan(productId: Long, ean: String?)

    @Query("SELECT * FROM products WHERE shoppingListId = :listId ORDER BY aisleId ASC, orderIndex ASC")
    fun getProductsByListFlow(listId: Long): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND supermarketId = :supermarketId ORDER BY aisleId ASC, orderIndex ASC")
    fun getProductsBySupermarketFlow(listId: Long, supermarketId: Long): Flow<List<ProductEntity>>
    
    // T4: Productos del supermercado X + productos "Cualquiera" (supermarketId = 0)
    @Query("SELECT * FROM products WHERE shoppingListId = :listId AND (supermarketId = :supermarketId OR supermarketId = 0) ORDER BY aisleId ASC, orderIndex ASC")
    fun getProductsBySupermarketOrAnyFlow(listId: Long, supermarketId: Long): Flow<List<ProductEntity>>
    
    @Query("UPDATE products SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchased(id: Long, isPurchased: Boolean)

    @Query("UPDATE products SET photoUri = :photoUri WHERE id = :productId")
    suspend fun updateProductPhoto(productId: Long, photoUri: String?)
}
