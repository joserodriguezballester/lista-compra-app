package com.jose.listacompra.domain.model

/**
 * Modelo de producto en la lista de la compra
 * ACTUALIZADO: Ahora incluye categoryId para agrupar por categoría
 */
data class Product(
    val id: Long = 0,
    val name: String,
    val categoryId: Long?,
    val aisleId: Long? = null,  // Deprecated
    val shoppingListId: Long = 1,
    val quantity: Float = 1f,
    val estimatedPrice: Float? = null,
    val offerId: Long? = null,
    val finalPrice: Float? = null,
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0,
    val aisleMap: Map<String, String>? = null , // ← NUEVO
    val photoUri: String? = null,
    val photoTimestamp: Long? = null,
    val isPhotoUserSelected: Boolean = false
) {
    /**
     * Indica si el producto tiene foto personalizada
     */
    fun hasPhoto(): Boolean = photoUri != null

    /**
     * Genera un emoji por defecto según la categoría
     * (uso si no hay foto)
     */
    fun getDefaultEmoji(): String {
        return when (categoryId) {
            1L -> "🥛"  // Lácteos
            2L -> "🥤"  // Bebidas
            3L -> "🍪"  // Galletas
            4L -> "🥩"  // Carnes
            else -> "🛒"
        }
    }
    companion object {
        fun generatePhotoFileName(name: String): String {
            return "product_${System.currentTimeMillis()}_${name.replace(" ", "_")}.jpg"
        }
    }
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
