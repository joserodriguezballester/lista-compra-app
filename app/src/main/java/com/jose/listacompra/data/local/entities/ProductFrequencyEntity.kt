package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_frequency",
    indices = [Index("productName")]
)
data class ProductFrequencyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,           // Nombre normalizado del producto
    val timesPurchased: Int = 1,       // Veces comprado
    val averageDaysBetween: Float? = null, // Días medios entre compras
    val lastPurchaseDate: Long,        // Última compra
    val estimatedNextDate: Long? = null, // Próxima compra estimada
    val category: String? = null       // Categoría/pasillo
)