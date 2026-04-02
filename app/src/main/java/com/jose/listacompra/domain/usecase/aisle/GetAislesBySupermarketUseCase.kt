package com.jose.listacompra.domain.usecase.aisle

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener pasillos por supermercado
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 */
@Singleton
class GetAislesBySupermarketUseCase @Inject constructor(
    private val aisleRepository: IAisleRepository
) {
    /**
     * Obtiene pasillos de un supermercado específico
     */
    suspend operator fun invoke(supermarketId: Long): List<Aisle> {
        return aisleRepository.getAislesBySupermarket(supermarketId)
    }
}