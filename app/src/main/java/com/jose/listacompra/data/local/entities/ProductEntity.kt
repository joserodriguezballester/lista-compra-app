package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/* MODIFICADO: ProductEntity ahora tiene categoryId */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.Companion.SET_NULL
        )
    ],
    indices = [Index("shoppingListId"), Index("categoryId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long?,  // FK a categories (nullable)
    val aisleId: Long?,     // ← HACER NULLABLE (mantener para compatibilidad)
    val shoppingListId: Long,
    val quantity: Float,
    val estimatedPrice: Float?,
    val offerId: Long?,
    val finalPrice: Float?,
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int,
    val aisleMap: String? = null , // ← NUEVO: JSON {"carrefour":"Pasillo 3","mercadona":"Pasillo 2"}
    // NUEVOS CAMPOS PARA FOTO
    val photoUri: String? = null,      // URI de la imagen seleccionada
    val photoTimestamp: Long? = null,  // Para ordenar por recientes
    val isPhotoUserSelected: Boolean = false  // true = elegida por usuario, false = default



)