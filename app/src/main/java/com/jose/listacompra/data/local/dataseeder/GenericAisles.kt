package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.domain.model.Aisle

/**
 * Pasillos genéricos para supermercados sin configuración específica
 * Cada supermercado tiene sus pasillos con IDs únicos para evitar conflictos
 */
val genericAisles: List<Aisle> by lazy {
    buildList {
        // Mercadona (supermarketId = 2)
        addAll(listOf(
            Aisle(100, "Frutas y Verduras", "🍎", 0, 2),
            Aisle(101, "Panadería", "🍞", 1, 2),
            Aisle(102, "Carnicería", "🥩", 2, 2),
            Aisle(103, "Pescadería", "🐟", 3, 2),
            Aisle(104, "Lácteos", "🥛", 4, 2),
            Aisle(105, "Despensa", "🥫", 5, 2),
            Aisle(106, "Bebidas", "🥤", 6, 2),
            Aisle(107, "Limpieza", "🧼", 7, 2),
            Aisle(108, "Higiene", "🧴", 8, 2),
            Aisle(109, "Congelados", "🧊", 9, 2)
        ))
        
        // Lidl (supermarketId = 3)
        addAll(listOf(
            Aisle(200, "Frutas y Verduras", "🍎", 0, 3),
            Aisle(201, "Panadería", "🍞", 1, 3),
            Aisle(202, "Lácteos", "🥛", 2, 3),
            Aisle(203, "Despensa", "🥫", 3, 3),
            Aisle(204, "Bebidas", "🥤", 4, 3),
            Aisle(205, "Limpieza", "🧼", 5, 3)
        ))
        
        // Aldi (supermarketId = 4)
        addAll(listOf(
            Aisle(300, "Frutas y Verduras", "🍎", 0, 4),
            Aisle(301, "Panadería", "🍞", 1, 4),
            Aisle(302, "Lácteos", "🥛", 2, 4),
            Aisle(303, "Despensa", "🥫", 3, 4),
            Aisle(304, "Bebidas", "🥤", 4, 4),
            Aisle(305, "Limpieza", "🧼", 5, 4)
        ))
        
        // Consum (supermarketId = 5)
        addAll(listOf(
            Aisle(400, "Frutas y Verduras", "🍎", 0, 5),
            Aisle(401, "Panadería", "🍞", 1, 5),
            Aisle(402, "Carnicería", "🥩", 2, 5),
            Aisle(403, "Lácteos", "🥛", 3, 5),
            Aisle(404, "Despensa", "🥫", 4, 5),
            Aisle(405, "Bebidas", "🥤", 5, 5),
            Aisle(406, "Limpieza", "🧼", 6, 5)
        ))
    }
}
