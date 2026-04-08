package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener productos de una lista
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 * 
 * T4: Soporte para supermercado por producto
 */
@Singleton
class GetProductsByListUseCase @Inject constructor(
    private val productRepository: IProductRepository
) {
    /**
     * Obtiene productos de una lista
     * 
     * @param listId ID de la lista
     * @param supermarketId ID del supermercado (null = todos, >0 = X + "Cualquiera")
     */
    operator fun invoke(listId: Long, supermarketId: Long? = null): Flow<List<Product>> {
        return when {
            supermarketId == null -> productRepository.getProductsByListFlow(listId)
            supermarketId > 0 -> productRepository.getProductsBySupermarketOrAnyFlow(listId, supermarketId)
            else -> productRepository.getProductsByListFlow(listId)
        }
    }
}