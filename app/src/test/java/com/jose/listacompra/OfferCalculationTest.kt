package com.jose.listacompra

import com.jose.listacompra.data.local.OfferEntity
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para cálculo de ofertas
 */
class OfferCalculationTest {

    // Simula la lógica de cálculo del Repository
    private fun calculateFinalPrice(quantity: Float, unitPrice: Float, offer: OfferEntity?): Float {
        return when (offer?.code) {
            "3x2" -> {
                val groups = (quantity / 3).toInt()
                val remainder = quantity % 3
                (groups * 2 + remainder) * unitPrice
            }
            "2nd_50" -> {
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.5f + remainder) * unitPrice
            }
            "2nd_70" -> {
                val pairs = (quantity / 2).toInt()
                val remainder = quantity % 2
                (pairs * 1.3f + remainder) * unitPrice
            }
            "2x1" -> {
                val groups = (quantity / 2).toInt()
                val remainder = quantity % 2
                (groups * 1 + remainder) * unitPrice
            }
            "4x3" -> {
                val groups = (quantity / 4).toInt()
                val remainder = quantity % 4
                (groups * 3 + remainder) * unitPrice
            }
            else -> quantity * unitPrice
        }
    }

    @Test
    fun `3x2 con 3 unidades paga 2`() {
        val offer = OfferEntity(code = "3x2", name = "3x2")
        val result = calculateFinalPrice(3f, 1.15f, offer)
        assertEquals(2.30f, result, 0.01f)  // 2 * 1.15
    }

    @Test
    fun `3x2 con 6 unidades paga 4`() {
        val offer = OfferEntity(code = "3x2", name = "3x2")
        val result = calculateFinalPrice(6f, 1.15f, offer)
        assertEquals(4.60f, result, 0.01f)  // 4 * 1.15
    }

    @Test
    fun `3x2 con 4 unidades paga 3`() {
        val offer = OfferEntity(code = "3x2", name = "3x2")
        val result = calculateFinalPrice(4f, 1.15f, offer)
        assertEquals(3.45f, result, 0.01f)  // 3 * 1.15 (2+1 del grupo, 1 suelta)
    }

    @Test
    fun `2x1 con 2 unidades paga 1`() {
        val offer = OfferEntity(code = "2x1", name = "2x1")
        val result = calculateFinalPrice(2f, 2.50f, offer)
        assertEquals(2.50f, result, 0.01f)  // 1 * 2.50
    }

    @Test
    fun `2nd_50 con 2 unidades aplica descuento`() {
        val offer = OfferEntity(code = "2nd_50", name = "2ª -50%")
        val result = calculateFinalPrice(2f, 2.00f, offer)
        assertEquals(3.00f, result, 0.01f)  // 2.00 + 1.00 (50%)
    }

    @Test
    fun `2nd_70 con 2 unidades aplica descuento mayor`() {
        val offer = OfferEntity(code = "2nd_70", name = "2ª -70%")
        val result = calculateFinalPrice(2f, 2.00f, offer)
        assertEquals(2.60f, result, 0.01f)  // 2.00 + 0.60 (70%)
    }

    @Test
    fun `sin oferta calcula precio normal`() {
        val result = calculateFinalPrice(5f, 1.15f, null)
        assertEquals(5.75f, result, 0.01f)  // 5 * 1.15
    }

    @Test
    fun `oferta desconocida calcula precio normal`() {
        val offer = OfferEntity(code = "UNKNOWN", name = "Unknown")
        val result = calculateFinalPrice(3f, 1.15f, offer)
        assertEquals(3.45f, result, 0.01f)  // 3 * 1.15 (sin oferta)
    }

    @Test
    fun `4x3 con 4 unidades paga 3`() {
        val offer = OfferEntity(code = "4x3", name = "4x3")
        val result = calculateFinalPrice(4f, 2.00f, offer)
        assertEquals(6.00f, result, 0.01f)  // 3 * 2.00
    }

    @Test
    fun `cantidad decimal funciona correctamente`() {
        val offer = OfferEntity(code = "3x2", name = "3x2")
        val result = calculateFinalPrice(3.5f, 1.00f, offer)
        assertEquals(3.00f, result, 0.01f)  // 2 + 1 (3 enteros) + 0.5 suelta
    }
}
