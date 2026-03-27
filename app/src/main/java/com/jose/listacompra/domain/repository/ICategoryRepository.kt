package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun insertAll(categories: List<Category>)
    suspend fun deleteCategory(id: Long)
}
