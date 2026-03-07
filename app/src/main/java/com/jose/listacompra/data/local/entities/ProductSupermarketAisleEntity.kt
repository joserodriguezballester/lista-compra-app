package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.Index

/**
 * Guarda qué pasillo se usó para cada producto en cada supermercado
 * Ej: "Leche" + "Carrefour" = "Pasillo 3"
 */
@Entity(
    tableName = "product_supermarket_aisle",
    primaryKeys = ["productName", "supermarket"],
    indices = [Index("supermarket")]
)
data class ProductSupermarketAisleEntity(
    val productName: String,     // "Leche Hacendado" (normalized)
    val supermarket: String,     // "carrefour", "mercadona"
    val aisleName: String,       // "Pasillo 3 - Lácteos"
    val aisleId: Long?,          // Si tienes IDs de pasillos
    val lastUsed: Long = System.currentTimeMillis()  // Para ordenar por frecuencia
)