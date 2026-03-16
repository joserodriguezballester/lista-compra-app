package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Offer

interface IOfferRepository {
    suspend fun getAllOffers(): List<Offer>
    suspend fun getOfferById(id: Long): Offer?
    suspend fun getDefaultOffers(): List<Offer>
    suspend fun insertOffer(offer: Offer): Long
    suspend fun updateOffer(offer: Offer)
    suspend fun deleteOffer(offer: Offer)
    suspend fun getOfferCount(): Int
    suspend fun insertAll(offers: List<Offer>)
}