package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Productos frecuentes/historial para autocompletado
 */
@Entity(tableName = "product_history")
data class ProductHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // Nombre normalizado (lowercase para búsquedas)
    val originalName: String,   // Nombre como lo escribió el usuario
    val aisleId: Long,
    val lastQuantity: Float,
    val lastPrice: Float?,
    val usageCount: Int = 1,    // Cuántas veces se ha usado (para ordenar por frecuencia)
    val lastUsed: Long = System.currentTimeMillis()
)