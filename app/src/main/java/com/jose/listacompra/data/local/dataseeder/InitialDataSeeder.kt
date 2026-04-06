package com.jose.listacompra.data.local.dataseeder

import android.util.Log
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.repository.ICategoryRepository
import com.jose.listacompra.domain.repository.IHistoryRepository
import com.jose.listacompra.domain.repository.IOfferRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.repository.IShoppingListRepository
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
    private val articuloRepository: IArticuloRepository,
    private val shoppingListRepository: IShoppingListRepository,
    private val productRepository: IProductRepository,
    private val offerRepository: IOfferRepository
) {

    companion object {
        private const val TAG = "InitialDataSeeder"
    }

    /**
     * Puebla todas las tablas en el orden correcto (padres antes que hijos)
     */
    suspend fun seedAll() {
        Log.d(TAG, "Starting seedAll...")
        
        // ORDEN CRÍTICO: Primero los padres, luego los hijos
        seedSupermarketsIfNeeded()
        seedCategoriesIfNeeded()
        seedOffersIfNeeded()
        seedAislesIfNeeded()
        seedShoppingListIfNeeded()
        seedCatalogIfNeeded()
        seedProductsIfNeeded()
        
        Log.d(TAG, "seedAll completed!")
    }

    /**
     * Inserta supermercados por defecto si la tabla está vacía
     */
    suspend fun seedSupermarketsIfNeeded() {
        if (supermarketRepository.getDefaultSupermarket() == null) {
            Log.d(TAG, "Seeding supermarkets...")
            val supermarkets = defaultSupermarkets.map { it.toDomain() }
            supermarketRepository.insertAll(supermarkets)
            Log.d(TAG, "Inserted ${supermarkets.size} supermarkets")
        }
    }

    /**
     * Inserta categorías por defecto si la tabla está vacía
     */
    suspend fun seedCategoriesIfNeeded() {
        if (categoryRepository.getCategoryById(1) == null) {
            Log.d(TAG, "Seeding categories...")
            val categoryList = categories.map { it.toDomain() }
            categoryRepository.insertAll(categoryList)
            Log.d(TAG, "Inserted ${categoryList.size} categories")
        }
    }

    /**
     * Inserta ofertas predefinidas si la tabla está vacía
     */
    suspend fun seedOffersIfNeeded() {
        if (offerRepository.getOfferCount() == 0) {
            Log.d(TAG, "Seeding default offers...")
            val defaultOffers = listOf(
                Offer(1, "3x2", "3x2", "Compra 3 y paga 2", true, "price * 2 / 3"),
                Offer(2, "2x1", "2x1", "Compra 2 y paga 1", true, "price / 2"),
                Offer(3, "2nd_50", "2ª -50%", "Segunda unidad al 50%", true, "price * 1.5"),
                Offer(4, "2nd_70", "2ª -70%", "Segunda unidad al 30%", true, "price * 1.3"),
                Offer(5, "4x3", "4x3", "Compra 4 y paga 3", true, "price * 3 / 4")
            )
            offerRepository.insertAll(defaultOffers)
            Log.d(TAG, "Inserted ${defaultOffers.size} default offers")
        }
    }

    /**
     * Inserta pasillos por defecto para cada supermercado
     */
    suspend fun seedAislesIfNeeded() {
        if (aisleRepository.getAllAisles().isEmpty()) {
            Log.d(TAG, "Seeding aisles...")
            
            // Pasillos de Carrefour (supermercado por defecto)
            val carrefourAisles = Aisle.getDefaultAisles()
            aisleRepository.insertAll(carrefourAisles)
            Log.d(TAG, "Inserted ${carrefourAisles.size} Carrefour aisles")
            
            // Pasillos genéricos para otros supermercados
            aisleRepository.insertAll(genericAisles)
            Log.d(TAG, "Inserted ${genericAisles.size} generic aisles")
        }
    }

    /**
     * Crea lista de la compra por defecto si no existe
     */
    suspend fun seedShoppingListIfNeeded() {
        val existingLists = shoppingListRepository.getAllLists()
        if (existingLists.isEmpty()) {
            Log.d(TAG, "Seeding default shopping list...")
            val listId = shoppingListRepository.createList(
                name = "Mi Lista",
                useDefaultAisles = true
            )
            Log.d(TAG, "Created default list with id: $listId")
        }
    }

    /**
     * Inserta artículos del catálogo si está vacío
     */
    suspend fun seedCatalogIfNeeded() {
        if (articuloRepository.getArticulosCount() == 0) {
            Log.d(TAG, "Seeding articulos...")
            val articulos = articulosBase.map { it.toArticulo() }
            articuloRepository.saveAll(articulos)
            Log.d(TAG, "Inserted ${articulos.size} articulos")
        }
    }

    /**
     * Inserta productos de ejemplo en la lista de la compra
     */
    suspend fun seedProductsIfNeeded() {
        val defaultList = shoppingListRepository.getDefaultList()
        if (defaultList != null) {
            val existingProducts = productRepository.getProductsByList(defaultList.id)
            if (existingProducts.isEmpty()) {
                Log.d(TAG, "Seeding initial products...")
                
                val defaultSupermarket = supermarketRepository.getDefaultSupermarket()
                val supermarketId = defaultSupermarket?.id ?: 1L
                
                val products = initialProducts.map { seedProduct ->
                    com.jose.listacompra.domain.model.Product(
                        name = seedProduct.name,
                        aisleId = seedProduct.aisleId,
                        shoppingListId = defaultList.id,
                        supermarketId = supermarketId,
                        quantity = seedProduct.quantity,
                        estimatedPrice = seedProduct.price,
                        finalPrice = seedProduct.price,
                        notes = seedProduct.notes ?: "",
                        offerId = seedProduct.offerId,
                        isPurchased = false
                    )
                }
                
                products.forEach { product ->
                    productRepository.insertProduct(product)
                }
                
                Log.d(TAG, "Inserted ${products.size} products to default list")
            }
        }
    }

    /**
     * Devuelve la lista transformada a objetos de dominio 'Articulo'
     */
    fun getInitialItems() = articulosBase.map { it.toArticulo() }
    
    /**
     * Inserta datos de historial de ejemplo si la tabla está vacía
     */
    private suspend fun seedHistoryIfNeeded() {
        val existingData = historyRepository.getAllFrequencies()
        if (existingData.isEmpty()) {
            Log.d(TAG, "Seeding history data...")
            
            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L // 1 día en ms
            
            // Datos de frecuencia de productos
            val frequencyData = listOf(
                com.jose.listacompra.data.local.entities.ProductFrequencyEntity(
                    productName = "leche entera",
                    originalName = "Leche entera",
                    timesPurchased = 8,
                    lastPurchaseDate = now - (2 * day),
                    averageDaysBetween = 3.5f,
                    estimatedNextDate = now + (1.5f * day).toLong(),
                    lastQuantity = 2f,
                    lastPrice = 1.35f,
                    lastSupermarketId = 2 // Mercadona
                ),
                com.jose.listacompra.data.local.entities.ProductFrequencyEntity(
                    productName = "pan de molde",
                    originalName = "Pan de molde",
                    timesPurchased = 5,
                    lastPurchaseDate = now - (5 * day),
                    averageDaysBetween = 4f,
                    estimatedNextDate = now - (1 * day), // ¡Ya debería estar!
                    lastQuantity = 1f,
                    lastPrice = 1.50f,
                    lastSupermarketId = 1 // Carrefour
                ),
                com.jose.listacompra.data.local.entities.ProductFrequencyEntity(
                    productName = "huevos",
                    originalName = "Huevos",
                    timesPurchased = 6,
                    lastPurchaseDate = now - (3 * day),
                    averageDaysBetween = 5f,
                    estimatedNextDate = now + (2 * day),
                    lastQuantity = 12f,
                    lastPrice = 2.10f,
                    lastSupermarketId = 2 // Mercadona
                ),
                com.jose.listacompra.data.local.entities.ProductFrequencyEntity(
                    productName = "yogur natural",
                    originalName = "Yogur natural",
                    timesPurchased = 4,
                    lastPurchaseDate = now - (7 * day),
                    averageDaysBetween = 7f,
                    estimatedNextDate = now,
                    lastQuantity = 1f,
                    lastPrice = 1.80f,
                    lastSupermarketId = 1 // Carrefour
                ),
                com.jose.listacompra.data.local.entities.ProductFrequencyEntity(
                    productName = "café molido",
                    originalName = "Café molido",
                    timesPurchased = 3,
                    lastPurchaseDate = now - (14 * day),
                    averageDaysBetween = 14f,
                    estimatedNextDate = now,
                    lastQuantity = 1f,
                    lastPrice = 3.50f,
                    lastSupermarketId = 3 // Lidl
                ),
                com.jose.listacompra.data.local.entities.ProductFrequencyEntity(
                    productName = "aceite de oliva",
                    originalName = "Aceite de oliva",
                    timesPurchased = 2,
                    lastPurchaseDate = now - (30 * day),
                    averageDaysBetween = 30f,
                    estimatedNextDate = now,
                    lastQuantity = 1f,
                    lastPrice = 5.50f,
                    lastSupermarketId = 1 // Carrefour
                )
            )
            
            // Historial de precios
            val priceHistoryData = listOf(
                // Leche entera - variación de precios
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "leche entera",
                    price = 1.25f,
                    quantity = 2,
                    fecha = now - (21 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "leche entera",
                    price = 1.30f,
                    quantity = 2,
                    fecha = now - (17 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "leche entera",
                    price = 1.35f,
                    quantity = 2,
                    fecha = now - (14 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "leche entera",
                    price = 1.28f,
                    quantity = 2,
                    fecha = now - (10 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "leche entera",
                    price = 1.35f,
                    quantity = 2,
                    fecha = now - (7 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "leche entera",
                    price = 1.32f,
                    quantity = 2,
                    fecha = now - (2 * day)
                ),
                
                // Pan de molde
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "pan de molde",
                    price = 1.45f,
                    quantity = 1,
                    fecha = now - (25 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "pan de molde",
                    price = 1.50f,
                    quantity = 1,
                    fecha = now - (20 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "pan de molde",
                    price = 1.48f,
                    quantity = 1,
                    fecha = now - (12 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "pan de molde",
                    price = 1.50f,
                    quantity = 1,
                    fecha = now - (5 * day)
                ),
                
                // Huevos
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "huevos",
                    price = 2.00f,
                    quantity = 12,
                    fecha = now - (18 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "huevos",
                    price = 2.10f,
                    quantity = 12,
                    fecha = now - (10 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "huevos",
                    price = 2.05f,
                    quantity = 12,
                    fecha = now - (3 * day)
                ),
                
                // Yogur
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "yogur natural",
                    price = 1.75f,
                    quantity = 1,
                    fecha = now - (21 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "yogur natural",
                    price = 1.80f,
                    quantity = 1,
                    fecha = now - (14 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "yogur natural",
                    price = 1.85f,
                    quantity = 1,
                    fecha = now - (7 * day)
                ),
                
                // Café
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "café molido",
                    price = 3.20f,
                    quantity = 1,
                    fecha = now - (45 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "café molido",
                    price = 3.50f,
                    quantity = 1,
                    fecha = now - (28 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "café molido",
                    price = 3.40f,
                    quantity = 1,
                    fecha = now - (14 * day)
                ),
                
                // Aceite
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "aceite de oliva",
                    price = 5.00f,
                    quantity = 1,
                    fecha = now - (60 * day)
                ),
                com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity(
                    productName = "aceite de oliva",
                    price = 5.50f,
                    quantity = 1,
                    fecha = now - (30 * day)
                )
            )
            
            // Insertar datos
            frequencyData.forEach { historyRepository.insertFrequency(it) }
            priceHistoryData.forEach { historyRepository.savePriceHistory(it) }
            
            Log.d(TAG, "Inserted ${frequencyData.size} frequency records and ${priceHistoryData.size} price history records")
        }
    }
}
