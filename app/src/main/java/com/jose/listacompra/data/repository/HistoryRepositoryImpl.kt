package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.domain.model.Purchase
import com.jose.listacompra.domain.model.SpendingStats
import com.jose.listacompra.domain.repository.IHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val purchaseDao: PurchaseHistoryDao,
    private val priceHistoryDao: ProductPriceHistoryDao,
    private val frequencyDao: ProductFrequencyDao,
    private val historyDao: ProductHistoryDao, // El de las sugerencias simples
    private val aisleDao: AisleDao
) : IHistoryRepository {

    override suspend fun savePurchaseTransaction(purchase: Purchase): Long {
        // 1. Insertamos el ticket principal
        val purchaseId = purchaseDao.insertPurchase(
            PurchaseHistoryEntity(
                fecha = System.currentTimeMillis(),
                total = purchase.total,
                tienda = purchase.storeName,
                numProductos = purchase.items.size,
                ahorroTotal = purchase.savings
            )
        )

        // 2. Insertamos el desglose de precios (mapeando de Product a la Entity)
        val priceRecords = purchase.items.map { product ->
            ProductPriceHistoryEntity(
                purchaseId = purchaseId,
                productName = product.name.uppercase().trim(),
                price = product.finalPrice ?: 0f,
                aisle = null, // Podrías sacar el nombre del pasillo si lo tienes
                fecha = System.currentTimeMillis()
            )
        }
        priceHistoryDao.insertAllPriceRecords(priceRecords)

        return purchaseId
    }

    override suspend fun getFrequency(productName: String): ProductFrequencyEntity? {
        return frequencyDao.getFrequencyForProduct(productName)
    }

    override suspend fun updateFrequency(entity: ProductFrequencyEntity) {
        frequencyDao.insertOrUpdateFrequency(entity)
    }

    override suspend fun getSpendingStats(): SpendingStats {
        val average = purchaseDao.getAveragePurchaseAmount() ?: 0f
        val total = purchaseDao.getTotalSpentSince(0) ?: 0f
        val count = purchaseDao.getAllPurchases().size

        return SpendingStats(
            averagePerPurchase = average,
            totalSpent = total,
            totalPurchasesCount = count
        )
    }

    override suspend fun getProductSuggestions(query: String): List<ProductSuggestion> {
        return historyDao.findSuggestions(query.lowercase()).map { entity ->
            ProductSuggestion(
                name = entity.originalName,
                aisleId = entity.aisleId,
                suggestedQuantity = entity.lastQuantity,
                suggestedPrice = entity.lastPrice,
                usageCount = entity.usageCount
            )
        }
    }
}