package com.jose.listacompra.domain.model

/**
 * Modelo de pasillo del supermercado
 */
data class Aisle(
    val id: Long = 0,
    val name: String,
    val emoji: String = "",
    val orderIndex: Int = 0,  // Para ordenar los pasillos
    val isDefault: Boolean = true  // Si es pasillo por defecto o añadido por usuario
) {
    companion object {
        /**
         * Lista de pasillos por defecto (Carrefour La Alberca)
         */
        fun getDefaultAisles(): List<Aisle> = listOf(
            Aisle(1, "Higiene y Belleza", "🧴", 0),
            Aisle(2, "Fruta y Verdura", "🍎", 1),
            Aisle(3, "Charcutería", "🥓", 2),
            Aisle(4, "Carnicería", "🥩", 3),
            Aisle(5, "Despensa - Pasillo 1: Galletas", "🥫", 4),
            Aisle(6, "Despensa - Pasillo 2: Chocolates", "🥫", 5),
            Aisle(7, "Despensa - Pasillo 3: Azúcar y Café", "🥫", 6),
            Aisle(8, "Despensa - Pasillo 4: Tomate Frito y Legumbres", "🥫", 7),
            Aisle(9, "Despensa - Pasillo 5: Aceite y Pastas", "🥫", 8),
            Aisle(10, "Papel", "🧻", 9),
            Aisle(11, "Droguería y Limpieza", "🧼", 10),
            Aisle(12, "Bebidas", "🥤", 11),
            Aisle(13, "Papas y Snacks", "🥜", 12),
            Aisle(14, "Bollería", "🥐", 13),
            Aisle(15, "Lácteos", "🥛", 14),
            Aisle(16, "Preparados", "🥪", 15),
            Aisle(17, "Quesos", "🧀", 16),
            Aisle(18, "Regalo (fidelización)", "🎁", 17),
            Aisle(19, "Congelados", "🧊", 18)
        )
    }
}
