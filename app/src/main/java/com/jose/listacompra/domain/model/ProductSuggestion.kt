package com.jose.listacompra.domain.model

/**
 * Sugerencia de autocompletado para productos
 */
data class ProductSuggestion(
    val name: String,
    val aisleId: Long,
    val suggestedQuantity: Float,
    val suggestedPrice: Float?,
    val usageCount: Int
)
