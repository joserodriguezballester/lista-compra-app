package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.entities.SupermarketAisleEntity

@Dao
interface SupermarketAisleDao {
    @Query("SELECT * FROM supermarket_aisles WHERE supermarketId = :superId ORDER BY orderIndex ASC")
    suspend fun getBySupermarket(superId: Long): List<SupermarketAisleEntity>

    @Query("SELECT * FROM supermarket_aisles WHERE id = :id")
    suspend fun getById(id: Long): SupermarketAisleEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(aisle: SupermarketAisleEntity): Long

    @Update
    suspend fun update(aisle: SupermarketAisleEntity)

    @Delete
    suspend fun delete(aisle: SupermarketAisleEntity)

    @Update
    suspend fun updateOrder(aisles: List<SupermarketAisleEntity>)

    // Buscar pasillo que contenga una categoría específica
    @Query("""
        SELECT * FROM supermarket_aisles 
        WHERE supermarketId = :superId
        AND categoryIds LIKE '%' || :categoryId || '%'
        LIMIT 1
    """)
    suspend fun findByCategory(superId: Long, categoryId: String): SupermarketAisleEntity?
}