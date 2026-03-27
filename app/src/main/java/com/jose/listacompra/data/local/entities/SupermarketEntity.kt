package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jose.listacompra.domain.model.Supermarket

@Entity(tableName = "supermarkets")
data class SupermarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🏪",
    val isDefault: Boolean = false
) {
    fun toDomain(): Supermarket = Supermarket(
        id = id,
        name = name,
        emoji = emoji,
        isDefault = isDefault
    )
    
    companion object {
        fun fromDomain(supermarket: Supermarket): SupermarketEntity = SupermarketEntity(
            id = supermarket.id,
            name = supermarket.name,
            emoji = supermarket.emoji,
            isDefault = supermarket.isDefault
        )
    }
}
