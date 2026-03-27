package com.jose.listacompra.domain.model

/**
 * Modelo de categoría de artículo
 * Las categorías sirven para clasificar artículos y asignar pasillos por defecto
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String = "📦"  // Emoji como icono
) {
    companion object {
        /**
         * Categorías por defecto
         */
        fun getDefaultCategories(): List<Category> = listOf(
            Category(1, "Frutas y Verduras", "🍎"),
            Category(2, "Carnes", "🥩"),
            Category(3, "Pescados", "🐟"),
            Category(4, "Lácteos", "🥛"),
            Category(5, "Panadería", "🍞"),
            Category(6, "Bebidas", "🥤"),
            Category(7, "Despensa", "🥫"),
            Category(8, "Congelados", "🧊"),
            Category(9, "Higiene", "🧴"),
            Category(10, "Limpieza", "🧼"),
            Category(11, "Mascotas", "🐕"),
            Category(12, "Bebé", "👶"),
            Category(13, "Hogar", "🏠"),
            Category(14, "Otros", "📦")
        )
    }
}
