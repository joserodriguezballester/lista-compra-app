package com.jose.listacompra.domain.usecase.supermarket

import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.ISupermarketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSupermarketsUseCase @Inject constructor(
    private val repository: ISupermarketRepository
) {
    operator fun invoke(): Flow<List<Supermarket>> {
        return repository.getAllSupermarkets()
    }
}
