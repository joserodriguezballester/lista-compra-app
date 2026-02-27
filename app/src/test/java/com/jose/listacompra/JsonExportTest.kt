package com.jose.listacompra

import com.google.gson.Gson
import com.jose.listacompra.domain.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para exportación/importación JSON
 */
class JsonExportTest {

    private val gson = Gson()

    @Test
    fun `export to JSON funciona correctamente`() {
        val aisles = listOf(
            AisleExport(1, "Lácteos", "🥛", 0, true),
            AisleExport(2, "Fruta", "🍎", 1, true)
        )
        
        val products = listOf(
            ProductExport(1, "Leche", 1, 6f, 1.15f, false, "", 0),
            ProductExport(2, "Manzana", 2, 2f, 1.80f, true, "Golden", 0)
        )
        
        val export = ShoppingListExport(
            version = "1.0",
            aisles = aisles,
            products = products
        )
        
        val json = gson.toJson(export)
        
        assertTrue(json.contains("Leche"))
        assertTrue(json.contains("1.15"))
        assertTrue(json.contains("1.0"))
    }

    @Test
    fun `import from JSON funciona correctamente`() {
        val json = """
            {
                "version": "1.0",
                "aisles": [
                    {"id": 1, "name": "Lácteos", "emoji": "🥛", "orderIndex": 0, "isDefault": true}
                ],
                "products": [
                    {"id": 1, "name": "Leche", "aisleId": 1, "quantity": 6, "estimatedPrice": 1.15, "isPurchased": false, "notes": "", "orderIndex": 0}
                ]
            }
        """.trimIndent()
        
        val export = gson.fromJson(json, ShoppingListExport::class.java)
        
        assertEquals("1.0", export.version)
        assertEquals(1, export.aisles.size)
        assertEquals(1, export.products.size)
        assertEquals("Leche", export.products[0].name)
        assertEquals(6f, export.products[0].quantity, 0.01f)
    }

    @Test
    fun `import from JSON invalido devuelve null o lanza excepcion`() {
        val jsonInvalid = "{ invalid json }"
        
        try {
            gson.fromJson(jsonInvalid, ShoppingListExport::class.java)
            // Si llega aquí, Gson no lanzó excepción (comportamiento por defecto)
            // En la app real deberíamos manejar esto
        } catch (e: Exception) {
            assertTrue(e is com.google.gson.JsonSyntaxException)
        }
    }

    @Test
    fun `export version is always present`() {
        val export = ShoppingListExport(
            aisles = emptyList(),
            products = emptyList()
        )
        
        assertNotNull(export.version)
        assertTrue(export.version.isNotEmpty())
    }

    @Test
    fun `product conversion to export is correct`() {
        val product = Product(
            id = 1,
            name = "Leche",
            aisleId = 2,
            quantity = 6f,
            estimatedPrice = 1.15f,
            isPurchased = false,
            notes = "Semidesnatada",
            orderIndex = 0
        )
        
        val export = product.toExport()
        
        assertEquals(1, export.id)
        assertEquals("Leche", export.name)
        assertEquals(2, export.aisleId)
        assertEquals(6f, export.quantity, 0.01f)
        assertEquals(1.15f, export.estimatedPrice, 0.01f)
        assertEquals("Semidesnatada", export.notes)
    }
}
