package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Supermarket
import kotlinx.coroutines.flow.Flow

interface ISupermarketRepository {
    fun getAllSupermarkets(): Flow<List<Supermarket>>
    suspend fun getSupermarketById(id: Long): Supermarket?
    suspend fun getDefaultSupermarket(): Supermarket?
    suspend fun insertSupermarket(supermarket: Supermarket): Long
    suspend fun insertAll(supermarkets: List<Supermarket>)
    suspend fun deleteSupermarket(id: Long)
}
