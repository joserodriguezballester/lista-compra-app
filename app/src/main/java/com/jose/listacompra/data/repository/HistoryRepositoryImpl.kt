package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.Purchase
import com.jose.listacompra.domain.model.SpendingStats
import com.jose.listacompra.domain.repository.IHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: ProductHistoryDao,
    private val frequencyDao: ProductFrequencyDao,
    private val priceHistoryDao: ProductPriceHistoryDao,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val aisleDao: AisleDao
) : IHistoryRepository {

    override suspend fun savePurchaseTransaction(purchase: Purchase): Long {
        // Guardar la compra principal
        val purchaseEntity = PurchaseHistoryEntity(
            supermarket = purchase.supermarket,
            fecha = purchase.fecha.time,
            total = purchase.total
        )
        val purchaseId = purchaseHistoryDao.insertPurchaseHistory(purchaseEntity)

        // Guardar cada producto con su precio
        purchase.products.forEach { product ->
            if (product.finalPrice != null) {
                val priceHistory = ProductPriceHistoryEntity(
                    purchaseId = purchaseId,
                    productName = product.name.lowercase(),
                    price = product.finalPrice!!,
                    quantity = product.quantity.toInt(),
                    aisle = null,
                    fecha = purchase.fecha.time
                )
                priceHistoryDao.insertPriceHistory(priceHistory)
            }
        }

        return purchaseId
    }

    override suspend fun getFrequency(productName: String): ProductFrequencyEntity? {
        return frequencyDao.getFrequency(productName)
    }

    override suspend fun updateFrequency(entity: ProductFrequencyEntity) {
        frequencyDao.insertFrequency(entity)
    }

    override suspend fun insertFrequency(entity: ProductFrequencyEntity) {
        frequencyDao.insertFrequency(entity)
    }

    override suspend fun getPriceHistory(productName: String): List<ProductPriceHistoryEntity> {
        return priceHistoryDao.getPriceHistory(productName)
    }

    override suspend fun getPriceStats(productName: String): PriceStats? {
        return priceHistoryDao.getPriceStats(productName)
    }

    override suspend fun savePriceHistory(priceHistory: ProductPriceHistoryEntity) {
        priceHistoryDao.insertPriceHistory(priceHistory)
    }

    override suspend fun getProductSuggestions(query: String): List<ProductFrequencyEntity> {
        return frequencyDao.findSuggestions(query.lowercase())
    }

    override suspend fun getSpendingStats(): SpendingStats {
        // TODO: Implementar estadísticas de gasto
        return SpendingStats(
            totalSpent = 0f,
            averagePerTrip = 0f,
            mostFrequentProducts = emptyList(),
            monthlySpending = emptyMap()
        )
    }
}