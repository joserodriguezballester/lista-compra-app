package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity

@Dao
interface ProductPriceHistoryDao {
    
    @Query("""
        SELECT * FROM product_price_history 
        WHERE productName = :productName 
        ORDER BY fecha ASC
    """)
    suspend fun getPriceHistory(productName: String): List<ProductPriceHistoryEntity>
    
    @Query("""
        SELECT * FROM product_price_history 
        WHERE productName = :productName 
        ORDER BY fecha DESC 
        LIMIT :limit
    """)
    suspend fun getRecentPriceHistory(productName: String, limit: Int = 10): List<ProductPriceHistoryEntity>
    
    @Query("""
        SELECT 
            MIN(price) as minPrice,
            MAX(price) as maxPrice,
            AVG(price) as avgPrice,
            COUNT(*) as totalPurchases
        FROM product_price_history 
        WHERE productName = :productName
    """)
    suspend fun getPriceStats(productName: String): PriceStats?
    
    @Query("""
        SELECT price FROM product_price_history 
        WHERE productName = :productName 
        ORDER BY fecha DESC 
        LIMIT 1
    """)
    suspend fun getLastPrice(productName: String): Float?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(priceHistory: ProductPriceHistoryEntity)
    
    // ProductFrequencyDao methods
    @Query("SELECT * FROM product_frequency WHERE productName = :productName LIMIT 1")
    suspend fun getFrequency(productName: String): ProductFrequencyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrequency(frequency: ProductFrequencyEntity)
    
    @Query("SELECT * FROM product_frequency WHERE productName LIKE '%' || :query || '%' ORDER BY timesPurchased DESC LIMIT 10")
    suspend fun findSuggestions(query: String): List<ProductFrequencyEntity>
}

data class PriceStats(
    val minPrice: Float,
    val maxPrice: Float,
    val avgPrice: Float,
    val totalPurchases: Int
)