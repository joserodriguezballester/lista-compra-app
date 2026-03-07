package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.entities.SupermarketEntity

@Dao
interface SupermarketDao {
    @Query("SELECT * FROM supermarkets ORDER BY orderIndex ASC")
    suspend fun getAll(): List<SupermarketEntity>

    @Query("SELECT * FROM supermarkets WHERE id = :id")
    suspend fun getById(id: Long): SupermarketEntity?

    @Query("SELECT * FROM supermarkets WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): SupermarketEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(supermarket: SupermarketEntity): Long

    @Update
    suspend fun update(supermarket: SupermarketEntity)

    @Delete
    suspend fun delete(supermarket: SupermarketEntity)
}