package com.jose.listacompra.data.local.dataseeder

/**
 * Datos de siembra para Articulo
 * Mantiene coherencia con el modelo de dominio Articulo
 * @see com.jose.listacompra.domain.model.Articulo
 */
data class SeedArticulo(
    val name: String,
    val categoryId: Long,
    val finalPrice: Float? = null,
    val size: Float = 1f,
    val unit: String = "ud",
    val ean: String? = null,
    val photoUri: String? = null
)

/**
 * Convierte SeedArticulo a modelo de dominio Articulo
 */
fun SeedArticulo.toArticulo() = com.jose.listacompra.domain.model.Articulo(
    name = name,
    categoryId = categoryId,
    finalPrice = finalPrice,
    size = size,
    unit = unit,
    ean = ean,
    photoUri = photoUri
)
