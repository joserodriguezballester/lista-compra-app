package com.jose.listacompra.domain.model

/**
 * Datos de producto obtenidos del escaneo
 */
data class ScannedProduct(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val imageUrl: String?,
    val category: String?,
    val found: Boolean,
    val ean: String? = null
)