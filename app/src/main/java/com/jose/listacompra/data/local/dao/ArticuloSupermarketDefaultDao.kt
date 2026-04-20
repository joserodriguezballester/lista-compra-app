package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.ArticuloSupermarketDefaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticuloSupermarketDefaultDao {
    @Query("SELECT * FROM articulo_supermarket_defaults WHERE articuloId = :articuloId AND supermarketId = :supermarketId LIMIT 1")
    suspend fun getDefaultAisle(articuloId: Long, supermarketId: Long): ArticuloSupermarketDefaultEntity?
    
    @Query("SELECT * FROM articulo_supermarket_defaults WHERE articuloId = :articuloId")
    fun getDefaultsForArticulo(articuloId: Long): Flow<List<ArticuloSupermarketDefaultEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ArticuloSupermarketDefaultEntity): Long
    
    @Query("DELETE FROM articulo_supermarket_defaults WHERE articuloId = :articuloId AND supermarketId = :supermarketId")
    suspend fun deleteDefault(articuloId: Long, supermarketId: Long)
    
    @Query("DELETE FROM articulo_supermarket_defaults")
    suspend fun deleteAll()

    @Query("SELECT * FROM articulo_supermarket_defaults ORDER BY articuloId ASC, supermarketId ASC")
    suspend fun getAllOnce(): List<ArticuloSupermarketDefaultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(defaults: List<ArticuloSupermarketDefaultEntity>)
}
