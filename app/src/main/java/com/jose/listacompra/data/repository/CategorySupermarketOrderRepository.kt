package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.CategorySupermarketOrderDao
import com.jose.listacompra.data.local.entities.CategorySupermarketOrderEntity
import com.jose.listacompra.domain.model.CategorySupermarketOrder
import com.jose.listacompra.domain.repository.ICategorySupermarketOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorySupermarketOrderRepository @Inject constructor(
    private val dao: CategorySupermarketOrderDao
) : ICategorySupermarketOrderRepository {
    
    override fun getOrdersBySupermarket(supermarketId: Long): Flow<List<CategorySupermarketOrder>> {
        return dao.getOrdersBySupermarket(supermarketId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getOrdersBySupermarketOnce(supermarketId: Long): List<CategorySupermarketOrder> {
        return dao.getOrdersBySupermarketOnce(supermarketId).map { it.toDomain() }
    }
    
    override suspend fun insertOrder(order: CategorySupermarketOrder): Long {
        return dao.insertOrder(CategorySupermarketOrderEntity.fromDomain(order))
    }
    
    override suspend fun insertAll(orders: List<CategorySupermarketOrder>) {
        dao.insertAll(orders.map { CategorySupermarketOrderEntity.fromDomain(it) })
    }
    
    override suspend fun updateOrdersForSupermarket(supermarketId: Long, orders: List<CategorySupermarketOrder>) {
        val entities = orders.map { CategorySupermarketOrderEntity.fromDomain(it) }
        dao.updateOrdersForSupermarket(supermarketId, entities)
    }
    
    override suspend fun deleteOrdersForSupermarket(supermarketId: Long) {
        dao.deleteOrdersForSupermarket(supermarketId)
    }
}
