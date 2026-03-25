package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import javax.inject.Inject

class UpdateArticuloUseCase@Inject constructor(
    private val repository: IArticuloRepository
) {
    suspend operator fun invoke(articulo: Articulo): Result<Unit> {
        // 1. Validaciones comunes
        if (articulo.name.isBlank()) {
            return Result.failure(Exception("El nombre es obligatorio"))
        }
        if (articulo.size <= 0) {
            return Result.failure(Exception("El tamaño debe ser mayor que cero"))
        }

        // 2. Validación específica de Update (El ID debe ser válido)
        if (articulo.id <= 0) {
            return Result.failure(Exception("No se puede actualizar un artículo sin ID válido"))
        }

        return try {
            repository.updateArticulo(articulo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}