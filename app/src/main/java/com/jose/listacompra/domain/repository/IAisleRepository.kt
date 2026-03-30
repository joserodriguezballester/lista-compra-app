package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Aisle
import kotlinx.coroutines.flow.Flow

interface IAisleRepository {
    suspend fun getAllAisles(): List<Aisle>
    suspend fun getAislesBySupermarket(supermarketId: Long): List<Aisle>
    fun getAislesBySupermarketFlow(supermarketId: Long): Flow<List<Aisle>>
    suspend fun addAisle(aisle: Aisle): Long
    suspend fun insertAll(aisles: List<Aisle>)
    suspend fun updateAisle(aisle: Aisle)
    suspend fun updateAisles(aisles: List<Aisle>)
    suspend fun deleteAisle(aisle: Aisle)
}
