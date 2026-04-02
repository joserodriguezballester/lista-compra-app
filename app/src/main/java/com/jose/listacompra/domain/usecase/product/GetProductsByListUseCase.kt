package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener productos de una lista
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 */
@Singleton
class GetProductsByListUseCase @Inject constructor(
    private val productRepository: IProductRepository
) {
    /**
     * Obtiene productos de una lista, opcionalmente filtrados por supermercado
     */
    operator fun invoke(listId: Long, supermarketId: Long? = null): Flow<List<Product>> {
        return if (supermarketId != null && supermarketId > 0) {
            productRepository.getProductsBySupermarketFlow(listId, supermarketId)
        } else {
            productRepository.getProductsByListFlow(listId)
        }
    }
}