package com.jose.listacompra.domain.usecase.offers

import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de uso: Obtener todas las ofertas
 * Clean Architecture - ViewModel no debe acceder directo a Repository
 */
@Singleton
class GetAllOffersUseCase @Inject constructor(
    private val offerRepository: IOfferRepository
) {
    /**
     * Obtiene todas las ofertas disponibles
     * Si no hay ninguna, crea las ofertas predefinidas
     */
    suspend operator fun invoke(): List<Offer> {
        var offers = offerRepository.getAllOffers()
        
        // Si no hay ofertas, crear las predefinidas
        if (offers.isEmpty()) {
            val defaultOffers = listOf(
                Offer(1, "3x2", "3x2", "Compra 3 y paga 2", true, "price * 2 / 3"),
                Offer(2, "2x1", "2x1", "Compra 2 y paga 1", true, "price / 2"),
                Offer(3, "2nd_50", "2ª -50%", "Segunda unidad al 50%", true, "price * 1.5"),
                Offer(4, "2nd_70", "2ª -70%", "Segunda unidad al 30%", true, "price * 1.3"),
                Offer(5, "4x3", "4x3", "Compra 4 y paga 3", true, "price * 3 / 4")
            )
            offerRepository.insertAll(defaultOffers)
            offers = defaultOffers
        }
        
        return offers
    }
}