package com.jose.listacompra.domain.usecase.list

import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject

class DeleteListUseCase @Inject constructor(
    private val repository: IShoppingListRepository
) {
    suspend operator fun invoke(list: ShoppingList) {
        // La lógica de "solo borrar si está archivada" se mantiene en el repo o aquí
        repository.deleteList(list)
    }
}