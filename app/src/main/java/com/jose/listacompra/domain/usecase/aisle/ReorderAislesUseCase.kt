package com.jose.listacompra.domain.usecase.aisle

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import javax.inject.Inject

class ReorderAislesUseCase @Inject constructor(
    private val repository: IAisleRepository
) {
    suspend operator fun invoke(reorderedList: List<Aisle>) {
        // La lógica de negocio: "El orden en la DB debe coincidir con la posición en la lista"
        val listWithNewIndices = reorderedList.mapIndexed { index, aisle ->
            aisle.copy(orderIndex = index)
        }
        // Enviamos la lista ya procesada al repositorio
        repository.updateAisles(listWithNewIndices)
    }
}