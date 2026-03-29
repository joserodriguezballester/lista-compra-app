package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.converters.toEntity
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : IProductRepository {

    override suspend fun getAllProducts(listId: Long): List<Product> =
        productDao.getAllProducts(listId).map { it.toDomain() }

    override suspend fun getProductById(productId: Long): Product? =
        productDao.getProductById(productId)?.toDomain()

    override suspend fun insertProduct(product: Product): Long =
        productDao.insertProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product.toEntity())

    override suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product.toEntity())

    override suspend fun updatePhoto(productId: Long, uri: String?) =
        productDao.updatePhotoUri(productId, uri)

    override suspend fun updateEan(productId: Long, ean: String?) =
        productDao.updateEan(productId, ean)

    override suspend fun getProductsByList(listId: Long): Flow<List<Product>> {
        return productDao.getProductsByListFlow(listId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNextOrderIndex(listId: Long): Int =
        (productDao.getMaxOrderIndex(listId) ?: -1) + 1

    override suspend fun deletePurchasedProducts(listId: Long) =
        productDao.deletePurchasedProducts(listId)

    override suspend fun deleteAllProductsFromList(listId: Long) =
        productDao.deleteAllProducts(listId)

    override suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<Product> =
        productDao.getProductsByAisle(listId, aisleId).map { it.toDomain() }
//
//    suspend fun getProductsByList(listId: Long, aisleId: Long): List<Product> {
//        return productDao.getProductsByAisle(listId, aisleId).map { it.toDomain() }
//    }

}
