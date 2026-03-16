package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.converters.toEntity
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
@Singleton
class OfferRepositoryImpl @Inject constructor(
    private val offerDao: OfferDao
) : IOfferRepository {
    override suspend fun getAllOffers() = offerDao.getAllOffers().map { it.toDomain() }

    override suspend fun getOfferById(id: Long) = offerDao.getOfferById(id)?.toDomain()

    override suspend fun getDefaultOffers() = offerDao.getDefaultOffers().map { it.toDomain() }

    override suspend fun insertOffer(offer: Offer) = offerDao.insertOffer(offer.toEntity())

    override suspend fun updateOffer(offer: Offer) = offerDao.updateOffer(offer.toEntity())

    override suspend fun deleteOffer(offer: Offer) = offerDao.deleteOffer(offer.toEntity())

    override suspend fun getOfferCount() = offerDao.getOfferCount()

    override suspend fun insertAll(offers: List<Offer>) {
        offerDao.insertAll(offers.map { it.toEntity() })
    }
}