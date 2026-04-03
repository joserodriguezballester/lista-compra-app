package com.jose.listacompra.ui.utils

import com.jose.listacompra.domain.model.Articulo

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
        lower.contains("platano") || lower.contains("plátano") -> "🍌"
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

/**
 * Calcula si una oferta se cumple con la cantidad actual
 */
data class OfferStatus(
    val needsMore: Boolean,
    val remaining: Int
)

fun calculateOfferStatus(quantity: Float, offerCode: String?): OfferStatus {
    if (offerCode == null) return OfferStatus(needsMore = false, remaining = 0)
    
    val qty = quantity.toInt()
    val minQty = when (offerCode) {
        "3x2", "3X2" -> 3
        "2x1", "2X1" -> 2
        "2nd_50" -> 2
        "2nd_70" -> 2
        "4x3", "4X3" -> 4
        else -> 1
    }
    
    val needsMore = qty < minQty
    val remaining = if (needsMore) minQty - qty else 0
    
    return OfferStatus(needsMore = needsMore, remaining = remaining)
}