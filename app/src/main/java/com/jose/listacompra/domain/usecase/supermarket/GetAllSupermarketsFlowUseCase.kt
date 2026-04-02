package com.jose.listacompra.domain.usecase.supermarket

import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.ISupermarketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener todos los supermercados
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 */
@Singleton
class GetAllSupermarketsFlowUseCase @Inject constructor(
    private val supermarketRepository: ISupermarketRepository
) {
    /**
     * Obtiene flow con todos los supermercados
     */
    operator fun invoke(): Flow<List<Supermarket>> {
        return supermarketRepository.getAllSupermarkets()
    }
}