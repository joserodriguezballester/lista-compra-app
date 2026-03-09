package com.jose.listacompra.domain.model

data class TotalsResult(
    val totalWithoutOffers: Float,
    val totalWithOffers: Float,
    val savings: Float
)