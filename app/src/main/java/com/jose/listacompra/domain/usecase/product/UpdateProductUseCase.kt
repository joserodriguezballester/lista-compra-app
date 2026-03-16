package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IOfferRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.usecase.offers.CalculatePriceUseCase
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: IProductRepository,
    private val offerRepository: IOfferRepository,
    private val calculatePriceUseCase: CalculatePriceUseCase
) {
    suspend operator fun invoke(product: Product) {
        // 1. Buscamos el código de la oferta si el producto tiene una asignada
        val offerCode = product.offerId?.let { id ->
            offerRepository.getOfferById(id)?.code
        }

        // 1. Lógica de negocio: Recalcular el precio final
        // Esto garantiza que si el usuario cambió el precio o la oferta, el total se actualice
        val newFinalPrice = calculatePriceUseCase(
            quantity = product.quantity,
            unitPrice = product.estimatedPrice ?: 0f,
            offerCode = offerCode
        )

        // 2. Aplicamos el cálculo y normalizamos datos (ej. espacios en blanco)
        val productToUpdate = product.copy(
            finalPrice = newFinalPrice.finalPrice,
            name = product.name.trim()
        )

        // 3. Mandamos al repositorio
        repository.updateProduct(productToUpdate)
    }
}
