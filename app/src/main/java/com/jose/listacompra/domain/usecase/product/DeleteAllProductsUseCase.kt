package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.repository.IProductRepository
import javax.inject.Inject

class DeleteAllProductsUseCase @Inject constructor(
    private val repository: IProductRepository
) {
    suspend operator fun invoke(listId: Long) {
        repository.deleteAllProductsFromList(listId)
    }
}