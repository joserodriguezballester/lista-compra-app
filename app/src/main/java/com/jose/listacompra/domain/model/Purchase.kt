package com.jose.listacompra.domain.model

data class Purchase(
    val id: Long = 0,
    val date: Long,
    val total: Float,
    val savings: Float,
    val storeName: String= "Carrefour",
    val productCount: Int,
    val items: List<Product> = emptyList()
)
