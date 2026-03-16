package com.jose.listacompra.domain.usecase.offers

import com.jose.listacompra.domain.model.OfferPreviewResult
import com.jose.listacompra.domain.repository.IOfferRepository
import javax.inject.Inject

// domain/usecase/offer/CalculatePriceUseCase.kt
class CalculatePriceUseCase @Inject constructor() { // Ya no necesita el repo

    operator fun invoke(quantity: Float, unitPrice: Float, offerCode: String?): OfferPreviewResult {
        val finalPriceValue = if (offerCode == null) {
            quantity * unitPrice
        } else {
            when (offerCode) {
                "3x2" -> {
                    val groupsOfThree = (quantity / 3).toInt()
                    val remaining = quantity % 3
                    ((groupsOfThree * 2) + remaining) * unitPrice
                }

                "2x1" -> {
                    val groupsOfTwo = (quantity / 2).toInt()
                    val remaining = quantity % 2
                    (groupsOfTwo + remaining) * unitPrice
                }

                "2nd_50" -> {
                    val pairs = (quantity / 2).toInt()
                    val remaining = quantity % 2
                    ((pairs * 1.5f) + remaining) * unitPrice
                }
                // Segunda unidad al 70% (típico de Carrefour)
                "2nd_70" -> {
                    val pairs = (quantity / 2).toInt()
                    val remaining = quantity % 2
                    ((pairs * 1.3f) + remaining) * unitPrice
                }

                "4x3" -> {
                    val groupsOfFour = (quantity / 4).toInt()
                    val remaining = quantity % 4
                    ((groupsOfFour * 3) + remaining) * unitPrice
                }
                // ... el resto de tu lógica when
                else -> quantity * unitPrice
            }
        }
        // 2. Calculamos el total sin oferta para sacar el ahorro
        val totalWithoutOffer = quantity * unitPrice
        // 3. RETORNAMOS EL OBJETO (Esto es lo que te faltaba)
        return OfferPreviewResult(
            finalPrice = finalPriceValue,
            savings = totalWithoutOffer - finalPriceValue,
            hasOffer = offerCode != null
        )
    }
}
