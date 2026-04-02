package com.jose.listacompra.data.local.dataseeder

/**
 * Productos de ejemplo para la lista de la compra inicial
 * Incluye algunos con supermercado preferido indicado en notas
 */
data class InitialSeedProduct(
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val price: Float?,
    val notes: String? = null
)

/**
 * IDs de pasillos de Carrefour (supermercado por defecto):
 * 1 = Panadería
 * 2 = Frutas y Verduras
 * 3 = Carnicería
 * 4 = Charcutería y Quesos
 * 5 = Pescadería
 * 6 = Congelados
 * 7 = Lácteos
 * 8 = Despensa
 * 9 = Bebidas
 * 10 = Bodega
 * 11 = Higiene y Belleza
 * 12 = Droguería y Limpieza
 * 13 = Mascotas
 * 14 = Papel
 * 15 = Bebé
 * 16 = Hogar y Textil
 * 17 = Electro
 * 18 = Otros
 * 19 = Quesos
 */

val initialProducts = listOf(
    // Lácteos
    InitialSeedProduct("Leche entera", 7, 6f, 1.15f),
    InitialSeedProduct("Yogures naturales", 7, 1f, 1.40f),
    InitialSeedProduct("Leche desnatada", 7, 3f, 1.20f, "del Consum"),
    
    // Panadería
    InitialSeedProduct("Pan de molde", 1, 1f, 1.50f, "del Mercadona"),
    InitialSeedProduct("Barra de pan", 1, 2f, 0.60f),
    
    // Frutas y Verduras
    InitialSeedProduct("Tomates", 2, 1f, 1.80f),
    InitialSeedProduct("Plátanos", 2, 1f, 1.20f),
    InitialSeedProduct("Manzanas", 2, 1f, 1.95f),
    
    // Despensa
    InitialSeedProduct("Galletas María", 8, 1f, 1.00f),
    InitialSeedProduct("Aceite de oliva", 8, 1f, 9.50f),
    InitialSeedProduct("Arroz", 8, 1f, 1.30f),
    InitialSeedProduct("Aceite girasol", 8, 1f, 3.00f, "del Lidl"),
    InitialSeedProduct("Café molido", 8, 1f, 3.20f),
    
    // Charcutería
    InitialSeedProduct("Jamón york", 4, 1f, 2.30f),
    InitialSeedProduct("Huevos docena", 4, 1f, 2.10f),
    
    // Quesos
    InitialSeedProduct("Queso rallado", 19, 1f, 1.85f),
    
    // Bebidas
    InitialSeedProduct("Zumo de naranja", 9, 1f, 2.80f),
    
    // Droguería
    InitialSeedProduct("Detergente", 12, 1f, 6.99f),
    
    // Papel
    InitialSeedProduct("Papel higiénico", 14, 1f, 4.50f)
)
