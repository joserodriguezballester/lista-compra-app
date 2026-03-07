package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.Index

/**
 * Guarda dónde se encuentra cada producto en cada supermercado
 * Ej: "Leche" en "Carrefour" = Pasillo 3 (id: 15)
 */
@Entity(
    tableName = "product_aisle_mappings",
    primaryKeys = ["productNameNormalized", "supermarketId"],
    indices = [Index("supermarketId"), Index("productNameNormalized")]
)
data class ProductAisleMappingEntity(
    val productNameNormalized: String,  // "LECHE ENTERA" (uppercase para búsqueda)
    val supermarketId: Long,
    val aisleId: Long,                   // FK a supermarket_aisles
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 1                // Para ordenar por frecuencia
)