package com.jose.listacompra.domain.model

/**
 * Modelo de supermercado
 */
data class Supermarket(
    val id: Long = 0,
    val name: String,
    val emoji: String = "🏪",
    val isDefault: Boolean = false
) {
    companion object {
        /**
         * Supermercados por defecto
         */
        fun getDefaultSupermarkets(): List<Supermarket> = listOf(
            Supermarket(1, "Carrefour La Alberca", "🛒", isDefault = true),
            Supermarket(2, "Mercadona Mislata", "🟢"),
            Supermarket(3, "Lidl", "🔵"),
            Supermarket(4, "Aldi", "🟡"),
            Supermarket(5, "Consum", "🟠")
        )
    }
}
