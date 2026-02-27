package com.jose.listacompra.domain.model

/**
 * Modelo para exportación/importación JSON
 */
data class ShoppingListExport(
    val version: String = "1.0",
    val exportDate: Long = System.currentTimeMillis(),
    val aisles: List<AisleExport>,
    val products: List<ProductExport>
)

data class AisleExport(
    val id: Long,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val isDefault: Boolean
)

data class ProductExport(
    val id: Long,
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val estimatedPrice: Float?,
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int
)

/**
 * Convierte lista de productos a formato de exportación
 */
fun List<Product>.toExportFormat(aisles: List<Aisle>): ShoppingListExport {
    return ShoppingListExport(
        aisles = aisles.map { it.toExport() },
        products = this.map { it.toExport() }
    )
}

fun Product.toExport(): ProductExport {
    return ProductExport(
        id = this.id,
        name = this.name,
        aisleId = this.aisleId,
        quantity = this.quantity,
        estimatedPrice = this.estimatedPrice,
        isPurchased = this.isPurchased,
        notes = this.notes,
        orderIndex = this.orderIndex
    )
}

fun Aisle.toExport(): AisleExport {
    return AisleExport(
        id = this.id,
        name = this.name,
        emoji = this.emoji,
        orderIndex = this.orderIndex,
        isDefault = this.isDefault
    )
}
