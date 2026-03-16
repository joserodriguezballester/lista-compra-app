package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.converters.toEntity
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.mapIndexed

@Singleton
class AisleRepositoryImpl @Inject constructor(
    private val aisleDao: AisleDao
) : IAisleRepository {

    override suspend fun getAllAisles(): List<Aisle> {
        return aisleDao.getAllAisles().map { it.toDomain() }
    }

    override suspend fun addAisle(aisle: Aisle): Long {
        return aisleDao.insertAisle(aisle.toEntity())
    }

    override suspend fun updateAisle(aisle: Aisle) {
        aisleDao.updateAisle(aisle.toEntity())
    }

    override suspend fun updateAisles(aisles: List<Aisle>) {
        val entities = aisles.map { it.toEntity() }
        aisleDao.updateAisles(entities)
    }

    override suspend fun deleteAisle(aisle: Aisle) {
        aisleDao.deleteAisle(aisle.toEntity())
    }

//    override suspend fun reorderAisles(reorderedAisles: List<Aisle>) {
//        val updatedAisles = reorderedAisles.mapIndexed { index, aisle ->
//            aisle.copy(orderIndex = index).toEntity()
//        }
//        aisleDao.updateAisles(updatedAisles)
//    }
}