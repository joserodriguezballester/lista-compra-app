package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.storage.ArticuloPhotoStorage
import javax.inject.Inject

class UpdateArticuloUseCase @Inject constructor(
    private val repository: IArticuloRepository,
    private val articuloPhotoStorage: ArticuloPhotoStorage
) {
    suspend operator fun invoke(articulo: Articulo): Result<Unit> {
        if (articulo.name.isBlank()) {
            return Result.failure(Exception("El nombre es obligatorio"))
        }
        if (articulo.size <= 0) {
            return Result.failure(Exception("El tamaño debe ser mayor que cero"))
        }
        if (articulo.id <= 0) {
            return Result.failure(Exception("No se puede actualizar un artículo sin ID válido"))
        }

        return try {
            val existingArticulo = repository.getArticuloById(articulo.id)
                ?: return Result.failure(Exception("No se encontró el artículo a actualizar"))

            val normalizedPhotoUri = when {
                articulo.photoUri.isNullOrBlank() -> null
                articulo.photoUri == existingArticulo.photoUri -> existingArticulo.photoUri
                else -> articuloPhotoStorage.centralizeIfNeeded(articulo.photoUri, articulo.name)
            }

            repository.updateArticulo(articulo.copy(photoUri = normalizedPhotoUri))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
