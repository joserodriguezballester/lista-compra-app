package com.jose.listacompra.domain.usecase.list

import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject

class UpdateListUseCase @Inject constructor(
    private val repository: IShoppingListRepository
) {
    suspend operator fun invoke(list: ShoppingList) {
        repository.updateList(list)
    }
}