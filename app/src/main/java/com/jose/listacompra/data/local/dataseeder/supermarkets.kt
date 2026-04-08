package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.data.local.entities.SupermarketEntity

/**
 * Supermercados por defecto
 * El primer supermercado (Carrefour La Alberca) es el por defecto
 * 
 * T4: Supermercado "Cualquiera" (id=0) para productos sin supermercado específico
 */
val defaultSupermarkets = listOf(
    SupermarketEntity(0, "Cualquiera", "📦", isDefault = false),
    SupermarketEntity(1, "Carrefour La Alberca", "🛒", isDefault = true),
    SupermarketEntity(2, "Mercadona Mislata", "🟢"),
    SupermarketEntity(3, "Lidl", "🔵"),
    SupermarketEntity(4, "Aldi", "🟡"),
    SupermarketEntity(5, "Consum", "🟠")
)
