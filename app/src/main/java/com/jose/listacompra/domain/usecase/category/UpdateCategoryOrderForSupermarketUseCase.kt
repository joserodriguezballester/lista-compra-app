package com.jose.listacompra.domain.usecase.category

import com.jose.listacompra.domain.model.CategorySupermarketOrder
import com.jose.listacompra.domain.repository.ICategorySupermarketOrderRepository
import javax.inject.Inject

/**
 * Actualiza el orden de las categorías para un supermercado.
 * Recibe una lista de IDs de categorías en el orden deseado.
 */
class UpdateCategoryOrderForSupermarketUseCase @Inject constructor(
    private val orderRepository: ICategorySupermarketOrderRepository
) {
    suspend operator fun invoke(supermarketId: Long, categoryIdsInOrder: List<Long>) {
        val orders = categoryIdsInOrder.mapIndexed { index, categoryId ->
            CategorySupermarketOrder(
                categoryId = categoryId,
                supermarketId = supermarketId,
                orderIndex = index
            )
        }
        orderRepository.updateOrdersForSupermarket(supermarketId, orders)
    }
}
