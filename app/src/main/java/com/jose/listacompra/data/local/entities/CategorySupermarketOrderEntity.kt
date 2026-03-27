package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jose.listacompra.domain.model.CategorySupermarketOrder

@Entity(
    tableName = "category_supermarket_orders",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("supermarketId"),
        Index(value = ["categoryId", "supermarketId"], unique = true)
    ]
)
data class CategorySupermarketOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val supermarketId: Long,
    val orderIndex: Int
) {
    fun toDomain(): CategorySupermarketOrder = CategorySupermarketOrder(
        id = id,
        categoryId = categoryId,
        supermarketId = supermarketId,
        orderIndex = orderIndex
    )
    
    companion object {
        fun fromDomain(order: CategorySupermarketOrder): CategorySupermarketOrderEntity = 
            CategorySupermarketOrderEntity(
                id = order.id,
                categoryId = order.categoryId,
                supermarketId = order.supermarketId,
                orderIndex = order.orderIndex
            )
    }
}
