package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.ProductAisleMappingEntity

@Dao
interface ProductAisleMappingDao {
    @Query("""
        SELECT * FROM product_aisle_mappings 
        WHERE productNameNormalized = :name AND supermarketId = :superId
    """)
    suspend fun getMapping(name: String, superId: Long): ProductAisleMappingEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(mapping: ProductAisleMappingEntity)

    @Query("""
        UPDATE product_aisle_mappings 
        SET useCount = useCount + 1, lastUsed = :now 
        WHERE productNameNormalized = :name AND supermarketId = :superId
    """)
    suspend fun incrementUseCount(name: String, superId: Long, now: Long = System.currentTimeMillis())

    // Buscar similares
    @Query("""
        SELECT * FROM product_aisle_mappings 
        WHERE productNameNormalized LIKE '%' || :search || '%' 
        AND supermarketId = :superId
        ORDER BY useCount DESC LIMIT 1
    """)
    suspend fun findSimilar(search: String, superId: Long): ProductAisleMappingEntity?
}
