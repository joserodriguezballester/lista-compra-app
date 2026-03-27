package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jose.listacompra.domain.model.Aisle

@Entity(
    tableName = "aisles",
    foreignKeys = [
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supermarketId")]
)
data class AisleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val supermarketId: Long = 1,  // FK al supermercado
    val isDefault: Boolean
) {
    fun toDomain(): Aisle = Aisle(
        id = id,
        name = name,
        emoji = emoji,
        orderIndex = orderIndex,
        supermarketId = supermarketId,
        isDefault = isDefault
    )
    
    companion object {
        fun fromDomain(aisle: Aisle): AisleEntity = AisleEntity(
            id = aisle.id,
            name = aisle.name,
            emoji = aisle.emoji,
            orderIndex = aisle.orderIndex,
            supermarketId = aisle.supermarketId,
            isDefault = aisle.isDefault
        )
    }
}
