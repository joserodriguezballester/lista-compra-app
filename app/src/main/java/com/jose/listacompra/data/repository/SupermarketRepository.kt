package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.SupermarketDao
import com.jose.listacompra.data.local.dataseeder.defaultSupermarkets
import com.jose.listacompra.data.local.entities.SupermarketEntity
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.ISupermarketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SupermarketRepository @Inject constructor(
    private val dao: SupermarketDao
) : ISupermarketRepository {
    
    override fun getAllSupermarkets(): Flow<List<Supermarket>> {
        return dao.getAllSupermarkets().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getSupermarketById(id: Long): Supermarket? {
        return dao.getSupermarketById(id)?.toDomain()
    }
    
    override suspend fun getDefaultSupermarket(): Supermarket? {
        return dao.getDefaultSupermarket()?.toDomain()
    }
    
    override suspend fun insertSupermarket(supermarket: Supermarket): Long {
        return dao.insertSupermarket(SupermarketEntity.fromDomain(supermarket))
    }
    
    override suspend fun insertAll(supermarkets: List<Supermarket>) {
        dao.insertAll(supermarkets.map { SupermarketEntity.fromDomain(it) })
    }

    override suspend fun ensureBuiltinSupermarkets() {
        defaultSupermarkets.forEach { supermarket ->
            dao.insertBuiltinSupermarket(
                id = supermarket.id,
                name = supermarket.name,
                emoji = supermarket.emoji,
                isDefault = supermarket.isDefault
            )
        }
    }
    
    override suspend fun deleteSupermarket(id: Long) {
        dao.deleteSupermarket(id)
    }
}
