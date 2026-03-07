package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/* NUEVO: Tabla de categorías */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,          // "Lácteos", "Bebidas", "Galletas"
    val emoji: String,         // "🥛", "🥤", "🍪"
    val description: String = "",
    val orderIndex: Int = 0    // Para ordenarlas
)