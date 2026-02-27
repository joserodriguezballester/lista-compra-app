package com.jose.listacompra.data.repository

import android.content.Context
import androidx.room.Room
import com.jose.listacompra.data.local.*
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.toExport
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShoppingListRepository(context: Context) {
    
    private val db = Room.databaseBuilder(
        context.applicationContext,
        ShoppingListDatabase::class.java,
        ShoppingListDatabase.DATABASE_NAME
    ).fallbackToDestructiveMigration()  // Para actualizar versión de BD
     .build()
    
    private val aisleDao = db.aisleDao()
    private val offerDao = db.offerDao()
    private val productDao = db.productDao()
    private val historyDao = db.productHistoryDao()
    private val gson = Gson()
    
    // ========== OFERTAS ==========
    
    suspend fun getAllOffers(): List<Offer> {
        return offerDao.getAllOffers().map { it.toDomain() }
    }
    
    suspend fun getOfferById(id: Long): Offer? {
        return offerDao.getOfferById(id)?.toDomain()
    }
    
    suspend fun getDefaultOffers(): List<Offer> {
        return offerDao.getDefaultOffers().map { it.toDomain() }
    }
    
    suspend fun addOffer(offer: Offer): Long {
        return offerDao.insertOffer(offer.toEntity())
    }
    
    suspend fun updateOffer(offer: Offer) {
        offerDao.updateOffer(offer.toEntity())
    }
    
    suspend fun deleteOffer(offer: Offer) {
        // No eliminar ofertas por defecto
        if (!offer.isDefault) {
            offerDao.deleteOffer(offer.toEntity())
        }
    }
    
    suspend fun initializeDefaultOffers() {
        val count = offerDao.getOfferCount()
        if (count == 0) {
            // Insertar ofertas predefinidas
            val defaultOffers = Offer.getDefaultOffers().map { it.toEntity() }
            offerDao.insertAll(defaultOffers)
        }
    }
    
    /**
     * Calcula el precio final aplicando una oferta
     */
    fun calculateFinalPrice(quantity: Float, unitPrice: Float, offerCode: String?): Float? {
        if (unitPrice <= 0 || quantity <= 0) return null
        
        return when (offerCode) {
            "3x2" -> {
                val groups = (quantity / 3).toInt()
                val remainder = quantity % 3
                (groups * 2 + remainder) * unitPrice
            }
            "2x1" -> {
                val groups = (quantity / 2).toInt()
                val remainder = quantity % 2
                (groups * 1 + remainder) * unitPrice
            }
            "2nd_50" -> {
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.5f + remainder) * unitPrice
            }
            "2nd_70" -> {
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.3f + remainder) * unitPrice  // 100% + 30%
            }
            "4x3" -> {
                val groups = (quantity / 4).toInt()
                val remainder = quantity % 4
                (groups * 3 + remainder) * unitPrice
            }
            else -> quantity * unitPrice  // Sin oferta o custom manual
        }
    }

    /**
     * Calcula el precio final aplicando una oferta (versión suspend con OfferEntity)
     */
    suspend fun calculateFinalPrice(quantity: Float, unitPrice: Float, offer: OfferEntity?): Float {
        return when (offer?.code) {
            "3x2" -> {
                // Lleva 3 paga 2
                val groups = (quantity / 3).toInt()
                val remainder = quantity % 3
                (groups * 2 + remainder) * unitPrice
            }
            "2x1" -> {
                // Lleva 2 paga 1
                val groups = (quantity / 2).toInt()
                val remainder = quantity % 2
                (groups * 1 + remainder) * unitPrice
            }
            "2nd_50" -> {
                // 2ª unidad -50%
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.5f + remainder) * unitPrice
            }
            "2nd_70" -> {
                // 2ª unidad -70%
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.3f + remainder) * unitPrice
            }
            "4x3" -> {
                // Lleva 4 paga 3
                val groups = (quantity / 4).toInt()
                val remainder = quantity % 4
                (groups * 3 + remainder) * unitPrice
            }
            else -> quantity * unitPrice
        }
    }
    
    /**
     * Calcula el precio final para un producto con su oferta asignada
     */
    suspend fun calculateProductFinalPrice(product: Product): Float? {
        if (product.estimatedPrice == null) return null
        
        val offer = product.offerId?.let { offerDao.getOfferById(it)?.toDomain() }
        return calculateFinalPrice(product.quantity, product.estimatedPrice, offer?.code)
    }
    
    // ========== PASILLOS ==========
    
    suspend fun getAllAisles(): List<Aisle> {
        return aisleDao.getAllAisles().map { it.toDomain() }
    }
    
    suspend fun addAisle(aisle: Aisle): Long {
        return aisleDao.insertAisle(aisle.toEntity())
    }
    
    suspend fun updateAisle(aisle: Aisle) {
        aisleDao.updateAisle(aisle.toEntity())
    }
    
    suspend fun deleteAisle(aisle: Aisle) {
        aisleDao.deleteAisle(aisle.toEntity())
    }
    
    suspend fun initializeDefaultAisles() {
        val existing = aisleDao.getAllAisles()
        if (existing.isEmpty()) {
            Aisle.getDefaultAisles().forEach { 
                aisleDao.insertAisle(it.toEntity())
            }
        }
    }
    
    // ========== PRODUCTOS ==========
    
    suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts().map { it.toDomain() }
    }
    
    suspend fun getProductsByAisle(aisleId: Long): List<Product> {
        return productDao.getProductsByAisle(aisleId).map { it.toDomain() }
    }
    
    /**
     * Añade un producto calculando automáticamente el precio final con oferta
     */
    suspend fun addProduct(product: Product): Long {
        val finalPrice = calculateProductFinalPrice(product)
        val productWithFinalPrice = product.copy(finalPrice = finalPrice)
        return productDao.insertProduct(productWithFinalPrice.toEntity())
    }
    
    /**
     * Actualiza un producto recalculando el precio final
     */
    suspend fun updateProduct(product: Product) {
        val finalPrice = calculateProductFinalPrice(product)
        val productWithFinalPrice = product.copy(finalPrice = finalPrice)
        productDao.updateProduct(productWithFinalPrice.toEntity())
    }
    
    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }
    
    suspend fun toggleProductPurchased(product: Product) {
        val updated = product.copy(isPurchased = !product.isPurchased)
        productDao.updateProduct(updated.toEntity())
    }
    
    suspend fun deletePurchasedProducts() {
        productDao.deletePurchasedProducts()
    }
    
    // ========== TOTALES ==========
    
    /**
     * Obtiene los totales calculados: con ofertas, sin ofertas y ahorro
     */
    fun getTotals(): Flow<TotalsResult> = flow {
        val products = productDao.getAllProducts().map { it.toDomain() }
        
        val totalWithoutOffers = products.sumOf { 
            it.totalPriceWithoutOffer().toDouble() 
        }.toFloat()
        
        val totalWithOffers = products.sumOf { 
            it.finalPriceToPay().toDouble() 
        }.toFloat()
        
        val savings = totalWithoutOffers - totalWithOffers
        
        emit(TotalsResult(
            totalWithoutOffers = totalWithoutOffers,
            totalWithOffers = totalWithOffers,
            savings = savings
        ))
    }
    
    fun getTotalEstimate(): Flow<Float> = flow {
        val products = productDao.getAllProducts().map { it.toDomain() }
        val total = products.sumOf { it.finalPriceToPay().toDouble() }.toFloat()
        emit(total)
    }
    
    data class TotalsResult(
        val totalWithoutOffers: Float,
        val totalWithOffers: Float,
        val savings: Float
    )
    
    // ========== EXPORT/IMPORT JSON ==========
    
    suspend fun exportToJson(): String {
        val aisles = aisleDao.getAllAisles().map { it.toDomain() }
        val products = productDao.getAllProducts().map { it.toDomain() }
        
        val export = com.jose.listacompra.domain.model.ShoppingListExport(
            aisles = aisles.map { it.toExport() },
            products = products.map { it.toExport() }
        )
        
        return gson.toJson(export)
    }
    
    suspend fun importFromJson(json: String): Boolean {
        return try {
            val export = gson.fromJson(json, com.jose.listacompra.domain.model.ShoppingListExport::class.java)

            // Limpiar datos actuales
            productDao.deleteAllProducts()

            // Importar pasillos (solo custom)
            export.aisles.filter { !it.isDefault }.forEach { aisle ->
                aisleDao.insertAisle(
                    AisleEntity(
                        id = aisle.id,
                        name = aisle.name,
                        emoji = aisle.emoji,
                        orderIndex = aisle.orderIndex,
                        isDefault = aisle.isDefault
                    )
                )
            }

            // Importar productos
            export.products.forEach { product ->
                productDao.insertProduct(
                    ProductEntity(
                        id = 0, // Nuevo ID autogenerado
                        name = product.name,
                        aisleId = product.aisleId,
                        quantity = product.quantity,
                        estimatedPrice = product.estimatedPrice,
                        offerId = null, // Las ofertas no se exportan/importan
                        finalPrice = null,
                        isPurchased = product.isPurchased,
                        notes = product.notes,
                        orderIndex = product.orderIndex
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== HISTORIAL DE PRODUCTOS (AUTOCOMPLETADO) ==========

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

    private fun ProductHistoryEntity.toSuggestion(): ProductSuggestion {
        return ProductSuggestion(
            name = this.originalName,
            aisleId = this.aisleId,
            suggestedQuantity = this.lastQuantity,
            suggestedPrice = this.lastPrice,
            usageCount = this.usageCount
        )
    }
}
