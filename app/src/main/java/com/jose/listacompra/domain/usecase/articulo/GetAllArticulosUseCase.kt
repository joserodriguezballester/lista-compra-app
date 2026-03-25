package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllArticulosUseCase@Inject constructor(
    private val repository: IArticuloRepository
) {
    operator fun invoke(): Flow<List<Articulo>> {
        return repository.getAllArticulos()
    }
}