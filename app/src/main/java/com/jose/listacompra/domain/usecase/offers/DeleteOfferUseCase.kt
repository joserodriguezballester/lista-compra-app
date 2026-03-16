package com.jose.listacompra.domain.usecase.offers

import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject

class DeleteOfferUseCase @Inject constructor(
    private val repository: IOfferRepository
) {
    suspend operator fun invoke(offer: Offer) {
        // La REGLA DE NEGOCIO vive aquí
        if (!offer.isDefault) {
            repository.deleteOffer(offer)
        }
    }
}