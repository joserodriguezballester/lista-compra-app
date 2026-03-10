package com.jose.listacompra.domain.usecase

import com.jose.listacompra.data.repository.ShoppingListRepository
import javax.inject.Inject


class UpdateProductUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(
        productId: Long,
        name: String,
        aisleId: Long,
        quantity: Float,
        price: Float?,
        offerId: Long?,
        photoUri: String?, // ← Nuevo parámetro
        ean: String?        // ← Nuevo parámetro
    ) {
        // 1. Obtener producto actual
        val current = repository.getProductById(productId) ?: return

        // 2. Crear copia con nuevos valores
        val updated = current.copy(
            name = name,
            aisleId = aisleId,
            quantity = quantity,
            estimatedPrice = price,
            offerId = offerId,
            photoUri = photoUri ?: current.photoUri, // Solo actualizar si se pasa
            ean = ean ?: current.ean
        )

        // 3. Guardar
        repository.updateProduct(updated)
    }
}