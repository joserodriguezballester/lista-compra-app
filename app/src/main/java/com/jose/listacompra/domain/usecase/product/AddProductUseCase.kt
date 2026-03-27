package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: IProductRepository
) {
    suspend operator fun invoke(product: Product): Long {
        return productRepository.insertProduct(product)
    }
}
