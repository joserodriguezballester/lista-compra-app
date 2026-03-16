package com.jose.listacompra.domain.usecase.list

import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject

class GetListByIdUseCase @Inject constructor(
    private val repository: IShoppingListRepository
) {
    suspend operator fun invoke(id: Long): ShoppingList? {
        return repository.getListById(id)
    }
}
