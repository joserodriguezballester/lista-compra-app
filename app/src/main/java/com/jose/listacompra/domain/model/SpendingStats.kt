package com.jose.listacompra.domain.model

data class SpendingStats (
    val averagePerPurchase: Float,
    val totalSpent: Float,
    val totalPurchasesCount: Int
)