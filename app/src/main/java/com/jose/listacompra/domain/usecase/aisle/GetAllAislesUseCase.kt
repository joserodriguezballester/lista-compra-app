package com.jose.listacompra.domain.usecase.aisle

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import javax.inject.Inject

class GetAllAislesUseCase @Inject constructor(
    private val repository: IAisleRepository
) {
    suspend operator fun invoke(): List<Aisle> {
        return repository.getAllAisles().sortedBy { it.orderIndex }
    }
}