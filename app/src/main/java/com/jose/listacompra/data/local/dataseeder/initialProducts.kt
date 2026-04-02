package com.jose.listacompra.data.local.dataseeder

/**
 * Productos de ejemplo para la lista de la compra inicial
 * Incluye algunos con supermercado preferido y ofertas
 */
data class InitialSeedProduct(
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val price: Float?,
    val notes: String? = null,
    val offerId: Long? = null  // 1=3x2, 2=2x1, 3=2ª-50%, 4=2ª-70%, 5=4x3
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
 * 
 * IDs de ofertas:
 * 1 = 3x2 (necesita 3 uds)
 * 2 = 2x1 (necesita 2 uds)
 * 3 = 2ª-50% (necesita 2 uds)
 * 4 = 2ª-70% (necesita 2 uds)
 * 5 = 4x3 (necesita 4 uds)
 */

val initialProducts = listOf(
    // === PRODUCTOS CON OFERTAS CUMPLIDAS ===
    
    // 3x2 - Yogures (3 uds = oferta cumplida)
    InitialSeedProduct("Yogures naturales", 7, 3f, 1.40f, offerId = 1),
    
    // 2x1 - Galletas (2 uds = oferta cumplida)
    InitialSeedProduct("Galletas María", 8, 2f, 1.00f, offerId = 2),
    
    // 2ª-50% - Aceite (2 uds = oferta cumplida)
    InitialSeedProduct("Aceite de oliva", 8, 2f, 4.75f, offerId = 3),
    
    // === PRODUCTOS CON OFERTAS SIN CUMPLIR ===
    
    // 3x2 - Leche (solo 2 uds, necesita 3)
    InitialSeedProduct("Leche entera", 7, 2f, 1.15f, offerId = 1),
    
    // 4x3 - Papel higiénico (solo 2 uds, necesita 4)
    InitialSeedProduct("Papel higiénico", 14, 2f, 4.50f, offerId = 5),
    
    // 2ª-70% - Café (solo 1 ud, necesita 2)
    InitialSeedProduct("Café molido", 8, 1f, 3.20f, offerId = 4),
    
    // === PRODUCTOS SIN OFERTA ===
    
    // Lácteos
    InitialSeedProduct("Leche desnatada", 7, 3f, 1.20f, "del Consum"),
    
    // Panadería
    InitialSeedProduct("Pan de molde", 1, 1f, 1.50f, "del Mercadona"),
    InitialSeedProduct("Barra de pan", 1, 2f, 0.60f),
    
    // Frutas y Verduras
    InitialSeedProduct("Tomates", 2, 1f, 1.80f),
    InitialSeedProduct("Plátanos", 2, 1f, 1.20f),
    InitialSeedProduct("Manzanas", 2, 1f, 1.95f),
    
    // Despensa
    InitialSeedProduct("Arroz", 8, 1f, 1.30f),
    InitialSeedProduct("Aceite girasol", 8, 1f, 3.00f, "del Lidl"),
    
    // Charcutería
    InitialSeedProduct("Jamón york", 4, 1f, 2.30f),
    InitialSeedProduct("Huevos docena", 4, 1f, 2.10f),
    
    // Quesos
    InitialSeedProduct("Queso rallado", 19, 1f, 1.85f),
    
    // Bebidas
    InitialSeedProduct("Zumo de naranja", 9, 1f, 2.80f),
    
    // Droguería
    InitialSeedProduct("Detergente", 12, 1f, 6.99f)
)