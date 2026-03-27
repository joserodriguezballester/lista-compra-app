package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.ArticuloSupermarketDefault
import kotlinx.coroutines.flow.Flow

interface IArticuloSupermarketDefaultRepository {
    suspend fun getDefaultAisle(articuloId: Long, supermarketId: Long): ArticuloSupermarketDefault?
    fun getDefaultsForArticulo(articuloId: Long): Flow<List<ArticuloSupermarketDefault>>
    suspend fun insertOrUpdate(default: ArticuloSupermarketDefault): Long
    suspend fun deleteDefault(articuloId: Long, supermarketId: Long)
}
