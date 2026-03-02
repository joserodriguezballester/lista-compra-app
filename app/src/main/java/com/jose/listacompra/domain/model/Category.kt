package com.jose.listacompra.domain.model

/**
 * Modelo de categoría de producto
 * Las categorías son taxonomías lógicas del producto (Lácteos, Bebidas, Galletas...)
 * Independientes del pasillo/supermercado
 */
data class Category(
    val id: Long = 0,
    val name: String,           // Nombre visible: "Lácteos", "Bebidas"
    val emoji: String,          // Emoji representativo: "🥛", "🥤"
    val description: String = "", // Descripción opcional
    val orderIndex: Int = 0     // Para ordenar las categorías
)
