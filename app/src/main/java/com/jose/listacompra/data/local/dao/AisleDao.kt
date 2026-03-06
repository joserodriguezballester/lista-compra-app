package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.AisleEntity

@Dao
interface AisleDao {
    @Query("SELECT * FROM aisles ORDER BY orderIndex ASC")
    suspend fun getAllAisles(): List<AisleEntity>

    @Query("SELECT * FROM aisles WHERE id = :id")
    suspend fun getAisleById(id: Long): AisleEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAisle(aisle: AisleEntity): Long

    @Update
    suspend fun updateAisle(aisle: AisleEntity)

    @Delete
    suspend fun deleteAisle(aisle: AisleEntity)

    @Query("DELETE FROM aisles WHERE isDefault = 0")
    suspend fun deleteCustomAisles()

    @Query("SELECT MAX(orderIndex) FROM aisles")
    suspend fun getMaxOrderIndex(): Int?

    @Update
    suspend fun updateAisles(aisles: List<AisleEntity>)
}