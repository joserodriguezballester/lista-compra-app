package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IOfferRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.usecase.offers.CalculatePriceUseCase
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repository: IProductRepository,
    private val offerRepository: IOfferRepository,
    private val calculatePriceUseCase: CalculatePriceUseCase
) {
    suspend operator fun invoke(product: Product): Long {
        // 1. Lógica de negocio: Obtener el orden siguiente
        val nextOrder = repository.getNextOrderIndex(product.shoppingListId)
        // 1. Buscamos el código de la oferta (si existe) para el cálculo
        val offerCode = product.offerId?.let { id ->
            offerRepository.getOfferById(id)?.code
        }
        // 2. Lógica de negocio: Calcular el precio real basado en ofertas
        val preview = calculatePriceUseCase(
            quantity = product.quantity,
            unitPrice = product.estimatedPrice ?: 0f,
            offerCode = offerCode,
        )

        // 3. Crear la instancia final lista para guardar
        val finalProduct = product.copy(
            orderIndex = nextOrder,
            finalPrice = preview.finalPrice,
            name = product.name.trim()
        )

        return repository.insertProduct(finalProduct)
    }
}
