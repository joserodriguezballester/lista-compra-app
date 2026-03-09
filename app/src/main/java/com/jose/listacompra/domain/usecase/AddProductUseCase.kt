package com.jose.listacompra.domain.usecase

import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Product
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {

    /**
     * Añade producto al pasillo 1 (genérico) temporalmente
     * Cuando tengas categorías implementadas, mejora esto
     */
    suspend operator fun invoke(
        name: String,
        quantity: Float = 1f,
        listId: Long,
        estimatedPrice: Float? = null,
        offerId: Long? = null
    ): Long {

        // Obtener siguiente posición
        val orderIndex = repository.getNextOrderIndex(listId)

        // Crear producto SIN usar categoryId ni aisleMap
        val product = Product(
            id = 0, // Auto-generado
            name = name.trim(),
            aisleId = 1L, // ← PASILLO GENÉRICO (temporal)
            shoppingListId = listId,
            quantity = quantity,
            estimatedPrice = estimatedPrice,
            offerId = offerId,
            finalPrice = estimatedPrice?.times(quantity),
            isPurchased = false,
            notes = "",
            orderIndex = orderIndex
        )

        // Guardar producto
        return repository.addProduct(product)
    }
}