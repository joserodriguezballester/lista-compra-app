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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val shoppingListId: Long,    // FK a shopping_lists
    val quantity: Float= 1f,
    val estimatedPrice: Float? = null,
    val offerId: Long? = null,
    val finalPrice: Float? = null,
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0,
    val photoUri: String? = null,      // ← NUEVO: URI de la foto
    val ean: String? = null              // ← NUEVO: Código de barras EAN
)