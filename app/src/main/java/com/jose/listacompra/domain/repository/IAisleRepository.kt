package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Aisle

interface IAisleRepository {
    suspend fun getAllAisles(): List<Aisle>
    suspend fun addAisle(aisle: Aisle): Long
    suspend fun updateAisle(aisle: Aisle)
    suspend fun updateAisles(aisles: List<Aisle>)
    suspend fun deleteAisle(aisle: Aisle)

}