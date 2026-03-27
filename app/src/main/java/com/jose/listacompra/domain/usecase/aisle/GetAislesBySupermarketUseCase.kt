package com.jose.listacompra.domain.usecase.aisle

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAislesBySupermarketUseCase @Inject constructor(
    private val repository: IAisleRepository
) {
    operator fun invoke(supermarketId: Long): Flow<List<Aisle>> {
        return repository.getAislesBySupermarketFlow(supermarketId)
    }
}
