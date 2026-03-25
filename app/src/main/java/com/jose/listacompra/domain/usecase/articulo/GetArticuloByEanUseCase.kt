package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import javax.inject.Inject

class GetArticuloByEanUseCase@Inject constructor(
    private val repository: IArticuloRepository
) {
    suspend operator fun invoke(ean: String): Articulo? {
        if (ean.isBlank()) return null
        return repository.getArticuloByEan(ean)
    }
}