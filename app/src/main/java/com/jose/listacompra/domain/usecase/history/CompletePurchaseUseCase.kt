package com.jose.listacompra.domain.usecase.history

import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.domain.repository.IHistoryRepository
import javax.inject.Inject

class CompletePurchaseUseCase @Inject constructor(
    private val repository: IHistoryRepository
) {
    suspend operator fun invoke(products: List<ProductPurchaseData>) {
        products.forEach { productData ->
            val name = productData.name.lowercase().trim()
            val existing = repository.getFrequency(name)
            val now = System.currentTimeMillis()

            val updatedEntity = if (existing != null) {
                val daysSinceLast = (now - existing.lastPurchaseDate) / (1000 * 60 * 60 * 24)
                val newCount = existing.timesPurchased + 1
                val newAverage = if (existing.averageDaysBetween != null) {
                    ((existing.averageDaysBetween * (newCount - 1)) + daysSinceLast) / newCount
                } else daysSinceLast.toFloat()

                val nextDate = now + (newAverage * 24 * 60 * 60 * 1000).toLong()

                existing.copy(
                    timesPurchased = newCount,
                    averageDaysBetween = newAverage,
                    lastPurchaseDate = now,
                    estimatedNextDate = nextDate,
                    lastAisleId = productData.aisleId ?: existing.lastAisleId,
                    lastPrice = productData.price ?: existing.lastPrice,
                    lastQuantity = productData.quantity
                )
            } else {
                ProductFrequencyEntity(
                    productName = name,
                    originalName = productData.name.trim(),
                    timesPurchased = 1,
                    lastPurchaseDate = now,
                    lastAisleId = productData.aisleId ?: 0L,
                    lastPrice = productData.price ?: 0f,
                    lastQuantity = productData.quantity
                )
            }

            repository.updateFrequency(updatedEntity)
        }
    }
}

data class ProductPurchaseData(
    val name: String,
    val quantity: Float = 1f,
    val price: Float? = null,
    val aisleId: Long? = null
)