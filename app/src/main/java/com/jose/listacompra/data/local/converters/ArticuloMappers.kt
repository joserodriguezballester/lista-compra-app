package com.jose.listacompra.data.local.converters

import com.jose.listacompra.data.local.entities.ArticuloEntity
import com.jose.listacompra.domain.model.Articulo

fun ArticuloEntity.toDomain(): Articulo {
    return Articulo(
        id = id,
        name = name,
        photoUri = photoUri,
        ean = ean,
        finalPrice = basePrice,
        categoryId = categoryId,
        size = size,
        unit = unit
    )
}

fun Articulo.toEntity(): ArticuloEntity {
    return ArticuloEntity(
        id = id,
        name = name,
        basePrice = finalPrice,
        photoUri = photoUri,
        ean = ean,
        categoryId = categoryId,
        size = size,
        unit = unit
    )
}
/**
 * Función extra para mostrar el tamaño formateado en la UI o el PDF
 * Ejemplo: "1.0 L" o "500.0 g"
 */
fun Articulo.getFormattedSize(): String {
    return "$size $unit"
}