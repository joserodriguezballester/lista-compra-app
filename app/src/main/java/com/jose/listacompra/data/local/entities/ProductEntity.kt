package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("shoppingListId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val shoppingListId: Long,    // FK a shopping_lists
    val quantity: Float,
    val estimatedPrice: Float?,  // Precio unitario normal
    val offerId: Long?,          // FK a offers (nullable)
    val finalPrice: Float?,      // Precio calculado con oferta aplicada
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int
)