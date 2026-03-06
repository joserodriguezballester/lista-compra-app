package com.jose.listacompra.domain.model

/**
 * Modelo de producto con campos de foto
 * VERSION CON FOTOS - añadir a tu Product.kt existente
 */
data class Product(
    val id: Long = 0,
    val name: String,
    val categoryId: Long? = null,
    val aisleId: Long = 0,
    val shoppingListId: Long = 1,
    val quantity: Float = 1f,
    val estimatedPrice: Float? = null,
    val offerId: Long? = null,
    val finalPrice: Float? = null,
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0,
    // CAMPOS DE FOTO - NUEVOS
    val photoUri: String? = null,
    val photoTimestamp: Long? = null,
    val isPhotoUserSelected: Boolean = false
) {
    // ... tus funciones existentes ...
    
    fun hasPhoto(): Boolean = photoUri != null
    
    fun totalPriceWithoutOffer(): Float = (estimatedPrice ?: 0f) * quantity
    
    fun finalPriceToPay(): Float = finalPrice ?: totalPriceWithoutOffer()
    
    /**
     * Devuelve emoji segun categoria si no hay foto
     */
    fun getDefaultEmoji(): String {
        return when (categoryId) {
            1L -> "🥛" // Lacteos
            2L -> "🥤" // Bebidas
            3L -> "🍪" // Galletas
            4L -> "🥩" // Carnes
            5L -> "🐟" // Pescados
            6L -> "🍎" // Frutas y Verduras
            7L -> "🥖" // Panaderia
            8L -> "❄️" // Congelados
            9L -> "🧼" // Limpieza
            10L -> "🥫" // Despensa
            else -> "🛒"
        }
    }
}
