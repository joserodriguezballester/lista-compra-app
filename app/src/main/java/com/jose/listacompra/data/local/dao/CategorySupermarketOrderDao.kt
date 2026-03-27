package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jose.listacompra.data.local.entities.CategorySupermarketOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategorySupermarketOrderDao {
    
    @Query("""
        SELECT * FROM category_supermarket_orders 
        WHERE supermarketId = :supermarketId 
        ORDER BY orderIndex ASC
    """)
    fun getOrdersBySupermarket(supermarketId: Long): Flow<List<CategorySupermarketOrderEntity>>
    
    @Query("""
        SELECT * FROM category_supermarket_orders 
        WHERE supermarketId = :supermarketId 
        ORDER BY orderIndex ASC
    """)
    suspend fun getOrdersBySupermarketOnce(supermarketId: Long): List<CategorySupermarketOrderEntity>
    
    @Query("""
        SELECT * FROM category_supermarket_orders 
        WHERE supermarketId = :supermarketId AND categoryId = :categoryId
        LIMIT 1
    """)
    suspend fun getOrder(supermarketId: Long, categoryId: Long): CategorySupermarketOrderEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CategorySupermarketOrderEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<CategorySupermarketOrderEntity>)
    
    @Query("DELETE FROM category_supermarket_orders WHERE supermarketId = :supermarketId")
    suspend fun deleteOrdersForSupermarket(supermarketId: Long)
    
    @Query("DELETE FROM category_supermarket_orders")
    suspend fun deleteAll()
    
    @Query("SELECT MAX(orderIndex) FROM category_supermarket_orders WHERE supermarketId = :supermarketId")
    suspend fun getMaxOrderIndex(supermarketId: Long): Int?
    
    @Transaction
    suspend fun updateOrdersForSupermarket(supermarketId: Long, orders: List<CategorySupermarketOrderEntity>) {
        deleteOrdersForSupermarket(supermarketId)
        insertAll(orders)
    }
}
