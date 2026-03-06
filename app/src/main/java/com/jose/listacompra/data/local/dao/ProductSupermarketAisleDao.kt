package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.ProductSupermarketAisleEntity

@Dao
interface ProductSupermarketAisleDao {

    @Query("""
        SELECT * FROM product_supermarket_aisle 
        WHERE productName = :name AND supermarket = :supermarket
    """)
    suspend fun getAisleForProduct(
        name: String,
        supermarket: String
    ): ProductSupermarketAisleEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveAisle(mapping: ProductSupermarketAisleEntity)

    @Query("""
        SELECT aisleName FROM product_supermarket_aisle 
        WHERE productName LIKE '%' || :search || '%' 
        AND supermarket = :supermarket
        ORDER BY lastUsed DESC 
        LIMIT 1
    """)
    suspend fun findSimilarProductAisle(
        search: String,
        supermarket: String
    ): String?
}