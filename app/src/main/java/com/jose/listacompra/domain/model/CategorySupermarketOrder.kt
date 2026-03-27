package com.jose.listacompra.domain.model

/**
 * Define el orden de una categoría dentro de un supermercado específico.
 * Cuando un supermercado no tiene pasillos personalizados, se usan las categorías
 * ordenadas según esta tabla.
 */
data class CategorySupermarketOrder(
    val id: Long = 0,
    val categoryId: Long,        // FK a Category
    val supermarketId: Long,     // FK a Supermarket
    val orderIndex: Int          // Orden en ese supermercado
)
