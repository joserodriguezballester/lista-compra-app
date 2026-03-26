package com.jose.listacompra.data.local.dataseeder

data class SeedProduct(
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val price: Float?
)