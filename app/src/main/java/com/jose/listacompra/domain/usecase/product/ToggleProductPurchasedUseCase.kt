package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IHistoryRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import javax.inject.Inject

class ToggleProductPurchasedUseCase @Inject constructor(
    private val productRepository: IProductRepository,
    private val historyRepository: IHistoryRepository,
    private val supermarketRepository: ISupermarketRepository,
    private val savePriceHistoryUseCase: SavePriceHistoryUseCase
) {
    suspend operator fun invoke(product: Product) {
        val markingAsPurchased = !product.isPurchased

        if (markingAsPurchased) {
            registerManualPurchaseIfNeeded(product)
        }

        productRepository.togglePurchased(product.id, markingAsPurchased)
    }

    private suspend fun registerManualPurchaseIfNeeded(product: Product) {
        val hasKnownPrice = product.estimatedPrice != null || product.finalPrice != null
        if (!hasKnownPrice) return

        val purchaseMoment = System.currentTimeMillis()
        val quantityForHistory = product.quantity.toInt().coerceAtLeast(1)
        val totalPaid = product.finalPriceToPay()
        val supermarketName = when {
            product.supermarketId > 0L -> supermarketRepository.getSupermarketById(product.supermarketId)?.name
            else -> null
        } ?: "Compra manual"

        val purchaseHistoryId = historyRepository.insertPurchaseHistory(
            PurchaseHistoryEntity(
                fecha = purchaseMoment,
                total = totalPaid,
                tienda = supermarketName,
                numProductos = quantityForHistory,
                ahorroTotal = product.savings().coerceAtLeast(0f)
            )
        )

        savePriceHistoryUseCase(
            productName = product.name,
            price = product.effectiveUnitPrice(),
            quantity = quantityForHistory,
            purchaseId = purchaseHistoryId,
            purchaseDate = purchaseMoment
        )
    }
}
