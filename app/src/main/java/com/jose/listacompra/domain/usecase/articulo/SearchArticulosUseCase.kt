package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Buscar artículos por nombre
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 */
@Singleton
class SearchArticulosUseCase @Inject constructor(
    private val articuloRepository: IArticuloRepository
) {
    /**
     * Busca artículos que coincidan con el query
     */
    suspend operator fun invoke(query: String): List<Articulo> {
        return articuloRepository.searchArticulos(query)
    }
}