package com.jose.listacompra.domain.model

/**
 * Asociación entre Artículo y Supermercado para guardar el pasillo por defecto
 * Cuando un artículo se añade a una lista en un supermercado concreto,
 * se recuerda en qué pasillo se colocó para sugerirlo la próxima vez
 */
data class ArticuloSupermarketDefault(
    val id: Long = 0,
    val articuloId: Long,
    val supermarketId: Long,
    val aisleId: Long
)
