package com.jose.listacompra.domain.util

import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.toDomain
import com.jose.listacompra.domain.model.Product

object PriceCalculator {

    /**
     * Calcula el precio final aplicando una oferta
     */
    fun calculateFinalPrice(quantity: Float, unitPrice: Float, offerCode: String?): Float? {
        if (unitPrice <= 0 || quantity <= 0) return null

        return when (offerCode) {
            "3x2" -> {
                val groups = (quantity / 3).toInt()
                val remainder = quantity % 3
                (groups * 2 + remainder) * unitPrice
            }
            "2x1" -> {
                val groups = (quantity / 2).toInt()
                val remainder = quantity % 2
                (groups * 1 + remainder) * unitPrice
            }
            "2nd_50" -> {
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.5f + remainder) * unitPrice
            }
            "2nd_70" -> {
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.3f + remainder) * unitPrice  // 100% + 30%
            }
            "4x3" -> {
                val groups = (quantity / 4).toInt()
                val remainder = quantity % 4
                (groups * 3 + remainder) * unitPrice
            }
            else -> quantity * unitPrice  // Sin oferta o custom manual
        }
    }
    /**
     * Calcula el precio final aplicando una oferta (versión suspend con OfferEntity)
     */
    fun calculateFinalPrice(quantity: Float, unitPrice: Float, offer: OfferEntity?): Float {
        return when (offer?.code) {
            "3x2" -> {
                // Lleva 3 paga 2
                val groups = (quantity / 3).toInt()
                val remainder = quantity % 3
                (groups * 2 + remainder) * unitPrice
            }
            "2x1" -> {
                // Lleva 2 paga 1
                val groups = (quantity / 2).toInt()
                val remainder = quantity % 2
                (groups * 1 + remainder) * unitPrice
            }
            "2nd_50" -> {
                // 2ª unidad -50%
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.5f + remainder) * unitPrice
            }
            "2nd_70" -> {
                // 2ª unidad -70%
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.3f + remainder) * unitPrice
            }
            "4x3" -> {
                // Lleva 4 paga 3
                val groups = (quantity / 4).toInt()
                val remainder = quantity % 4
                (groups * 3 + remainder) * unitPrice
            }
            else -> quantity * unitPrice
        }
    }

}