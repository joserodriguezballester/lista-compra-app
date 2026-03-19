package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import kotlinx.coroutines.flow.Flow

class GetAllArticulosUseCase(private val repository: IArticuloRepository) {
    operator fun invoke(): Flow<List<Articulo>> {
        return repository.getAllArticulos()
    }
}