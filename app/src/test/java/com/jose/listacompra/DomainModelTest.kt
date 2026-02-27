package com.jose.listacompra

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para modelos de dominio
 */
class DomainModelTest {

    @Test
    fun `product totalPrice calcula correctamente`() {
        val product = Product(
            id = 1,
            name = "Leche",
            aisleId = 1,
            quantity = 6f,
            estimatedPrice = 1.15f
        )
        assertEquals(6.90f, product.totalPrice(), 0.01f)
    }

    @Test
    fun `product totalPrice sin precio devuelve cero`() {
        val product = Product(
            id = 1,
            name = "Tomates",
            aisleId = 2,
            quantity = 2f,
            estimatedPrice = null
        )
        assertEquals(0f, product.totalPrice(), 0.01f)
    }

    @Test
    fun `product totalPrice con cantidad cero devuelve cero`() {
        val product = Product(
            id = 1,
            name = "Test",
            aisleId = 1,
            quantity = 0f,
            estimatedPrice = 5.00f
        )
        assertEquals(0f, product.totalPrice(), 0.01f)
    }

    @Test
    fun `aisle getDefaultAisles devuelve 19 pasillos`() {
        val aisles = Aisle.getDefaultAisles()
        assertEquals(19, aisles.size)
    }

    @Test
    fun `aisle getDefaultAisles incluye higiene y belleza`() {
        val aisles = Aisle.getDefaultAisles()
        val firstAisle = aisles.first()
        assertEquals("Higiene y Belleza", firstAisle.name)
        assertEquals("🧴", firstAisle.emoji)
    }

    @Test
    fun `aisle getDefaultAisles orden es correcto`() {
        val aisles = Aisle.getDefaultAisles()
        assertEquals(0, aisles[0].orderIndex)
        assertEquals(1, aisles[1].orderIndex)
        assertEquals(18, aisles[18].orderIndex)
    }

    @Test
    fun `product copy funciona correctamente`() {
        val product = Product(
            id = 1,
            name = "Leche",
            aisleId = 1,
            quantity = 6f,
            estimatedPrice = 1.15f,
            isPurchased = false
        )
        
        val purchased = product.copy(isPurchased = true)
        
        assertEquals(1, purchased.id)
        assertEquals("Leche", purchased.name)
        assertTrue(purchased.isPurchased)
        assertFalse(product.isPurchased)  // Original no cambia
    }
}
