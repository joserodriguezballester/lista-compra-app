package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import javax.inject.Inject

/**
 * Busca artículos por nombre parcial para sugerencias
 */
class SearchArticulosUseCase @Inject constructor(
    private val articuloRepository: IArticuloRepository
) {
    suspend operator fun invoke(query: String): List<Articulo> {
        if (query.isBlank()) return emptyList()
        return articuloRepository.searchArticulos(query)
    }
}
