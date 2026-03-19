package com.jose.listacompra.data.local.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.entities.ArticuloEntity
import kotlinx.coroutines.flow.Flow

interface ArticuloDao {

    @Query("SELECT * FROM articulos ORDER BY name ASC")
    fun getAllArticulos(): Flow<List<ArticuloEntity>>

    @Query("SELECT * FROM articulos WHERE id = :id")
    suspend fun getArticuloById(id: Long): ArticuloEntity?

    // --- OPERACIONES DE GESTIÓN (TECLADO/EDICIÓN) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticulo(articulo: ArticuloEntity): Long

    @Update
    suspend fun updateArticulo(articulo: ArticuloEntity)

    @Delete
    suspend fun deleteArticulo(articulo: ArticuloEntity)
// --- CONSULTAS PARA EL SCANNER ---

    @Query("SELECT * FROM articulos WHERE ean = :ean LIMIT 1")
    suspend fun getArticuloByEan(ean: String): ArticuloEntity?

    // --- CONSULTAS PARA LA VOZ Y BUSCADOR ---

    // Busca coincidencias en el nombre (ej: "leche")
    @Query("SELECT * FROM articulos WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
   suspend fun searchArticulosByName(query: String): List<ArticuloEntity>
}