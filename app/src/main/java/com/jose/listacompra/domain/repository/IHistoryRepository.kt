package com.jose.listacompra.domain.repository

import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.domain.model.Purchase
import com.jose.listacompra.domain.model.SpendingStats

interface IHistoryRepository {
    // Guarda el ticket y los precios, devuelve el ID de la compra
    suspend fun savePurchaseTransaction(purchase: Purchase): Long

    // Gestión de frecuencias (usamos la Entity directamente para no crear más clases)
    suspend fun getFrequency(productName: String): ProductFrequencyEntity?
    suspend fun updateFrequency(entity: ProductFrequencyEntity)

    // Consultas
    suspend fun getProductSuggestions(query: String): List<ProductSuggestion>
    suspend fun getSpendingStats(): SpendingStats
}