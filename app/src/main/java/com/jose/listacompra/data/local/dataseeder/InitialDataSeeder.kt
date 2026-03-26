package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.data.local.dao.ArticuloDao
import com.jose.listacompra.data.local.entities.ArticuloEntity
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Articulo

/**
 * Carga datos iniciales de la lista de Carrefour de Jose
 * Se ejecuta la primera vez que se abre la app
 */
object InitialDataSeeder {

    /**
     * Productos de la lista "lista-carro-buena.txt" (Carrefour Mislata)
     * Precios estimados basados en el archivo
     */
    private val carrefourProducts = listOf(
        // HIGIENE Y BELLEZA (ID: 1)
        SeedProduct("Máquina de afeitar", 1, 1f, 10f), // Promedio 5-15€

        // PANADERÍA (mapeado a BOLLERÍA ID: 14 o podemos usar ID existente)
        // Como no hay Panadería en default, usamos Despensa Galletas (ID: 5) o Bollería (ID: 14)
        SeedProduct("Pan de payés", 14, 1f, null),
        SeedProduct("Pan", 14, 1f, null),

        // FRUTA Y VERDURA (ID: 2)
        SeedProduct("Tomates", 2, 1f, null),
        SeedProduct("Calabacines", 2, 2f, null),
        SeedProduct("Berenjenas", 2, 2f, null),
        SeedProduct("Pimientos rojos", 2, 2f, null),
        SeedProduct("Plátanos", 2, 1f, 1.50f),
        SeedProduct("Manzana Golden", 2, 1f, 1.80f),
        SeedProduct("Manzana roja", 2, 1f, 1.80f),
        SeedProduct("Uva", 2, 1f, 2.50f),
        SeedProduct("Brócoli", 2, 1f, 1.50f),
        SeedProduct("Cebolla", 2, 1f, 1f),

        // CHARCUTERÍA (ID: 3)
        SeedProduct("Taquitos de jamón", 3, 2f, 2.99f),
        SeedProduct("Taquitos de chorizo", 3, 2f, 2.15f),
        SeedProduct("Huevos", 3, 1f, 2.50f),

        // DESPENSA - GALLETAS (ID: 5)
        SeedProduct("Galletas María", 5, 1f, 2.50f), // Promedio 2-3€

        // DESPENSA - AZUCAR Y CAFÉ (ID: 7)
        SeedProduct("Sal fina", 7, 1f, 0.70f),
        SeedProduct("Azúcar", 7, 1f, 1.30f),

        // DESPENSA - TOMATE Y LEGUMBRES (ID: 8)
        SeedProduct("Tomate de la abuela", 8, 1f, 1.10f),

        // DESPENSA - ACEITE Y PASTAS (ID: 9)
        SeedProduct("Fideuá", 9, 1f, 1.20f),
        SeedProduct("Starlux", 9, 1f, 1.50f),

        // BEBIDAS (ID: 12)
        SeedProduct("Zumo", 12, 1f, 1.75f),
        SeedProduct("Gaseosas", 12, 1f, 1.50f),
        SeedProduct("Batidos", 12, 1f, 2f),

        // LÁCTEOS (ID: 15)
        SeedProduct("Leche", 15, 6f, 1.15f),

        // PREPARADOS (ID: 16)
        SeedProduct("Capuchinos", 16, 5f, 2.50f), // Promedio 2-3€
        SeedProduct("Pizza", 16, 1f, 3.50f), // Promedio 3-4€

        // QUESOS (ID: 17)
        SeedProduct("Queso fresco", 17, 2f, 2.30f),
        SeedProduct("Queso rallado", 17, 2f, 2.00f),

        // REGALO (ID: 18)
        SeedProduct("Queso (fidelización)", 18, 1f, 0f)
    )
    private val articulosBase = listOf(
        SeedArticulo("Leche Entera Carrefour", 15, 1.15f, 1f, "L", "8410100010015",
            "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100010015_01.jpg"), // Ejemplo real Carrefour

        SeedArticulo("Huevos Docena L", 3, 2.50f, 12f, "ud", null,
            "https://images.pexels.com/photos/162712/egg-white-food-simple-162712.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Huevos

        SeedArticulo("Pan de Molde Blanco", 14, 1.80f, 1f, "paquete", null,
            "https://images.pexels.com/photos/1586942/pexels-photo-1586942.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Pan molde

        SeedArticulo("Arroz Redondo 1kg", 9, 1.30f, 1f, "kg", "8410231234567",
            "https://images.pexels.com/photos/4187621/pexels-photo-4187621.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Arroz

        SeedArticulo("Pasta Fideuá n2", 9, 1.20f, 500f, "g", null,
            "https://images.pexels.com/photos/5692131/pexels-photo-5692131.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Pasta fideos

        SeedArticulo("Tomate Frito Receta Artesana", 8, 2.10f, 1f, "ud", null,
            "https://jcarrefour.vtexassets.com/arquivos/ids/135932/8410022200235_01.jpg"), // Tomate frito real

        SeedArticulo("Aceite de Oliva Virgen Extra", 9, 9.50f, 1f, "L", "8423456789012",
            "https://images.pexels.com/photos/33783/olive-oil-salad-dressing-cooking-oil.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Aceite oliva

        SeedArticulo("Detergente Gel Activo", 1, 6.99f, 3f, "L", null,
            "https://via.placeholder.com/300/4F46E5/FFFFFF?text=Detergente"), // Placeholder azul

        SeedArticulo("Papel Higiénico 12 rollos", 1, 4.50f, 1f, "paquete", null,
            "https://via.placeholder.com/300/F9FAFB/333333?text=Papel+Higienico"), // Placeholder gris

        SeedArticulo("Pechuga de Pollo Bandeja", 3, 5.50f, 1f, "kg", null,
            "https://images.pexels.com/photos/618775/pexels-photo-618775.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Pollo

        SeedArticulo("Manzanas Golden", 2, 1.95f, 1f, "kg", null,
            "https://images.pexels.com/photos/102104/pexels-photo-102104.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Manzanas

        SeedArticulo("Plátano de Canarias", 2, 2.10f, 1f, "kg", null,
            "https://images.pexels.com/photos/2872767/pexels-photo-2872767.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"), // Plátanos

        SeedArticulo("Queso Rallado Emmental", 17, 1.85f, 200f, "g", null,
            "https://via.placeholder.com/300/FEF3C7/92400E?text=Queso+Rallado"), // Placeholder queso

        SeedArticulo("Yogur Natural Pack 8", 15, 1.40f, 1f, "paquete", null,
            "https://via.placeholder.com/300/FFFFFF/4F46E5?text=Yogur+Natural"), // Placeholder yogur

        SeedArticulo("Cerveza Premium Pack 6", 12, 3.90f, 6f, "ud", "8412345678901",
            "https://images.pexels.com/photos/1552630/pexels-photo-1552630.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1") // Cervezas
    )
    /**
     * Semilla el historial de productos si está vacío
     */
    suspend fun seedIfNeeded(repository: ShoppingListRepository) {
        val existingHistory = repository.getFrequentProducts()
        // Solo sembrar si el historial está vacío (primera instalación)
        if (existingHistory.isEmpty()) {
            carrefourProducts.forEach { product ->
                repository.saveToHistory(
                    name = product.name,
                    aisleId = product.aisleId,
                    quantity = product.quantity,
                    price = product.price
                )
            }
        }
    }

    /**
     * Mapea e inserta los artículos si el catálogo está vacío
     */
    suspend fun seedCatalogIfNeeded(articuloDao: ArticuloDao) {
        if (articuloDao.getArticulosCount() == 0) {
            articulosBase.forEach { seed ->
                val entity = ArticuloEntity(
                    name = seed.name,
                    categoryId = seed.categoryId,
                    basePrice = seed.finalPrice, // Mapeamos finalPrice a basePrice de la Entity
                    size = seed.size,
                    unit = seed.unit,
                    ean = seed.ean,
                    photoUri = seed.photoUri
                )
                articuloDao.insertArticulo(entity)
            }
        }
    }
    /**
     * Devuelve la lista transformada a objetos de dominio 'Articulo'
     */
    fun getInitialItems(): List<Articulo> {
        return articulosBase.map { seed ->
            Articulo(
                name = seed.name,
                categoryId = seed.categoryId,
                finalPrice = seed.finalPrice,
                size = seed.size,
                unit = seed.unit,
                ean = seed.ean,
                photoUri = seed.photoUri
            )
        }
    }

}