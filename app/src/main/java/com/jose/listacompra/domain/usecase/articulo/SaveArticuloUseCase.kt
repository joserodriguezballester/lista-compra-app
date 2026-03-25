package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import javax.inject.Inject

class SaveArticuloUseCase@Inject constructor(
    private val repository: IArticuloRepository
){
    suspend operator fun invoke(articulo: Articulo): Result<Unit> {
        // Reglas de negocio
        if (articulo.name.isBlank()) {
            return Result.failure(Exception("El nombre del artículo no puede estar vacío"))
        }
        if (articulo.size <= 0) {
            return Result.failure(Exception("El tamaño debe ser mayor que cero"))
        }

        repository.saveArticulo(articulo)
        return Result.success(Unit)
    }
}