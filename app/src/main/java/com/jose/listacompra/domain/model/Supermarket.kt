package com.jose.listacompra.domain.model

data class Supermarket(
    val id: Long = 0,
    val name: String,
    val displayName: String,
    val emoji: String = "🏪",
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
) {
    companion object {
        fun getDefaults(): List<Supermarket> = listOf(
            Supermarket(1, "carrefour", "Carrefour", "🛒", true),
            Supermarket(2, "mercadona", "Mercadona", "🍅"),
            Supermarket(3, "lidl", "Lidl", "🥨"),
            Supermarket(4, "aldi", "Aldi", "🥐")
        )
    }
}
