package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.data.local.dao.ArticuloDao
import com.jose.listacompra.data.local.dao.CategoryDao
import com.jose.listacompra.data.local.dao.SupermarketDao
import com.jose.listacompra.data.local.entities.ArticuloEntity
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Articulo

/**
 * Carga datos iniciales de la lista de Carrefour de Jose
 * Se ejecuta la primera vez que se abre la app
 */
object InitialDataSeeder {

//    /**
//     * Semilla el historial de productos si está vacío
//     */
//    suspend fun seedIfNeeded(repository: ShoppingListRepository) {
//        val existingHistory = repository.getFrequentProducts()
//        if (existingHistory.isEmpty()) {
//            carrefourProducts.forEach { product ->
//                repository.saveToHistory(
//                    name = product.name,
//                    aisleId = product.aisleId,
//                    quantity = product.quantity,
//                    price = product.price
//                )
//            }
//        }
//    }
//
//    /**
//     * Mapea e inserta los artículos si el catálogo está vacío
//     */
//    suspend fun seedCatalogIfNeeded(articuloDao: ArticuloDao) {
//        if (articuloDao.getArticulosCount() == 0) {
//            articulosBase.forEach { seed ->
//                val entity = ArticuloEntity(
//                    name = seed.name,
//                    categoryId = seed.categoryId,
//                    basePrice = seed.finalPrice,
//                    size = seed.size,
//                    unit = seed.unit,
//                    ean = seed.ean,
//                    photoUri = seed.photoUri
//                )
//                articuloDao.insertArticulo(entity)
//            }
//        }
//    }
//
//    /**
//     * Inserta supermercados por defecto si la tabla está vacía
//     */
//    suspend fun seedSupermarketsIfNeeded(supermarketDao: SupermarketDao) {
//        if (supermarketDao.getDefaultSupermarket() == null) {
//            supermarketDao.insertAll(supermarkets)
//        }
//    }
//
//    /**
//     * Inserta categorías por defecto si la tabla está vacía
//     */
//    suspend fun seedCategoriesIfNeeded(categoryDao: CategoryDao) {
//        if (categoryDao.getCategoryById(1) == null) {
//            categoryDao.insertAll(categories)
//        }
//    }
//
//    /**
//     * Devuelve la lista transformada a objetos de dominio 'Articulo'
//     */
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

    // --- FUNCIONES DE CARGA (LOGIC) ---

    suspend fun seedAll(
        categoryDao: CategoryDao,
        supermarketDao: SupermarketDao,
        articuloDao: ArticuloDao,
        repository: ShoppingListRepository
    ) {
        // ORDEN CRÍTICO: Primero los padres, luego los hijos
        seedCategoriesIfNeeded(categoryDao)
        seedSupermarketsIfNeeded(supermarketDao)
        seedCatalogIfNeeded(articuloDao)
        seedIfNeeded(repository)
    }

    suspend fun seedCategoriesIfNeeded(categoryDao: CategoryDao) {
        if (categoryDao.getCategoryById(1) == null) {
            categoryDao.insertAll(categories)
        }
    }

    suspend fun seedSupermarketsIfNeeded(supermarketDao: SupermarketDao) {
        if (supermarketDao.getDefaultSupermarket() == null) {
            supermarketDao.insertAll(supermarkets)
        }
    }

    suspend fun seedCatalogIfNeeded(articuloDao: ArticuloDao) {
        if (articuloDao.getArticulosCount() == 0) {
            articulosBase.forEach { seed ->
                articuloDao.insertArticulo(
                    ArticuloEntity(
                        name = seed.name,
                        categoryId = seed.categoryId,
                        basePrice = seed.finalPrice,
                        size = seed.size,
                        unit = seed.unit,
                        ean = seed.ean,
                        photoUri = seed.photoUri
                    )
                )
            }
        }
    }

    suspend fun seedIfNeeded(repository: ShoppingListRepository) {
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
}
