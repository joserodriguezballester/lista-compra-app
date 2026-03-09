package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // Nombre de la lista (ej: "Carrefour Mislata")
    val supermarketId: Long? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),  // Timestamp automático
    val estado: String = "ACTIVA"  // "ACTIVA" o "ARCHIVADA"
)