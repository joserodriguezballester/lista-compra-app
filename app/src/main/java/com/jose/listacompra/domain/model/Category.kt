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
) {
    companion object {
        fun getDefaultCategories(): List<Category> = listOf(
            Category(name = "Lácteos", emoji = "🥛"),
            Category(name = "Bebidas", emoji = "🥤"),
            Category(name = "Galletas", emoji = "🍪"),
            Category(name = "Carnes", emoji = "🥩"),
            Category(name = "Pescados", emoji = "🐟"),
            Category(name = "Frutas y Verduras", emoji = "🍎"),
            Category(name = "Panadería", emoji = "🥖"),
            Category(name = "Congelados", emoji = "❄️"),
            Category(name = "Limpieza", emoji = "🧼"),
            Category(name = "Despensa", emoji = "🥫")
        )
    }
}
