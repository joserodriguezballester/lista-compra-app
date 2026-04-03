package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.converters.toSuggestion
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductHistoryEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.ProductSuggestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val productDao: ProductDao,
    private val shoppingListDao: ShoppingListDao,
    private val aisleDao: AisleDao,
    private val offerDao: OfferDao,
    private val historyDao: ProductHistoryDao,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val productPriceHistoryDao: ProductPriceHistoryDao,
    private val productFrequencyDao: ProductFrequencyDao
) {

    suspend fun findProductSuggestions(query: String): List<ProductSuggestion> {
        if (query.length < 2) return emptyList()
        return historyDao.findSuggestions(query.lowercase()).map { it.toSuggestion() }
    }

    suspend fun saveToHistory(name: String, aisleId: Long, quantity: Float, price: Float?) {
        val normalizedName = name.lowercase().trim()
        val existing = historyDao.findByName(normalizedName)

        if (existing != null) {
            historyDao.updateUsage(normalizedName, quantity, price)
        } else {
            historyDao.insert(
                ProductHistoryEntity(
                    name = normalizedName,
                    originalName = name.trim(),
                    aisleId = aisleId,
                    lastQuantity = quantity,
                    lastPrice = price
                )
            )
        }
    }

    suspend fun getFrequentProducts(): List<ProductSuggestion> {
        return historyDao.getMostFrequent().map { it.toSuggestion() }
    }

    suspend fun isHistoryEmpty(): Boolean {
        return historyDao.getMostFrequent().isEmpty()
    }

    // ========== HISTORIAL DE COMPRAS (TICKETS) ==========
    
    /**
     * Guarda una compra completa con todos sus productos
     */
    suspend fun savePurchase(
        total: Float,
        numProductos: Int,
        tienda: String = "Carrefour",
        ahorro: Float = 0f,
        products: List<Triple<String, Float, String?>> // nombre, precio, pasillo
    ): Long {
        // 1. Guardar la compra
        val purchase = PurchaseHistoryEntity(
            fecha = System.currentTimeMillis(),
            total = total,
            tienda = tienda,
            numProductos = numProductos,
            ahorroTotal = ahorro
        )
        val purchaseId = purchaseHistoryDao.insertPurchase(purchase)
        
        // 2. Guardar cada producto con su precio
        products.forEach { (name, price, aisle) ->
            val priceRecord = ProductPriceHistoryEntity(
                purchaseId = purchaseId,
                productName = name.lowercase().trim(),
                price = price,
                aisle = aisle,
                fecha = System.currentTimeMillis()
            )
            productPriceHistoryDao.insertPriceHistory(priceRecord)
        }
        
        // 3. Actualizar frecuencias
        products.forEach { (name, _, _) ->
            updateProductFrequency(name.lowercase().trim())
        }
        
        return purchaseId
    }
    
    private suspend fun updateProductFrequency(productName: String) {
        val existing = productFrequencyDao.getFrequencyForProduct(productName)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            val daysSinceLast = (now - existing.lastPurchaseDate) / (1000 * 60 * 60 * 24)
            val newCount = existing.timesPurchased + 1
            
            val newAverage = if (existing.averageDaysBetween != null) {
                ((existing.averageDaysBetween * (newCount - 1)) + daysSinceLast) / newCount
            } else {
                daysSinceLast.toFloat()
            }
            
            val nextPurchase = now + (newAverage * 24 * 60 * 60 * 1000).toLong()
            
            productFrequencyDao.insertOrUpdateFrequency(
                existing.copy(
                    timesPurchased = newCount,
                    averageDaysBetween = newAverage,
                    lastPurchaseDate = now,
                    estimatedNextDate = nextPurchase
                )
            )
        } else {
            productFrequencyDao.insertOrUpdateFrequency(
                ProductFrequencyEntity(
                    productName = productName,
                    originalName = productName,
                    timesPurchased = 1,
                    lastPurchaseDate = now
                )
            )
        }
    }
    
    suspend fun getSuggestedProductsByFrequency(): List<ProductFrequencyEntity> {
        val now = System.currentTimeMillis()
        return productFrequencyDao.getProductsDueForPurchase(now)
            .sortedByDescending { it.timesPurchased }
    }
    
    suspend fun getMostFrequentProducts(): List<ProductFrequencyEntity> {
        return productFrequencyDao.getMostFrequentProducts()
    }
    
    suspend fun getAllPurchases(): List<PurchaseHistoryEntity> {
        return purchaseHistoryDao.getAllPurchases()
    }
    
    suspend fun getSpendingStats(): Triple<Float?, Float, Int> {
        val average = purchaseHistoryDao.getAveragePurchaseAmount() ?: 0f
        val totalSpent = purchaseHistoryDao.getTotalSpentSince(0) ?: 0f
        val totalPurchases = purchaseHistoryDao.getAllPurchases().size
        return Triple(average, totalSpent, totalPurchases)
    }
}