package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import javax.inject.Inject

class ToggleProductPurchasedUseCase @Inject constructor(
    private val repository: IProductRepository
) {
    suspend operator fun invoke(product: Product) {
        // Invertimos el estado del booleano
        val updatedProduct = product.copy(isPurchased = !product.isPurchased)

        // Lo mandamos al repositorio (que ya usa la interfaz)
        repository.updateProduct(updatedProduct)
    }
}