package com.jose.listacompra.domain.usecase.aisle

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import javax.inject.Inject

class InitializeAislesUseCase @Inject constructor(
    private val repository: IAisleRepository
) {
    suspend operator fun invoke() {
        val existing = repository.getAllAisles()

        if (existing.isEmpty()) {
            // Obtenemos los objetos de dominio por defecto
            val defaultAisles = Aisle.getDefaultAisles()

            // Los insertamos uno a uno (o creas un insertAll en el repo)
            defaultAisles.forEach { aisle ->
                repository.addAisle(aisle)
            }
        }
    }
}