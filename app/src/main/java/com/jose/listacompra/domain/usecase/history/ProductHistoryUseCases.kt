package com.jose.listacompra.domain.usecase.history

import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.domain.repository.IHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener sugerencias de productos del historial
 */
@Singleton
class GetProductHistorySuggestionsUseCase @Inject constructor(
    private val historyRepository: IHistoryRepository
) {
    suspend operator fun invoke(query: String): List<ProductFrequencyEntity> {
        return historyRepository.getProductSuggestions(query)
    }
}

/**
 * Caso de uso: Obtener historial de precios de un producto
 */
@Singleton
class GetPriceHistoryUseCase @Inject constructor(
    private val historyRepository: IHistoryRepository
) {
    suspend operator fun invoke(productName: String): List<ProductPriceHistoryEntity> {
        return historyRepository.getPriceHistory(productName.lowercase())
    }
}

/**
 * Caso de uso: Obtener estadísticas de precio de un producto
 */
@Singleton
class GetPriceStatsUseCase @Inject constructor(
    private val historyRepository: IHistoryRepository
) {
    suspend operator fun invoke(productName: String): PriceStats? {
        return historyRepository.getPriceStats(productName.lowercase())
    }
}

/**
 * Caso de uso: Guardar frecuencia de producto (pasillo, cantidad)
 */
@Singleton
class UpdateProductFrequencyUseCase @Inject constructor(
    private val historyRepository: IHistoryRepository
) {
    suspend operator fun invoke(
        name: String,
        aisleId: Long?,
        quantity: Float,
        price: Float?,
        supermarketId: Long?
    ) {
        val normalizedName = name.lowercase().trim()
        val existing = historyRepository.getFrequency(normalizedName)
        
        if (existing != null) {
            historyRepository.updateFrequency(
                existing.copy(
                    usageCount = existing.usageCount + 1,
                    lastQuantity = quantity,
                    lastPrice = price ?: existing.lastPrice,
                    aisleId = aisleId ?: existing.aisleId,
                    lastSupermarketId = supermarketId ?: existing.lastSupermarketId
                )
            )
        } else {
            historyRepository.insertFrequency(
                ProductFrequencyEntity(
                    name = normalizedName,
                    originalName = name,
                    aisleId = aisleId ?: 0L,
                    lastQuantity = quantity,
                    lastPrice = price ?: 0f,
                    lastSupermarketId = supermarketId ?: 0L,
                    usageCount = 1
                )
            )
        }
    }
}

/**
 * Caso de uso: Guardar precio histórico
 */
@Singleton
class SavePriceHistoryUseCase @Inject constructor(
    private val historyRepository: IHistoryRepository
) {
    suspend operator fun invoke(
        productName: String,
        price: Float,
        quantity: Int,
        aisle: String? = null
    ) {
        historyRepository.savePriceHistory(
            ProductPriceHistoryEntity(
                productName = productName.lowercase().trim(),
                price = price,
                quantity = quantity,
                aisle = aisle,
                fecha = System.currentTimeMillis()
            )
        )
    }
}