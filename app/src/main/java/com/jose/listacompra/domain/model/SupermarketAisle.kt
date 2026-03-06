package com.jose.listacompra.domain.model

data class SupermarketAisle( val id: Long = 0,
                             val supermarketId: Long,
                             val name: String,
                             val emoji: String = "🛒",
                             val orderIndex: Int = 0,
                             val categoryIds: List<Long> = emptyList(),  // Categorías que contiene
                             val isDefault: Boolean = true
)
