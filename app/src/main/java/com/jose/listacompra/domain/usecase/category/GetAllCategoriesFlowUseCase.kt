package com.jose.listacompra.domain.usecase.category

import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.repository.ICategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener todas las categorías
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 */
@Singleton
class GetAllCategoriesFlowUseCase @Inject constructor(
    private val categoryRepository: ICategoryRepository
) {
    /**
     * Obtiene flow con todas las categorías
     */
    operator fun invoke(): Flow<List<Category>> {
        return categoryRepository.getAllCategories()
    }
}