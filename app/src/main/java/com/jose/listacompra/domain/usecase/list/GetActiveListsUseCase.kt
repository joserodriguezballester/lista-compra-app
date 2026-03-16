package com.jose.listacompra.domain.usecase.list

import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject

class GetActiveListsUseCase @Inject constructor(
    private val repository: IShoppingListRepository
) {
    suspend operator fun invoke() = repository.getActiveLists()
}