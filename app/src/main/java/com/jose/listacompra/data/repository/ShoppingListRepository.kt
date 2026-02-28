package com.jose.listacompra.data.repository

import android.content.Context
import androidx.room.Room
import com.jose.listacompra.data.local.*
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.domain.model.ShoppingList
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
    
    private val shoppingListDao = db.shoppingListDao()
    private val aisleDao = db.aisleDao()
    private val offerDao = db.offerDao()
    private val productDao = db.productDao()
    private val historyDao = db.productHistoryDao()
    private val gson = Gson()
    
    // ========== LISTAS DE COMPRAS ==========
    
    suspend fun getActiveLists(): List<ShoppingList> {
        return shoppingListDao.getActiveLists().map { it.toDomain() }
    }
    
    suspend fun getArchivedLists(): List<ShoppingList> {
        return shoppingListDao.getArchivedLists().map { it.toDomain() }
    }
    
    suspend fun getAllLists(): List<ShoppingList> {
        return shoppingListDao.getAllLists().map { it.toDomain() }
    }
    
    suspend fun getListById(id: Long): ShoppingList? {
        return shoppingListDao.getListById(id)?.toDomain()
    }
    
    suspend fun createList(name: String, useDefaultAisles: Boolean = true): Long {
        val list = ShoppingListEntity(
            name = name,
            fechaCreacion = System.currentTimeMillis(),
            estado = "ACTIVA"
        )
        return shoppingListDao.insertList(list)
    }
    
    suspend fun updateList(list: ShoppingList) {
        shoppingListDao.updateList(list.toEntity())
    }
    
    suspend fun deleteList(list: ShoppingList) {
        // Solo permitir eliminar si está archivada
        if (list.isArchived()) {
            shoppingListDao.deleteList(list.toEntity())
        }
    }
    
    suspend fun archiveList(listId: Long) {
        shoppingListDao.archiveList(listId)
    }
    
    suspend fun unarchiveList(listId: Long) {
        shoppingListDao.unarchiveList(listId)
    }
    
    /**
     * Crea una lista por defecto si no existe ninguna
     */
    suspend fun createDefaultListIfNeeded(): Long {
        val lists = shoppingListDao.getAllLists()
        return if (lists.isEmpty()) {
            val defaultList = ShoppingListEntity(
                name = "Mi Lista",
                fechaCreacion = System.currentTimeMillis(),
                estado = "ACTIVA"
            )
            shoppingListDao.insertList(defaultList)
        } else {
            lists.first().id
        }
    }
    
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
    
    /**
     * Reordena los pasillos actualizando sus orderIndex
     */
    suspend fun reorderAisles(reorderedAisles: List<Aisle>) {
        // Actualizar el orderIndex de cada pasillo según su nueva posición
        val updatedAisles = reorderedAisles.mapIndexed { index, aisle ->
            aisle.copy(orderIndex = index).toEntity()
        }
        aisleDao.updateAisles(updatedAisles)
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
    
    suspend fun getAllProducts(listId: Long): List<Product> {
        return productDao.getAllProducts(listId).map { it.toDomain() }
    }
    
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<Product> {
        return productDao.getProductsByAisle(listId, aisleId).map { it.toDomain() }
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
     * Actualiza un producto existente con nuevos datos
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
    
    suspend fun deletePurchasedProducts(listId: Long) {
        productDao.deletePurchasedProducts(listId)
    }
    
    suspend fun deleteAllProductsFromList(listId: Long) {
        productDao.deleteAllProducts(listId)
    }
    
    // ========== TOTALES ==========
    
    /**
     * Obtiene los totales calculados: con ofertas, sin ofertas y ahorro
     */
    fun getTotals(listId: Long): Flow<TotalsResult> = flow {
        val products = productDao.getAllProducts(listId).map { it.toDomain() }
        
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
    
    fun getTotalEstimate(listId: Long): Flow<Float> = flow {
        val products = productDao.getAllProducts(listId).map { it.toDomain() }
        val total = products.sumOf { it.finalPriceToPay().toDouble() }.toFloat()
        emit(total)
    }
    
    data class TotalsResult(
        val totalWithoutOffers: Float,
        val totalWithOffers: Float,
        val savings: Float
    )
    
    // ========== EXPORT/IMPORT JSON ==========
    
    suspend fun exportToJson(listId: Long): String {
        val aisles = aisleDao.getAllAisles().map { it.toDomain() }
        val products = productDao.getAllProducts(listId).map { it.toDomain() }
        
        val export = com.jose.listacompra.domain.model.ShoppingListExport(
            aisles = aisles.map { it.toExport() },
            products = products.map { it.toExport() }
        )
        
        return gson.toJson(export)
    }
    
    suspend fun importFromJson(json: String, listId: Long): Boolean {
        return try {
            val export = gson.fromJson(json, com.jose.listacompra.domain.model.ShoppingListExport::class.java)

            // Limpiar datos actuales de la lista
            productDao.deleteAllProducts(listId)

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
                        shoppingListId = listId,
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
