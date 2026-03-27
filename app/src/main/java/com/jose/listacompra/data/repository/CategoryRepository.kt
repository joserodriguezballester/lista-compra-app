package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.CategoryDao
import com.jose.listacompra.data.local.entities.CategoryEntity
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.repository.ICategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) : ICategoryRepository {
    
    override fun getAllCategories(): Flow<List<Category>> {
        return dao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCategoryById(id: Long): Category? {
        return dao.getCategoryById(id)?.toDomain()
    }
    
    override suspend fun insertCategory(category: Category): Long {
        return dao.insertCategory(CategoryEntity.fromDomain(category))
    }
    
    override suspend fun insertAll(categories: List<Category>) {
        dao.insertAll(categories.map { CategoryEntity.fromDomain(it) })
    }
    
    override suspend fun deleteCategory(id: Long) {
        dao.deleteCategory(id)
    }
}
