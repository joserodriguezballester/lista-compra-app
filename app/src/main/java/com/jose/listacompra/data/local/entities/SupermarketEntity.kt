package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supermarkets")
data class SupermarketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,              // "Carrefour"
    val displayName: String,       // "Carrefour La Alberca"
    val emoji: String = "🏪",
    val isDefault: Boolean = false, // Uno por defecto
    val orderIndex: Int = 0
)