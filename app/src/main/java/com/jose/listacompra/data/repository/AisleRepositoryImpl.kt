package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AisleRepositoryImpl @Inject constructor(
    private val aisleDao: AisleDao
) : IAisleRepository {

    override suspend fun getAllAisles(): List<Aisle> {
        return aisleDao.getAllAisles().map { it.toDomain() }
    }
    
    override suspend fun getAislesBySupermarket(supermarketId: Long): List<Aisle> {
        return aisleDao.getAislesBySupermarket(supermarketId).map { it.toDomain() }
    }
    
    override fun getAislesBySupermarketFlow(supermarketId: Long) = 
        aisleDao.getAislesBySupermarketFlow(supermarketId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addAisle(aisle: Aisle): Long {
        return aisleDao.insertAisle(AisleEntity.fromDomain(aisle))
    }

    override suspend fun updateAisle(aisle: Aisle) {
        aisleDao.updateAisle(AisleEntity.fromDomain(aisle))
    }

    override suspend fun updateAisles(aisles: List<Aisle>) {
        val entities = aisles.map { AisleEntity.fromDomain(it) }
        aisleDao.updateAisles(entities)
    }

    override suspend fun deleteAisle(aisle: Aisle) {
        aisleDao.deleteAisle(AisleEntity.fromDomain(aisle))
    }
}
