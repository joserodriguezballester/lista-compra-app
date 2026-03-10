package com.jose.listacompra.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.ShoppingList

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringToMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromMapToString(map: Map<String, String>?): String? {
        if (map == null) return null
        return gson.toJson(map)
    }
}
// ==================== CONVERTERS DE PASILLO ====================

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
// ==================== CONVERTERS DE SHOPPING LIST ====================

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


// ==================== CONVERTERS DE OFERTA ====================

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

// ==================== CONVERTERS DE PRODUCTO ====================

fun ProductEntity.toDomain(): Product = Product(
    id = this.id,
    name = this.name,
  //  categoryId = this.categoryId,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex,
    // Campos de foto (si existen)
    photoUri = this.photoUri,
    ean = this.ean,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = this.id,
    name = this.name,
 //   categoryId = this.categoryId,
    aisleId = this.aisleId,
    shoppingListId = this.shoppingListId,
    quantity = this.quantity,
    estimatedPrice = this.estimatedPrice,
    offerId = this.offerId,
    finalPrice = this.finalPrice,
    isPurchased = this.isPurchased,
    notes = this.notes,
    orderIndex = this.orderIndex,
    photoUri = this.photoUri,
    ean = this.ean,
  )

// ==================== CONVERTERS DE MAP (JSON) ====================

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

// ==================== CONVERTERS DE LISTAS (si las tienes) ====================

fun List<String>.toJson(): String {
    return gson.toJson(this)
}

fun String.toStringList(): List<String> {
    val type = object : TypeToken<List<String>>() {}.type
    return gson.fromJson(this, type) ?: emptyList()
}
