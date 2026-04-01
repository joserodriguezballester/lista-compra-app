package com.jose.listacompra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.preferences.ThemePreferences
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.repository.ICategoryRepository
import com.jose.listacompra.domain.repository.IOfferRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import com.jose.listacompra.domain.usecase.list.GetDefaultListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val productsByAisle: Map<Aisle, List<Product>> = emptyMap(),
    val totalPrice: Float = 0f,
    val totalItems: Int = 0,
    val purchasedItems: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val supermarkets: List<Supermarket> = emptyList(),
    val selectedSupermarketId: Long? = null,
    val aisles: List<Aisle> = emptyList(),
    val categories: List<Category> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val articleSuggestions: List<Articulo> = emptyList()
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: IProductRepository,
    private val supermarketRepository: ISupermarketRepository,
    private val aisleRepository: IAisleRepository,
    private val categoryRepository: ICategoryRepository,
    private val offerRepository: IOfferRepository,
    private val themePreferences: ThemePreferences,
    private val getDefaultListUseCase: GetDefaultListUseCase,
    private val articuloRepository: IArticuloRepository
) : ViewModel() {

    private val TAG = "ProductListViewModel"

    // ID de la lista actual
    private var currentListId: Long = 0

    // UI State combinado
    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    // Tema
    val isDarkTheme: StateFlow<Boolean> = themePreferences.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    ) as StateFlow<Boolean>

    init {
        viewModelScope.launch {
            // Obtener/crear ID de lista por defecto
            currentListId = getDefaultListUseCase()
            Log.d(TAG, "Using list id: $currentListId")

            // Cargar datos iniciales
            loadSupermarkets()
            loadCategories()
            loadOffers()

            // Seleccionar supermercado por defecto
            val defaultSupermarket = _uiState.value.supermarkets.find { it.isDefault }
            if (defaultSupermarket != null) {
                selectSupermarket(defaultSupermarket.id)
            }
        }
    }

    fun selectSupermarket(supermarketId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSupermarketId = supermarketId) }
            loadAisles(supermarketId)
            loadProducts()
        }
    }

    private suspend fun loadSupermarkets() {
        try {
            supermarketRepository.getAllSupermarkets().collect { supermarketList ->
                _uiState.update { it.copy(supermarkets = supermarketList) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading supermarkets", e)
            _uiState.update { it.copy(error = "Error al cargar supermercados") }
        }
    }

    private suspend fun loadCategories() {
        try {
            categoryRepository.getAllCategories().collect { categoryList ->
                _uiState.update { it.copy(categories = categoryList) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading categories", e)
        }
    }

    private suspend fun loadOffers() {
        try {
            val offerList = offerRepository.getAllOffers()
            _uiState.update { it.copy(offers = offerList) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading offers", e)
        }
    }

    private suspend fun loadAisles(supermarketId: Long) {
        try {
            val aisleList = aisleRepository.getAislesBySupermarket(supermarketId)
            _uiState.update { it.copy(aisles = aisleList) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading aisles", e)
            _uiState.update { it.copy(error = "Error al cargar pasillos") }
        }
    }

    private suspend fun loadProducts() {
        if (currentListId == 0L) {
            Log.w(TAG, "currentListId is 0, skipping loadProducts")
            return
        }

        try {
            _uiState.update { it.copy(isLoading = true) }
            val supermarketId = _uiState.value.selectedSupermarketId

            val products = if (supermarketId != null) {
                productRepository.getProductsBySupermarketFlow(currentListId, supermarketId)
            } else {
                productRepository.getProductsByListFlow(currentListId)
            }

            products.collect { productList ->
                val grouped = groupProductsByAisle(productList)
                val totals = calculateTotals(productList)

                _uiState.update {
                    it.copy(
                        productsByAisle = grouped,
                        totalPrice = totals.first,
                        totalItems = totals.second,
                        purchasedItems = totals.third,
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading products", e)
            _uiState.update { it.copy(error = "Error al cargar productos", isLoading = false) }
        }
    }

    private fun groupProductsByAisle(products: List<Product>): Map<Aisle, List<Product>> {
        val grouped = mutableMapOf<Aisle, List<Product>>()
        val unknownAisle = Aisle(id = -1, name = "Sin pasillo", emoji = "📦", orderIndex = 999, supermarketId = 0)

        for (product in products) {
            val aisle = _uiState.value.aisles.find { it.id == product.aisleId } ?: unknownAisle
            val existingList = grouped[aisle] ?: emptyList()
            grouped[aisle] = existingList + product
        }

        return grouped.toSortedMap(compareBy { it.orderIndex })
    }

    private fun calculateTotals(products: List<Product>): Triple<Float, Int, Int> {
        val totalItems = products.size
        val purchasedItems = products.count { it.isPurchased }
        val totalPrice = products.sumOf { it.finalPrice?.toDouble() ?: 0.0 }.toFloat()
        return Triple(totalPrice, totalItems, purchasedItems)
    }

    fun toggleProductPurchased(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.togglePurchased(product.id, !product.isPurchased)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling product purchased", e)
                _uiState.update { it.copy(error = "Error al actualizar producto") }
            }
        }
    }

    fun addProduct(
        name: String,
        quantity: Float = 1f,
        aisleId: Long? = null,
        price: Float? = null,
        offerId: Long? = null
    ) {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot add product: currentListId is 0")
                _uiState.update { it.copy(error = "Error: no hay lista activa") }
                return@launch
            }

            try {
                val finalPrice = calculateFinalPrice(quantity, price, offerId)
                
                val product = Product(
                    shoppingListId = currentListId,
                    name = name,
                    quantity = quantity,
                    aisleId = aisleId ?: _uiState.value.aisles.firstOrNull()?.id ?: 0L,
                    supermarketId = _uiState.value.selectedSupermarketId ?: 1L,
                    estimatedPrice = price,
                    finalPrice = finalPrice,
                    offerId = offerId,
                    isPurchased = false
                )
                productRepository.insertProduct(product)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding product", e)
                _uiState.update { it.copy(error = "Error al añadir producto: ${e.message}") }
            }
        }
    }

    fun removeProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing product", e)
                _uiState.update { it.copy(error = "Error al eliminar producto") }
            }
        }
    }

    fun clearList() {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot clear list: currentListId is 0")
                return@launch
            }

            try {
                productRepository.deleteAllProductsFromList(currentListId)
                Log.d(TAG, "Cleared all products from list $currentListId")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing list", e)
                _uiState.update { it.copy(error = "Error al vaciar la lista") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            themePreferences.toggleManualTheme()
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                // Recalcular precio final si cambió la oferta o cantidad
                val finalPrice = calculateFinalPrice(product.quantity, product.estimatedPrice, product.offerId)
                val updatedProduct = product.copy(finalPrice = finalPrice)
                
                productRepository.updateProduct(updatedProduct)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating product", e)
                _uiState.update { it.copy(error = "Error al actualizar producto") }
            }
        }
    }

    fun deletePurchasedProducts() {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot delete purchased: currentListId is 0")
                return@launch
            }

            try {
                productRepository.deletePurchasedProducts(currentListId)
                Log.d(TAG, "Deleted purchased products from list $currentListId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting purchased products", e)
                _uiState.update { it.copy(error = "Error al eliminar productos comprados") }
            }
        }
    }

    fun searchArticles(query: String) {
        viewModelScope.launch {
            try {
                val results = articuloRepository.searchArticulos(query)
                _uiState.update { it.copy(articleSuggestions = results) }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching articles", e)
            }
        }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(articleSuggestions = emptyList()) }
    }

    /**
     * Calcula el precio final según la oferta aplicada
     */
    private fun calculateFinalPrice(
        quantity: Float,
        unitPrice: Float?,
        offerId: Long?
    ): Float? {
        if (unitPrice == null) return null
        if (offerId == null) return unitPrice * quantity

        val offer = _uiState.value.offers.find { it.id == offerId } ?: return unitPrice * quantity
        val qty = quantity.toInt()

        return when (offer.code) {
            "3x2" -> {
                val groups = qty / 3
                val remainder = qty % 3
                (groups * 2 + remainder) * unitPrice
            }
            "2x1" -> {
                val groups = qty / 2
                val remainder = qty % 2
                (groups + remainder) * unitPrice
            }
            "2nd_50" -> {
                val pairs = qty / 2
                val remainder = qty % 2
                pairs * (unitPrice * 1.5f) + remainder * unitPrice
            }
            "2nd_70" -> {
                val pairs = qty / 2
                val remainder = qty % 2
                pairs * (unitPrice * 1.3f) + remainder * unitPrice
            }
            "4x3" -> {
                val groups = qty / 4
                val remainder = qty % 4
                (groups * 3 + remainder) * unitPrice
            }
            else -> unitPrice * quantity
        }
    }
}
