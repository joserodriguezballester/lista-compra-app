package com.jose.listacompra.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articulos")
data class ArticuloEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val basePrice: Float? = null,
    val photoUri: String? = null,
    @ColumnInfo(index = true) // Índice para que el Scanner sea instantáneo
    val ean: String? = null,
    val categoryId: Long = 1,  // Por defecto: Sin categoría (ID 1)
    val size: Float=1f,
    val unit: String="ud" // La unidad: "L", "ml", "kg", "g", "paquete"
)
