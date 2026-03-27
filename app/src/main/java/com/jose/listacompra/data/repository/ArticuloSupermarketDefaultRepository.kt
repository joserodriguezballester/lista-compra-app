package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.ArticuloSupermarketDefaultDao
import com.jose.listacompra.data.local.entities.ArticuloSupermarketDefaultEntity
import com.jose.listacompra.domain.model.ArticuloSupermarketDefault
import com.jose.listacompra.domain.repository.IArticuloSupermarketDefaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ArticuloSupermarketDefaultRepository @Inject constructor(
    private val dao: ArticuloSupermarketDefaultDao
) : IArticuloSupermarketDefaultRepository {
    
    override suspend fun getDefaultAisle(articuloId: Long, supermarketId: Long): ArticuloSupermarketDefault? {
        return dao.getDefaultAisle(articuloId, supermarketId)?.toDomain()
    }
    
    override fun getDefaultsForArticulo(articuloId: Long): Flow<List<ArticuloSupermarketDefault>> {
        return dao.getDefaultsForArticulo(articuloId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertOrUpdate(default: ArticuloSupermarketDefault): Long {
        return dao.insertOrUpdate(ArticuloSupermarketDefaultEntity.fromDomain(default))
    }
    
    override suspend fun deleteDefault(articuloId: Long, supermarketId: Long) {
        dao.deleteDefault(articuloId, supermarketId)
    }
}
