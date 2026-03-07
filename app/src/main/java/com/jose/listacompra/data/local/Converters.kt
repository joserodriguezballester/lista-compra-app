package com.jose.listacompra.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.CategoryEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.ShoppingList

// ==================== CONVERTIDORES DE SHOPPING LIST ====================

fun ShoppingListEntity.toDomain(): ShoppingList = ShoppingList(
    id = this.id,
    name = this.name,
    supermarketId = this.supermarketId,
    fechaCreacion = this.fechaCreacion,
    estado = this.estado
)

fun ShoppingList.toEntity(): ShoppingListEntity = ShoppingListEntity(
    id = this.id,
    name = this.name,
    supermarketId = this.supermarketId,
    fechaCreacion = this.fechaCreacion,
    estado = this.estado
)

// ==================== CONVERTIDORES DE CATEGORÍA (NUEVO) ====================

fun CategoryEntity.toDomain(): Category = Category(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    description = this.description,
    orderIndex = this.orderIndex
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    description = this.description,
    orderIndex = this.orderIndex
)

// ==================== CONVERTIDORES DE PASILLO ====================

fun AisleEntity.toDomain(): Aisle = Aisle(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    orderIndex = this.orderIndex,
    isDefault = this.isDefault
)

fun Aisle.toEntity(): AisleEntity = AisleEntity(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    orderIndex = this.orderIndex,
    isDefault = this.isDefault
)

// ==================== CONVERTIDORES DE OFERTA ====================

fun OfferEntity.toDomain(): Offer = Offer(
    id = this.id,
    code = this.code,
    name = this.name,
    description = this.description,
    isDefault = this.isDefault,
    formula = this.formula
)

fun Offer.toEntity(): OfferEntity = OfferEntity(
    id = this.id,
    code = this.code,
    name = this.name,
    description = this.description,
    isDefault = this.isDefault,
    formula = this.formula
)

// ==================== CONVERTIDORES DE PRODUCTO (ACTUALIZADO CON categoryId) ====================

fun ProductEntity.toDomain(): Product = Product(
    id = this.id,
    name = this.name,
    categoryId = this.categoryId,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex,
    aisleMap = this.aisleMap.toAisleMap() ,
    photoUri = this.photoUri,
    photoTimestamp = this.photoTimestamp,
    isPhotoUserSelected = this.isPhotoUserSelected
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = this.id,
    name = this.name,
    categoryId = this.categoryId,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex,
    aisleMap = this.aisleMap.toJsonString(),
    photoUri = this.photoUri,
    photoTimestamp = this.photoTimestamp,
    isPhotoUserSelected = this.isPhotoUserSelected
// ← AÑADIR
)
// Conversor de Map<String, String> ↔ JSON String
private val gson = Gson()

fun Map<String, String>?.toJsonString(): String? {
    return this?.let { gson.toJson(it) }
}

fun String?.toAisleMap(): Map<String, String>? {
    return this?.let {
        val type = object : TypeToken<Map<String, String>>() {}.type
        gson.fromJson(it, type)
    }
}