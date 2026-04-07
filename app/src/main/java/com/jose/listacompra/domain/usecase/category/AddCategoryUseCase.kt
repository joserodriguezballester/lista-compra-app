package com.jose.listacompra.domain.usecase.category

import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.repository.ICategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: ICategoryRepository
) {
    suspend operator fun invoke(category: Category): Long {
        return repository.insertCategory(category)
    }
}
