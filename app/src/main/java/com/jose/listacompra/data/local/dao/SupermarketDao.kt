package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.SupermarketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupermarketDao {
    @Query("SELECT * FROM supermarkets ORDER BY name")
    fun getAllSupermarkets(): Flow<List<SupermarketEntity>>

    @Query("SELECT * FROM supermarkets ORDER BY id ASC")
    suspend fun getAllSupermarketsOnce(): List<SupermarketEntity>

    @Query("SELECT * FROM supermarkets WHERE id = :id")
    suspend fun getSupermarketById(id: Long): SupermarketEntity?

    @Query("SELECT * FROM supermarkets WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultSupermarket(): SupermarketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupermarket(supermarket: SupermarketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(supermarkets: List<SupermarketEntity>)

    @Query("INSERT OR IGNORE INTO supermarkets(id, name, emoji, isDefault) VALUES(:id, :name, :emoji, :isDefault)")
    suspend fun insertBuiltinSupermarket(id: Long, name: String, emoji: String, isDefault: Boolean)

    @Query("DELETE FROM supermarkets WHERE id = :id AND id NOT IN (0, 1, 2, 3, 4, 5)")
    suspend fun deleteSupermarket(id: Long)

    @Query("DELETE FROM supermarkets WHERE id NOT IN (0, 1, 2, 3, 4, 5)")
    suspend fun deleteCustomSupermarkets()

    @Query("DELETE FROM supermarkets WHERE id NOT IN (0, 1, 2, 3, 4, 5)")
    suspend fun deleteAll()
}
