package com.jose.listacompra.domain.usecase.list

import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject

class CreateListUseCase @Inject constructor(
    private val repository: IShoppingListRepository
) {
    // Añadimos useDefaultAisles con un valor por defecto
    suspend operator fun invoke(name: String, useDefaultAisles: Boolean = true): Long {
        return repository.createList(name, useDefaultAisles)
    }


}