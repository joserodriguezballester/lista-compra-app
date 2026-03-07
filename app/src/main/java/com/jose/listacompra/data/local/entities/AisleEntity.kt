package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aisles")
data class AisleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val isDefault: Boolean
)