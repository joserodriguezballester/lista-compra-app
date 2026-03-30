package com.jose.listacompra.data.local.dataseeder

/**
 * Datos de siembra para productos del historial
 * Usado para productos frecuentes de Carrefour
 */
data class SeedProduct(
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val price: Float?
)
