package com.jose.listacompra.domain.repository

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    // Lectura
    suspend fun getAllProducts(listId: Long): List<Product>
    suspend fun getProductById(productId: Long): Product?
    suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<Product>
    suspend fun getNextOrderIndex(listId: Long): Int

    // Escritura
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun deletePurchasedProducts(listId: Long)
    suspend fun deleteAllProductsFromList(listId: Long)

    // Updates específicos
    suspend fun updatePhoto(productId: Long, uri: String?)
    suspend fun updateEan(productId: Long, ean: String?)

    fun getProductsByList(listId: Long): Flow<List<Product>>

}