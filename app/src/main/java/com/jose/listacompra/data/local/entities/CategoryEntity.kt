package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jose.listacompra.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val icon: String = "📦"
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        icon = icon
    )
    
    companion object {
        fun fromDomain(category: Category): CategoryEntity = CategoryEntity(
            id = category.id,
            name = category.name,
            icon = category.icon
        )
    }
}
