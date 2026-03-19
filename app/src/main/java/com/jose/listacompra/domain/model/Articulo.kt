package com.jose.listacompra.domain.model

import coil.size.Size

data class Articulo(
    val id: Long = 0,
    val name: String,
    val finalPrice: Float? = null,
    val photoUri: String? = null,
    val ean: String? = null,
    val categoryId: Long = 0,
    val size: Float=1f,
    val unit: String="ud" // La unidad: "L", "ml", "kg", "g", "paquete"
)
