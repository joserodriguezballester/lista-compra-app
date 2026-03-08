package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,        // Código corto: "3x2", "2nd_50", "custom"
    val name: String,        // Nombre visible: "3x2", "2ª unidad -50%"
    val description: String, // Descripción larga
    val isDefault: Boolean,  // true = predefinida, false = custom del usuario
    val formula: String      // Fórmula de cálculo (para referencia)
)