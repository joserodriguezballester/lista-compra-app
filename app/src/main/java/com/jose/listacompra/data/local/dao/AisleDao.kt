package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.entities.AisleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AisleDao {
    @Query("SELECT * FROM aisles ORDER BY orderIndex ASC")
    suspend fun getAllAisles(): List<AisleEntity>
    
    @Query("SELECT * FROM aisles WHERE supermarketId = :supermarketId ORDER BY orderIndex ASC")
    suspend fun getAislesBySupermarket(supermarketId: Long): List<AisleEntity>
    
    @Query("SELECT * FROM aisles WHERE supermarketId = :supermarketId ORDER BY orderIndex ASC")
    fun getAislesBySupermarketFlow(supermarketId: Long): Flow<List<AisleEntity>>

    @Query("SELECT * FROM aisles WHERE id = :id")
    suspend fun getAisleById(id: Long): AisleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAisle(aisle: AisleEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aisles: List<AisleEntity>)

    @Update
    suspend fun updateAisle(aisle: AisleEntity)

    @Delete
    suspend fun deleteAisle(aisle: AisleEntity)

    @Query("DELETE FROM aisles WHERE isDefault = 0")
    suspend fun deleteCustomAisles()

    @Query("SELECT MAX(orderIndex) FROM aisles WHERE supermarketId = :supermarketId")
    suspend fun getMaxOrderIndex(supermarketId: Long): Int?

    @Update
    suspend fun updateAisles(aisles: List<AisleEntity>)
    
    @Query("DELETE FROM aisles")
    suspend fun deleteAll()
}
