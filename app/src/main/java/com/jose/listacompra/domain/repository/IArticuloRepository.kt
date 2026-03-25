package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Articulo
import kotlinx.coroutines.flow.Flow

interface IArticuloRepository {
    fun getAllArticulos(): Flow<List<Articulo>>
    suspend fun getArticuloByEan(ean: String): Articulo?
    suspend fun getArticuloById(id: Long): Articulo?
    suspend fun searchArticulos(query: String): List<Articulo>
    suspend fun saveArticulo(articulo: Articulo)
    suspend fun deleteArticulo(articulo: Articulo)
    suspend fun updateArticulo(articulo: Articulo)
}