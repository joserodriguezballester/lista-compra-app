package com.jose.listacompra.domain.usecase.history

import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.domain.model.Purchase
import com.jose.listacompra.domain.repository.IHistoryRepository
import javax.inject.Inject

class CompletePurchaseUseCase @Inject constructor(
    private val repository: IHistoryRepository
) {
    suspend operator fun invoke(purchase: Purchase) {
        // 1. Guardar la compra completa (Ticket + Precios)
        val purchaseId = repository.savePurchaseTransaction(purchase)

        // 2. Actualizar la inteligencia de cada producto
        purchase.items.forEach { product ->
            val name = product.name.uppercase().trim()
            val existing = repository.getFrequency(name)
            val now = System.currentTimeMillis()

            val updatedEntity = if (existing != null) {
                // Tu lógica matemática original de promedios
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
                    estimatedNextDate = nextDate
                )
            } else {
                ProductFrequencyEntity(
                    productName = name,
                    timesPurchased = 1,
                    lastPurchaseDate = now
                )
            }

            repository.updateFrequency(updatedEntity)
        }
    }
}