package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jose.listacompra.domain.model.ArticuloSupermarketDefault

@Entity(
    tableName = "articulo_supermarket_defaults",
    foreignKeys = [
        ForeignKey(
            entity = ArticuloEntity::class,
            parentColumns = ["id"],
            childColumns = ["articuloId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AisleEntity::class,
            parentColumns = ["id"],
            childColumns = ["aisleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("articuloId"),
        Index("supermarketId"),
        Index("aisleId"),
        Index(value = ["articuloId", "supermarketId"], unique = true)
    ]
)
data class ArticuloSupermarketDefaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articuloId: Long,
    val supermarketId: Long,
    val aisleId: Long
) {
    fun toDomain(): ArticuloSupermarketDefault = ArticuloSupermarketDefault(
        id = id,
        articuloId = articuloId,
        supermarketId = supermarketId,
        aisleId = aisleId
    )
    
    companion object {
        fun fromDomain(default: ArticuloSupermarketDefault): ArticuloSupermarketDefaultEntity = 
            ArticuloSupermarketDefaultEntity(
                id = default.id,
                articuloId = default.articuloId,
                supermarketId = default.supermarketId,
                aisleId = default.aisleId
            )
    }
}
