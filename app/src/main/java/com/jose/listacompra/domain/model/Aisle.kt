package com.jose.listacompra.domain.model

/**
 * Modelo de pasillo del supermercado
 * Cada supermercado tiene sus propios pasillos con un orden específico
 */
data class Aisle(
    val id: Long = 0,
    val name: String,
    val emoji: String = "",
    val orderIndex: Int = 0,        // Orden dentro del supermercado
    val supermarketId: Long = 1,     // FK al supermercado
    val isDefault: Boolean = true    // Si es pasillo por defecto o añadido por usuario
) {
    companion object {
        /**
         * Pasillos por defecto para Carrefour La Alberca
         */
        fun getDefaultAislesForCarrefour(): List<Aisle> = listOf(
            Aisle(1, "Higiene y Belleza", "🧴", 0, 1),
            Aisle(2, "Fruta y Verdura", "🍎", 1, 1),
            Aisle(3, "Charcutería", "🥓", 2, 1),
            Aisle(4, "Carnicería", "🥩", 3, 1),
            Aisle(5, "Despensa - Pasillo 1: Galletas", "🥫", 4, 1),
            Aisle(6, "Despensa - Pasillo 2: Chocolates", "🥫", 5, 1),
            Aisle(7, "Despensa - Pasillo 3: Azúcar y Café", "🥫", 6, 1),
            Aisle(8, "Despensa - Pasillo 4: Tomate Frito y Legumbres", "🥫", 7, 1),
            Aisle(9, "Despensa - Pasillo 5: Aceite y Pastas", "🥫", 8, 1),
            Aisle(10, "Papel", "🧻", 9, 1),
            Aisle(11, "Droguería y Limpieza", "🧼", 10, 1),
            Aisle(12, "Bebidas", "🥤", 11, 1),
            Aisle(13, "Papas y Snacks", "🥜", 12, 1),
            Aisle(14, "Bollería", "🥐", 13, 1),
            Aisle(15, "Lácteos", "🥛", 14, 1),
            Aisle(16, "Preparados", "🥪", 15, 1),
            Aisle(17, "Quesos", "🧀", 16, 1),
            Aisle(18, "Regalo (fidelización)", "🎁", 17, 1),
            Aisle(19, "Congelados", "🧊", 18, 1)
        )
    }
}
