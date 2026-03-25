package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import javax.inject.Inject

class SearchArticuloUseCase@Inject constructor(
    private val repository: IArticuloRepository
) {
    suspend operator fun invoke(query: String): List<Articulo> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) return emptyList()

        return repository.searchArticulos(cleanQuery)
    }
}