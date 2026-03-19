package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository

class DeleteArticuloUseCase(private val repository: IArticuloRepository) {
    suspend operator fun invoke(articulo: Articulo) {
        repository.deleteArticulo(articulo)
    }
}