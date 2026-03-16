package com.jose.listacompra.domain.usecase.list

import android.R.attr.name
import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject

class GetDefaultListUseCase @Inject constructor(
    private val repository: IShoppingListRepository
) {
    suspend operator fun invoke(): Long {
        val lists = repository.getAllLists()
        return if (lists.isEmpty()) {
            repository.createList(
                name = "Mi Lista",
                useDefaultAisles = true
            )
        } else {
            lists.first().id
        }
    }
}