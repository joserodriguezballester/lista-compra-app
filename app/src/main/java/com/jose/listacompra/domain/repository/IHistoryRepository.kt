package com.jose.listacompra.domain.repository

import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.domain.model.Purchase
import com.jose.listacompra.domain.model.SpendingStats

interface IHistoryRepository {
    // Guarda el ticket y los precios, devuelve el ID de la compra
    suspend fun savePurchaseTransaction(purchase: Purchase): Long

    // Gestión de frecuencias
    suspend fun getFrequency(productName: String): ProductFrequencyEntity?
    suspend fun updateFrequency(entity: ProductFrequencyEntity)
    suspend fun insertFrequency(entity: ProductFrequencyEntity)

    // Historial de precios
    suspend fun getPriceHistory(productName: String): List<ProductPriceHistoryEntity>
    suspend fun getPriceStats(productName: String): PriceStats?
    suspend fun savePriceHistory(priceHistory: ProductPriceHistoryEntity)

    // Consultas
    suspend fun getProductSuggestions(query: String): List<ProductFrequencyEntity>
    suspend fun getSpendingStats(): SpendingStats
}