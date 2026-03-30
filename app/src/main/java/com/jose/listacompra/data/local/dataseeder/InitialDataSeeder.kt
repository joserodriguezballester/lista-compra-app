package com.jose.listacompra.data.local.dataseeder

import android.util.Log
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.repository.ICategoryRepository
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
    private val shoppingListRepository: IShoppingListRepository
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
        seedAislesIfNeeded()
        seedShoppingListIfNeeded()
        seedCatalogIfNeeded()
        
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
     * Devuelve la lista transformada a objetos de dominio 'Articulo'
     */
    fun getInitialItems() = articulosBase.map { it.toArticulo() }
}
