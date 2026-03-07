package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supermarket_aisles",
    foreignKeys = [
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("supermarketId")]
)

data class SupermarketAisleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supermarketId: Long,       // FK a supermarket
    val name: String,               // "Pasillo 3 - Lácteos y yogures"
    val emoji: String = "🥛",
    val orderIndex: Int = 0,        // Orden dentro de ese super
    val categoryIds: String?,        // JSON [1, 2, 3] - categorías que contiene
    val isDefault: Boolean = true    // ¿Es pasillo del sistema o creado por user?
)