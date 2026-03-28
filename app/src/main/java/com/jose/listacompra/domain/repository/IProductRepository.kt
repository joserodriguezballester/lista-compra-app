package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    suspend fun getAllProducts(listId: Long): List<Product>
    suspend fun getProductById(id: Long): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun togglePurchased(productId: Long, isPurchased: Boolean)
    fun getProductsByListFlow(listId: Long): Flow<List<Product>>
    fun getProductsBySupermarketFlow(listId: Long, supermarketId: Long): Flow<List<Product>>
    
    // Métodos adicionales
    suspend fun updatePhoto(productId: Long, photoUri: String?)
    suspend fun updateEan(productId: Long, ean: String?)
    suspend fun getNextOrderIndex(listId: Long): Int?
    suspend fun deletePurchasedProducts(listId: Long)
    suspend fun deleteAllProductsFromList(listId: Long)
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<Product>
}
