package com.jose.listacompra.domain.usecase.category

import com.jose.listacompra.domain.repository.ICategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: ICategoryRepository
) {
    suspend operator fun invoke(categoryId: Long) {
        repository.deleteCategory(categoryId)
    }
}
