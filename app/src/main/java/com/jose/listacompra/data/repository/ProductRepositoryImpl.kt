package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : IProductRepository {

    override suspend fun getAllProducts(listId: Long): List<Product> {
        return productDao.getAllProducts(listId).map { it.toDomain() }
    }

    override suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(ProductEntity.fromDomain(product))
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(ProductEntity.fromDomain(product))
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(ProductEntity.fromDomain(product))
    }

    override suspend fun togglePurchased(productId: Long, isPurchased: Boolean) {
        productDao.updatePurchased(productId, isPurchased)
    }

    override fun getProductsByListFlow(listId: Long): Flow<List<Product>> {
        return productDao.getProductsByListFlow(listId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProductsBySupermarketFlow(listId: Long, supermarketId: Long): Flow<List<Product>> {
        return productDao.getProductsBySupermarketFlow(listId, supermarketId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updatePhoto(productId: Long, photoUri: String?) {
        productDao.updateProductPhoto(productId, photoUri)
    }

    override suspend fun updateEan(productId: Long, ean: String?) {
        productDao.updateEan(productId, ean)
    }

    override suspend fun getProductsByList(listId: Long): List<Product> {
        return productDao.getAllProducts(listId).map { it.toDomain() }
    }

    override suspend fun getNextOrderIndex(listId: Long): Int? {
        return productDao.getMaxOrderIndex(listId)
    }

    override suspend fun deletePurchasedProducts(listId: Long) {
        productDao.deletePurchasedProducts(listId)
    }

    override suspend fun deleteAllProductsFromList(listId: Long) {
        productDao.deleteAllProducts(listId)
    }

    override suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<Product> {
        return productDao.getProductsByAisle(listId, aisleId).map { it.toDomain() }
    }
}
