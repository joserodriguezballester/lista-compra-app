package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import com.jose.listacompra.domain.model.ProductAisleMapping

/**
 * Guarda dónde se encuentra cada producto en cada supermercado
 * Ej: "Leche" en "Carrefour" = Pasillo 3 (id: 15)
 */
@Entity(
    tableName = "product_aisle_mappings",
    primaryKeys = ["productNameNormalized", "supermarketId"],
    indices = [
        Index("supermarketId"),
        Index("productNameNormalized"),
        Index("aisleId")
    ]
)
data class ProductAisleMappingEntity(
    val productNameNormalized: String,  // "LECHE ENTERA" (uppercase para búsqueda)
    val supermarketId: Long,
    val aisleId: Long,                // FK a supermarket_aisles
    val aisleName: String,              // ← NUEVO: "Pasillo 3 - Lácteos"
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 1             // Para ordenar por frecuencia
)

/**
 * Conversor a dominio
 */
fun ProductAisleMappingEntity.toDomain(): ProductAisleMapping {
    return ProductAisleMapping(
        productNameNormalized = this.productNameNormalized,
        supermarketId = this.supermarketId,
        aisleId = this.aisleId,
        aisleName = this.aisleName,  // ← Incluido
        lastUsed = this.lastUsed,
        useCount = this.useCount
    )
}

