package com.jose.listacompra.domain.usecase.aisle

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import javax.inject.Inject

class UpdateAisleUseCase @Inject constructor(
    private val repository: IAisleRepository
) {
    suspend operator fun invoke(aisle: Aisle) = repository.updateAisle(aisle)
}