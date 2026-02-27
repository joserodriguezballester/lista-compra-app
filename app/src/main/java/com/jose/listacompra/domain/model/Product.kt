package com.jose.listacompra.domain.model

/**
 * Modelo de producto en la lista de la compra
 * Incluye soporte para ofertas y cálculo de precio final
 */
data class Product(
    val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val quantity: Float = 1f,
    val estimatedPrice: Float? = null,    // Precio unitario normal
    val offerId: Long? = null,            // FK a oferta (nullable)
    val finalPrice: Float? = null,        // Precio calculado con oferta aplicada
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0               // Para ordenar dentro del pasillo
) {
    /**
     * Calcula el precio total sin ofertas (precio unitario * cantidad)
     */
    fun totalPriceWithoutOffer(): Float = (estimatedPrice ?: 0f) * quantity
    
    /**
     * Obtiene el precio final a pagar (con oferta aplicada si existe)
     */
    fun finalPriceToPay(): Float = finalPrice ?: totalPriceWithoutOffer()
    
    /**
     * Calcula el ahorro por la oferta aplicada
     */
    fun savings(): Float = totalPriceWithoutOffer() - finalPriceToPay()
    
    /**
     * Indica si el producto tiene una oferta aplicada
     */
    fun hasOffer(): Boolean = offerId != null && finalPrice != null
    
    /**
     * Obtiene el precio unitario real pagado (considerando ofertas)
     */
    fun effectiveUnitPrice(): Float {
        return if (quantity > 0) finalPriceToPay() / quantity else 0f
    }
}
