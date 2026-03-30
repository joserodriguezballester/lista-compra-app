package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.repository.ICategoryRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Carga datos iniciales de la lista de Carrefour de Jose
 * Se ejecuta la primera vez que se abre la app
 * 
 * Usa Repositorios (Clean Architecture) en lugar de DAOs directamente
 */
@Singleton
class InitialDataSeeder @Inject constructor(
    private val categoryRepository: ICategoryRepository,
    private val supermarketRepository: ISupermarketRepository,
    private val aisleRepository: IAisleRepository,
    private val articuloRepository: IArticuloRepository
) {

    /**
     * Puebla todas las tablas en el orden correcto (padres antes que hijos)
     */
    suspend fun seedAll(shoppingListRepository: ShoppingListRepository? = null) {
        // ORDEN CRÍTICO: Primero los padres, luego los hijos
        seedSupermarketsIfNeeded()
        seedCategoriesIfNeeded()
        seedAislesIfNeeded()
        seedCatalogIfNeeded()
        
        // Historial de productos (opcional)
        shoppingListRepository?.let { seedHistoryIfNeeded(it) }
    }

    /**
     * Inserta supermercados por defecto si la tabla está vacía
     */
    suspend fun seedSupermarketsIfNeeded() {
        if (supermarketRepository.getDefaultSupermarket() == null) {
            val supermarkets = listOf(
                Supermarket(1, "Carrefour La Alberca", "🛒", isDefault = true),
                Supermarket(2, "Mercadona Mislata", "🟢"),
                Supermarket(3, "Lidl", "🔵"),
                Supermarket(4, "Aldi", "🟡"),
                Supermarket(5, "Consum", "🟠")
            )
            supermarketRepository.insertAll(supermarkets)
        }
    }

    /**
     * Inserta categorías por defecto si la tabla está vacía
     */
    suspend fun seedCategoriesIfNeeded() {
        if (categoryRepository.getCategoryById(1) == null) {
            val categories = listOf(
                Category(1, "Higiene y Belleza", "🧴"),
                Category(2, "Frutas y Verduras", "🍎"),
                Category(3, "Carnicería y Charcutería", "🥩"),
                Category(4, "Lácteos", "🥛"),
                Category(5, "Despensa - Galletas", "🥫"),
                Category(6, "Despensa - Chocolates", "🍫"),
                Category(7, "Despensa - Azúcar y Café", "☕"),
                Category(8, "Despensa - Tomate y Legumbres", "🥫"),
                Category(9, "Despensa - Aceite y Pastas", "🍝"),
                Category(10, "Papel", "🧻"),
                Category(11, "Droguería y Limpieza", "🧼"),
                Category(12, "Bebidas", "🥤"),
                Category(13, "Papas y Snacks", "🥜"),
                Category(14, "Bollería y Panadería", "🥐"),
                Category(15, "Lácteos y Huevos", "🥛"),
                Category(16, "Preparados", "🥪"),
                Category(17, "Quesos", "🧀"),
                Category(18, "Regalo", "🎁"),
                Category(19, "Congelados", "🧊")
            )
            categoryRepository.insertAll(categories)
        }
    }

    /**
     * Inserta pasillos por defecto para cada supermercado
     */
    suspend fun seedAislesIfNeeded() {
        if (aisleRepository.getAllAisles().isEmpty()) {
            // Pasillos de Carrefour (supermercado por defecto)
            val carrefourAisles = Aisle.getDefaultAislesForCarrefour()
            aisleRepository.insertAll(carrefourAisles)
            
            // Pasillos genéricos para otros supermercados
            val genericAisles = getGenericAisles()
            aisleRepository.insertAll(genericAisles)
        }
    }

    /**
     * Inserta artículos del catálogo si está vacío
     */
    suspend fun seedCatalogIfNeeded() {
        if (articuloRepository.getArticulosCount() == 0) {
            val articulos = articulosBase.map { seed ->
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
            articuloRepository.saveAll(articulos)
        }
    }

    /**
     * Semilla el historial de productos si está vacío
     */
    suspend fun seedHistoryIfNeeded(repository: ShoppingListRepository) {
        val existingHistory = repository.getFrequentProducts()
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

    /**
     * Pasillos genéricos para otros supermercados (Mercadona, Lidl, Aldi, Consum)
     */
    private fun getGenericAisles(): List<Aisle> = listOf(
        // Mercadona (supermarketId = 2)
        Aisle(100, "Frutas y Verduras", "🍎", 0, 2),
        Aisle(101, "Panadería", "🍞", 1, 2),
        Aisle(102, "Carnicería", "🥩", 2, 2),
        Aisle(103, "Pescadería", "🐟", 3, 2),
        Aisle(104, "Lácteos", "🥛", 4, 2),
        Aisle(105, "Despensa", "🥫", 5, 2),
        Aisle(106, "Bebidas", "🥤", 6, 2),
        Aisle(107, "Limpieza", "🧼", 7, 2),
        Aisle(108, "Higiene", "🧴", 8, 2),
        Aisle(109, "Congelados", "🧊", 9, 2),
        
        // Lidl (supermarketId = 3)
        Aisle(200, "Frutas y Verduras", "🍎", 0, 3),
        Aisle(201, "Panadería", "🍞", 1, 3),
        Aisle(202, "Lácteos", "🥛", 2, 3),
        Aisle(203, "Despensa", "🥫", 3, 3),
        Aisle(204, "Bebidas", "🥤", 4, 3),
        Aisle(205, "Limpieza", "🧼", 5, 3),
        
        // Aldi (supermarketId = 4)
        Aisle(300, "Frutas y Verduras", "🍎", 0, 4),
        Aisle(301, "Panadería", "🍞", 1, 4),
        Aisle(302, "Lácteos", "🥛", 2, 4),
        Aisle(303, "Despensa", "🥫", 3, 4),
        Aisle(304, "Bebidas", "🥤", 4, 4),
        Aisle(305, "Limpieza", "🧼", 5, 4),
        
        // Consum (supermarketId = 5)
        Aisle(400, "Frutas y Verduras", "🍎", 0, 5),
        Aisle(401, "Panadería", "🍞", 1, 5),
        Aisle(402, "Carnicería", "🥩", 2, 5),
        Aisle(403, "Lácteos", "🥛", 3, 5),
        Aisle(404, "Despensa", "🥫", 4, 5),
        Aisle(405, "Bebidas", "🥤", 5, 5),
        Aisle(406, "Limpieza", "🧼", 6, 5)
    )
}
