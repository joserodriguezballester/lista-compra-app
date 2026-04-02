package com.jose.listacompra.ui.utils

import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.OfferPreviewResult
import com.jose.listacompra.domain.usecase.offers.CalculatePriceUseCase

/**
 * Calcula si una oferta se cumple con la cantidad actual
 */
data class OfferStatus(
    val needsMore: Boolean,
    val remaining: Int,
    val minQuantity: Int
)

fun calculateOfferStatus(quantity: Float, offer: Offer?): OfferStatus {
    if (offer == null) return OfferStatus(needsMore = false, remaining = 0, minQuantity = 0)
    
    val qty = quantity.toInt()
    val minQty = when (offer.code) {
        "3x2" -> 3
        "2x1" -> 2
        "2nd_50" -> 2
        "2nd_70" -> 2
        "4x3" -> 4
        else -> 1
    }
    
    val needsMore = qty < minQty
    val remaining = if (needsMore) minQty - qty else 0
    
    return OfferStatus(needsMore = needsMore, remaining = remaining, minQuantity = minQty)
}

/**
 * Wrapper para CalculatePriceUseCase que devuelve el resultado formateado
 */
fun calculateOfferPrice(
    quantity: Float,
    unitPrice: Float,
    offer: Offer?,
    calculatePriceUseCase: CalculatePriceUseCase
): OfferCalculationResult {
    val result = calculatePriceUseCase(quantity, unitPrice, offer?.code)
    
    return OfferCalculationResult(
        finalPrice = result.finalPrice,
        originalPrice = quantity * unitPrice,
        savings = result.savings,
        hasOffer = result.hasOffer,
        offerApplied = offer != null && result.savings > 0
    )
}

data class OfferCalculationResult(
    val finalPrice: Float,
    val originalPrice: Float,
    val savings: Float,
    val hasOffer: Boolean,
    val offerApplied: Boolean
)

/**
 * Obtiene el emoji de categoría para productos sin imagen
 */
fun getCategoryEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("leche") -> "🥛"
        lower.contains("pan") -> "🍞"
        lower.contains("huevo") -> "🥚"
        lower.contains("yogur") -> "🥛"
        lower.contains("queso") -> "🧀"
        lower.contains("tomate") -> "🍅"
        lower.contains("platano", "plátano") -> "🍌"
        lower.contains("manzana") -> "🍎"
        lower.contains("naranja") -> "🍊"
        lower.contains("pollo") -> "🍗"
        lower.contains("carne") -> "🥩"
        lower.contains("pescado") -> "🐟"
        lower.contains("galleta") -> "🍪"
        lower.contains("café") -> "☕"
        lower.contains("aceite") -> "🫒"
        lower.contains("agua") -> "💧"
        lower.contains("cerveza") -> "🍺"
        lower.contains("vino") -> "🍷"
        lower.contains("detergente") -> "🧴"
        lower.contains("papel") -> "🧻"
        lower.contains("jabón") -> "🧼"
        else -> "📦"
    }
}