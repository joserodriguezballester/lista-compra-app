package com.jose.listacompra.domain.model


data class ProductAisleMapping(
    val productNameNormalized: String,
    val supermarketId: Long,
    val aisleId: Long,
    val aisleName: String,
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 1
)