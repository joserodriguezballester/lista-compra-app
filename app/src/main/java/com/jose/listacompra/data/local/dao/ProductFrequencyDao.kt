package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity

@Dao
interface ProductFrequencyDao {
    @Query("SELECT * FROM product_frequency ORDER BY timesPurchased DESC")
    suspend fun getAllFrequencies(): List<ProductFrequencyEntity>

    @Query("SELECT * FROM product_frequency WHERE productName = :name")
    suspend fun getFrequencyForProduct(name: String): ProductFrequencyEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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