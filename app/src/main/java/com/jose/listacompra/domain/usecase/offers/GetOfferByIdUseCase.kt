package com.jose.listacompra.domain.usecase.offers

import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject

class GetOfferByIdUseCase @Inject constructor(
    private val repository: IOfferRepository
) {
    suspend operator fun invoke(id: Long): Offer? = repository.getOfferById(id)
}