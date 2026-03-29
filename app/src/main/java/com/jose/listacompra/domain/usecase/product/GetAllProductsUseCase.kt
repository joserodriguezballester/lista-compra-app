package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val productRepository: IProductRepository
) {

    operator fun invoke(listId: Long): Flow<List<Product>> {
        return productRepository.getProductsByListFlow(listId)
    }
}
