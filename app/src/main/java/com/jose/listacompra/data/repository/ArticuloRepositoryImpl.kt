package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.converters.toEntity
import com.jose.listacompra.data.local.dao.ArticuloDao
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ArticuloRepositoryImpl @Inject constructor(
    private val articuloDao: ArticuloDao
) : IArticuloRepository {

    override fun getAllArticulos(): Flow<List<Articulo>> {
        // Transformamos el Flow de Entity a Flow de Articulo (Domain)
        return articuloDao.getAllArticulos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getArticuloByEan(ean: String): Articulo? {
        return articuloDao.getArticuloByEan(ean)?.toDomain()
    }

    override suspend fun getArticuloById(id: Long): Articulo? {
        return articuloDao.getArticuloById(id)?.toDomain()
    }

    override suspend fun searchArticulos(query: String): List<Articulo> {
        // searchArticulosByName en el DAO debería devolver List<ArticuloEntity>
        return articuloDao.searchArticulosByName(query).map { it.toDomain() }
    }

    override suspend fun saveArticulo(articulo: Articulo) {
        articuloDao.insertArticulo(articulo.toEntity())
    }

    override suspend fun deleteArticulo(articulo: Articulo) {
        articuloDao.deleteArticulo(articulo.toEntity())
    }

    override suspend fun updateArticulo(articulo: Articulo) {
        articuloDao.insertArticulo(articulo.toEntity()) // da igual crear que modificar
    }
}