package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import javax.inject.Inject

class ToggleProductPurchasedUseCase @Inject constructor(
    private val productRepository: IProductRepository
) {
    suspend operator fun invoke(product: Product) {
        productRepository.togglePurchased(product.id, !product.isPurchased)
    }
}
