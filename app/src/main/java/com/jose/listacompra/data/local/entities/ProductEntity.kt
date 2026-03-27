package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jose.listacompra.domain.model.Product

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArticuloEntity::class,
            parentColumns = ["id"],
            childColumns = ["articuloId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shoppingListId"), Index("articuloId"), Index("supermarketId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val shoppingListId: Long,    // FK a shopping_lists
    val articuloId: Long? = null, // FK al artículo del catálogo
    val supermarketId: Long = 1,  // FK al supermercado
    val quantity: Float = 1f,
    val estimatedPrice: Float? = null,
    val offerId: Long? = null,
    val finalPrice: Float? = null,
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0,
    val photoUri: String? = null,      // URI de la foto
    val ean: String? = null            // Código de barras EAN
) {
    fun toDomain(): Product = Product(
        id = id,
        name = name,
        aisleId = aisleId,
        shoppingListId = shoppingListId,
        articuloId = articuloId,
        supermarketId = supermarketId,
        quantity = quantity,
        estimatedPrice = estimatedPrice,
        offerId = offerId,
        finalPrice = finalPrice,
        isPurchased = isPurchased,
        notes = notes,
        orderIndex = orderIndex,
        photoUri = photoUri,
        ean = ean
    )
    
    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            name = product.name,
            aisleId = product.aisleId,
            shoppingListId = product.shoppingListId,
            articuloId = product.articuloId,
            supermarketId = product.supermarketId,
            quantity = product.quantity,
            estimatedPrice = product.estimatedPrice,
            offerId = product.offerId,
            finalPrice = product.finalPrice,
            isPurchased = product.isPurchased,
            notes = product.notes,
            orderIndex = product.orderIndex,
            photoUri = product.photoUri,
            ean = product.ean
        )
    }
}
