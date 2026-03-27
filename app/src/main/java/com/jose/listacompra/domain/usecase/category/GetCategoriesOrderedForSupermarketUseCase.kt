package com.jose.listacompra.domain.usecase.category

import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.CategorySupermarketOrder
import com.jose.listacompra.domain.repository.ICategoryRepository
import com.jose.listacompra.domain.repository.ICategorySupermarketOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Obtiene las categorías ordenadas para un supermercado específico.
 * Si no hay orden definido, devuelve las categorías en orden alfabético.
 */
class GetCategoriesOrderedForSupermarketUseCase @Inject constructor(
    private val categoryRepository: ICategoryRepository,
    private val orderRepository: ICategorySupermarketOrderRepository
) {
    data class OrderedCategory(
        val category: Category,
        val orderIndex: Int
    )
    
    operator fun invoke(supermarketId: Long): Flow<List<OrderedCategory>> {
        return categoryRepository.getAllCategories().map { categories ->
            val orders = orderRepository.getOrdersBySupermarketOnce(supermarketId)
                .associateBy { it.categoryId }
            
            categories.map { category ->
                val order = orders[category.id]
                OrderedCategory(
                    category = category,
                    orderIndex = order?.orderIndex ?: Int.MAX_VALUE
                )
            }.sortedBy { it.orderIndex }
        }
    }
}
