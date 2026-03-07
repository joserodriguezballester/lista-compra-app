package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_frequency",
    indices = [Index("productName")]
)
data class ProductFrequencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val timesPurchased: Int = 1,
    val averageDaysBetween: Float? = null,
    val lastPurchaseDate: Long,
    val estimatedNextDate: Long? = null,
    val category: String? = null
)