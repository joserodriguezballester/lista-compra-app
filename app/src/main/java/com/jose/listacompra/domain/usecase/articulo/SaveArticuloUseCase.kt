package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.storage.ArticuloPhotoStorage
import javax.inject.Inject

class SaveArticuloUseCase @Inject constructor(
    private val repository: IArticuloRepository,
    private val articuloPhotoStorage: ArticuloPhotoStorage
) {
    suspend operator fun invoke(articulo: Articulo): Result<Unit> {
        if (articulo.name.isBlank()) {
            return Result.failure(Exception("El nombre del artículo no puede estar vacío"))
        }
        if (articulo.size <= 0) {
            return Result.failure(Exception("El tamaño debe ser mayor que cero"))
        }

        return try {
            val normalizedPhotoUri = articulo.photoUri
                ?.takeIf { it.isNotBlank() }
                ?.let { articuloPhotoStorage.centralizeIfNeeded(it) }

            repository.saveArticulo(articulo.copy(photoUri = normalizedPhotoUri))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
