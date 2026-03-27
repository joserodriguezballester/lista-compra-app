package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.CategorySupermarketOrder
import kotlinx.coroutines.flow.Flow

interface ICategorySupermarketOrderRepository {
    fun getOrdersBySupermarket(supermarketId: Long): Flow<List<CategorySupermarketOrder>>
    suspend fun getOrdersBySupermarketOnce(supermarketId: Long): List<CategorySupermarketOrder>
    suspend fun insertOrder(order: CategorySupermarketOrder): Long
    suspend fun insertAll(orders: List<CategorySupermarketOrder>)
    suspend fun updateOrdersForSupermarket(supermarketId: Long, orders: List<CategorySupermarketOrder>)
    suspend fun deleteOrdersForSupermarket(supermarketId: Long)
}
