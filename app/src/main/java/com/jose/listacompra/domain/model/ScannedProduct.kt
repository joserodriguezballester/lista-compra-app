package com.jose.listacompra.domain.model

/**
 * Producto escaneado desde OpenFoodFacts
 * Usado para autocompletar datos al crear un artículo
 */
data class ScannedProduct(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val imageUrl: String?,
    val quantity: String?,
    val categoryTag: String?
)
