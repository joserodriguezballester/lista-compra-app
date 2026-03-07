package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val supermarketId: Long? = null,       // ← FK a supermarket (nullable)
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: String = "ACTIVA"
)