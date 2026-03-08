package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.ProductHistoryEntity

@Dao
interface ProductHistoryDao {

    @Query("SELECT * FROM product_history WHERE name LIKE :query || '%' ORDER BY usageCount DESC, lastUsed DESC LIMIT 5")
    suspend fun findSuggestions(query: String): List<ProductHistoryEntity>

    @Query("SELECT * FROM product_history WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): ProductHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(history: ProductHistoryEntity): Long

    @Query("UPDATE product_history SET usageCount = usageCount + 1, lastUsed = :timestamp, lastQuantity = :quantity, lastPrice = :price WHERE name = :name")
    suspend fun updateUsage(name: String, quantity: Float, price: Float?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM product_history ORDER BY usageCount DESC LIMIT 20")
    suspend fun getMostFrequent(): List<ProductHistoryEntity>
}