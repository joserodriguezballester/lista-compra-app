package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Frecuencia de compra de un producto para sugerencias
 */
@Entity(tableName = "product_frequency")
data class ProductFrequencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,          // Nombre normalizado (lowercase)
    val originalName: String,         // Nombre original para mostrar
    val timesPurchased: Int = 0,      // Veces comprado
    val lastPurchaseDate: Long = 0,   // Última fecha de compra
    val averageDaysBetween: Float? = null,
    val estimatedNextDate: Long? = null,
    val category: String? = null,
    // Campos para sugerencias
    val lastAisleId: Long = 0,        // Último pasillo donde se compró
    val lastQuantity: Float = 1f,     // Última cantidad comprada
    val lastPrice: Float = 0f,        // Último precio
    val lastSupermarketId: Long = 0,  // Último supermercado
    val preferredAisleId: Long = 0    // Pasillo preferido (más usado)
)