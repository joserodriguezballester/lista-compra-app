package com.jose.listacompra.domain.usecase.data

import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.ArticuloDao
import com.jose.listacompra.data.local.dao.ArticuloSupermarketDefaultDao
import com.jose.listacompra.data.local.dao.CategorySupermarketOrderDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import javax.inject.Inject

/**
 * UseCase: Resetear datos a estado de producción
 * 
 * Mantiene:
 * - Supermercados (semilla)
 * - Categorías (semilla)
 * - Pasillos por defecto (Carrefour)
 * - Ofertas por defecto
 * 
 * Elimina:
 * - Artículos creados por usuario
 * - Productos en listas
 * - Listas de compra
 * - Historial de precios
 * - Frecuencia de productos
 * - Historial de compras
 * - Preferencias por supermercado
 */
class ResetDataToProductionUseCase @Inject constructor(
    private val articuloDao: ArticuloDao,
    private val shoppingListDao: ShoppingListDao,
    private val productDao: ProductDao,
    private val productHistoryDao: ProductHistoryDao,
    private val productPriceHistoryDao: ProductPriceHistoryDao,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val productFrequencyDao: ProductFrequencyDao,
    private val articuloSupermarketDefaultDao: ArticuloSupermarketDefaultDao,
    private val categorySupermarketOrderDao: CategorySupermarketOrderDao,
    private val aisleDao: AisleDao,
    private val offerDao: OfferDao
) {
    suspend operator fun invoke() {
        // Eliminar datos de usuario
        articuloDao.deleteAll()
        shoppingListDao.deleteAll()
        productDao.deleteAll()
        productHistoryDao.deleteAll()
        productPriceHistoryDao.deleteAll()
        purchaseHistoryDao.deleteAll()
        productFrequencyDao.deleteAll()
        articuloSupermarketDefaultDao.deleteAll()
        categorySupermarketOrderDao.deleteAll()
        
        // Eliminar pasillos y ofertas no por defecto
        aisleDao.deleteCustomAisles()
        offerDao.deleteCustomOffers()
    }
}
