package com.jose.listacompra.utils

import com.jose.listacompra.domain.model.Offer

/**
 * Calcula el precio final aplicando una oferta
 */
fun calculateOfferPrice(
    unitPrice: Float,
    quantity: Float,
    offer: Offer
): Float {
    return when (offer.code) {
        "3x2" -> unitPrice * (quantity.toInt() / 3 * 2 + quantity.toInt() % 3)
        "2x1" -> unitPrice * (quantity.toInt() / 2 + quantity.toInt() % 2)
        "2nd_50" -> {
            val fullPrice = quantity.toInt() / 2 * unitPrice
            val halfPrice = quantity.toInt() / 2 * unitPrice * 0.5f
            fullPrice + halfPrice + (quantity.toInt() % 2) * unitPrice
        }
        "2nd_70" -> {
            val fullPrice = quantity.toInt() / 2 * unitPrice
            val discountedPrice = quantity.toInt() / 2 * unitPrice * 0.3f
            fullPrice + discountedPrice + (quantity.toInt() % 2) * unitPrice
        }
        "4x3" -> unitPrice * (quantity.toInt() / 4 * 3 + quantity.toInt() % 4)
        else -> unitPrice * quantity
    }
}

/**
 * Formatea un precio con 2 decimales
 */
fun formatPrice(price: Float?): String {
    if (price == null) return "0,00 €"
    return String.format("%.2f €", price)
}
