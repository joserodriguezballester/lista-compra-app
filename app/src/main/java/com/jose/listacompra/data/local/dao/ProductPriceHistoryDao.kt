package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity

@Dao
interface ProductPriceHistoryDao {
    @Query("SELECT * FROM product_price_history WHERE productName = :name ORDER BY fecha DESC")
    suspend fun getPriceHistoryForProduct(name: String): List<ProductPriceHistoryEntity>

    @Query("SELECT * FROM product_price_history ORDER BY fecha DESC LIMIT 100")
    suspend fun getRecentPriceHistory(): List<ProductPriceHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPriceRecord(record: ProductPriceHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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