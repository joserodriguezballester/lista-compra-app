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
}
