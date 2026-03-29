package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.data.local.entities.SupermarketEntity

val supermarkets = listOf(
    SupermarketEntity(1, "Carrefour La Alberca", "🛒", isDefault = true),
    SupermarketEntity(2, "Mercadona Mislata", "🟢"),
    SupermarketEntity(3, "Lidl", "🔵"),
    SupermarketEntity(4, "Aldi", "🟡"),
    SupermarketEntity(5, "Consum", "🟠")
)