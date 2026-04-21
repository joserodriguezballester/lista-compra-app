package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IHistoryRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import com.jose.listacompra.domain.usecase.history.CompletePurchaseUseCase
import com.jose.listacompra.domain.usecase.history.ProductPurchaseData
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import javax.inject.Inject

class ToggleProductPurchasedUseCase @Inject constructor(
    private val productRepository: IProductRepository,
    private val historyRepository: IHistoryRepository,
    private val supermarketRepository: ISupermarketRepository,
    private val savePriceHistoryUseCase: SavePriceHistoryUseCase,
    private val completePurchaseUseCase: CompletePurchaseUseCase
) {
    suspend operator fun invoke(product: Product) {
        val markingAsPurchased = !product.isPurchased

        if (markingAsPurchased) {
            val purchaseMoment = System.currentTimeMillis()
            registerCompletedPurchase(product, purchaseMoment)
            registerManualPurchasePriceHistoryIfNeeded(product, purchaseMoment)
        }

        productRepository.togglePurchased(product.id, markingAsPurchased)
    }

    private suspend fun registerCompletedPurchase(product: Product, purchaseMoment: Long) {
        val unitPrice = when {
            product.estimatedPrice != null || product.finalPrice != null -> product.effectiveUnitPrice()
            else -> null
        }

        completePurchaseUseCase(
            products = listOf(
                ProductPurchaseData(
                    name = product.name,
                    quantity = product.quantity,
                    price = unitPrice,
                    aisleId = product.aisleId.takeIf { it > 0L },
                    supermarketId = product.supermarketId.takeIf { it > 0L }
                )
            ),
            purchaseDate = purchaseMoment
        )
    }

    private suspend fun registerManualPurchasePriceHistoryIfNeeded(product: Product, purchaseMoment: Long) {
        val hasKnownPrice = product.estimatedPrice != null || product.finalPrice != null
        if (!hasKnownPrice) return

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
