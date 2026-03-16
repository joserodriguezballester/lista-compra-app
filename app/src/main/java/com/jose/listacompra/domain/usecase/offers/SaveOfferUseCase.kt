package com.jose.listacompra.domain.usecase.offers

import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject

class SaveOfferUseCase @Inject constructor(
    private val repository: IOfferRepository
) {
    suspend operator fun invoke(offer: Offer) {
        if (offer.id == 0L) {
            repository.insertOffer(offer)
        } else {
            repository.updateOffer(offer)
        }
    }
}