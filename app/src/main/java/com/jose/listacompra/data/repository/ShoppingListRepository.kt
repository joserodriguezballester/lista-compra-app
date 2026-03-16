package com.jose.listacompra.data.repository

import com.google.gson.Gson
import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.converters.toEntity
import com.jose.listacompra.data.local.converters.toSuggestion
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductHistoryEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.model.TotalsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    private val productFrequencyDao: ProductFrequencyDao,

) {
    private val gson = Gson()


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
        val priceRecords = products.map { (name, price, aisle) ->
            ProductPriceHistoryEntity(
                purchaseId = purchaseId,
                productName = name.uppercase().trim(),
                price = price,
                aisle = aisle,
                fecha = System.currentTimeMillis()
            )
        }
        productPriceHistoryDao.insertAllPriceRecords(priceRecords)
        
        // 3. Actualizar frecuencias
        products.forEach { (name, _, _) ->
            updateProductFrequency(name.uppercase().trim())
        }
        
        return purchaseId
    }
    
    /**
     * Actualiza o crea la frecuencia de un producto
     */
    private suspend fun updateProductFrequency(productName: String) {
        val existing = productFrequencyDao.getFrequencyForProduct(productName)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            // Calcular días desde última compra
            val daysSinceLast = (now - existing.lastPurchaseDate) / (1000 * 60 * 60 * 24)
            val newCount = existing.timesPurchased + 1
            
            // Calcular promedio de días entre compras
            val newAverage = if (existing.averageDaysBetween != null) {
                ((existing.averageDaysBetween * (newCount - 1)) + daysSinceLast) / newCount
            } else {
                daysSinceLast.toFloat()
            }
            
            // Estimar próxima compra
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
            // Producto nuevo
            productFrequencyDao.insertOrUpdateFrequency(
                ProductFrequencyEntity(
                    productName = productName,
                    timesPurchased = 1,
                    lastPurchaseDate = now
                )
            )
        }
    }
    
    /**
     * Obtiene productos que probablemente necesites comprar
     */
    suspend fun getSuggestedProductsByFrequency(): List<ProductFrequencyEntity> {
        val now = System.currentTimeMillis()
        return productFrequencyDao.getProductsDueForPurchase(now)
            .sortedByDescending { it.timesPurchased }
    }
    
    /**
     * Obtiene el precio promedio de un producto
     */
    suspend fun getAveragePriceForProduct(name: String): Float? {
        return productPriceHistoryDao.getAveragePriceForProduct(name.uppercase().trim())
    }
    
    /**
     * Obtiene historial de precios de un producto
     */
    suspend fun getPriceHistoryForProduct(name: String): List<ProductPriceHistoryEntity> {
        return productPriceHistoryDao.getPriceHistoryForProduct(name.uppercase().trim())
    }
    
    /**
     * Obtiene los productos más comprados
     */
    suspend fun getMostFrequentProducts(): List<ProductFrequencyEntity> {
        return productFrequencyDao.getMostFrequentProducts()
    }
    
    /**
     * Obtiene todas las compras
     */
    suspend fun getAllPurchases(): List<PurchaseHistoryEntity> {
        return purchaseHistoryDao.getAllPurchases()
    }
    
    /**
     * Obtiene estadísticas de gasto
     */
    suspend fun getSpendingStats(): Triple<Float?, Float, Int> {
        val average = purchaseHistoryDao.getAveragePurchaseAmount() ?: 0f
        val totalSpent = purchaseHistoryDao.getTotalSpentSince(0) ?: 0f
        val totalPurchases = purchaseHistoryDao.getAllPurchases().size
        return Triple(average, totalSpent, totalPurchases)
    }


    /**
     * Busca el mejor pasillo para un producto según historial
     * Retorna aisleId o null si no encuentra
     */
    private suspend fun findBestAisleIdForProduct(
        productName: String,
        supermarketId: Long?
    ): Long? {
        if (supermarketId == null) return null

        // Buscar en historial de productos para este supermercado
        val normalized = productName.trim().uppercase()
        val history = productFrequencyDao.getFrequencyForProduct(normalized)

        // Si existe historial, usar el último pasillo conocido
        // Nota: Necesitarás modificar ProductFrequencyEntity para guardar aisleId
        return history?.let {
            // Intentar extraer aisleId del historial
            // Por ahora: retorna el primer pasillo disponible
            aisleDao.getAllAisles().firstOrNull()?.id
        } ?: aisleDao.getAllAisles().firstOrNull()?.id
    }



}
