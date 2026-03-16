package com.jose.listacompra.domain.usecase.offers

import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject

class GetAllOffersUseCase @Inject constructor(
    private val repository: IOfferRepository
) {
    suspend operator fun invoke(): List<Offer> = repository.getAllOffers()
}