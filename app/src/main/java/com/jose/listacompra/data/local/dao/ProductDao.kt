package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.entities.ProductEntity

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

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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

    /* NUEVO: Obtener siguiente orderIndex para una lista */
    @Query("SELECT IFNULL(MAX(orderIndex), -1) + 1 FROM products WHERE shoppingListId = :listId")
    suspend fun getNextOrderIndex(listId: Long): Int

    /* NUEVO: Buscar productos por nombre (para autocompletado) */
    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :query || '%' 
        AND shoppingListId = :listId
        ORDER BY name ASC
        LIMIT 10
    """)
    suspend fun searchByName(listId: Long, query: String): List<ProductEntity>


}