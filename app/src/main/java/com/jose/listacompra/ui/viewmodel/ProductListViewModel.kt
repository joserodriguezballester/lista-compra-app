package com.jose.listacompra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.R
import com.jose.listacompra.data.repository.ThemeRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import com.jose.listacompra.domain.repository.ICategoryRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.usecase.list.GetDefaultListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: IProductRepository,
    private val supermarketRepository: ISupermarketRepository,
    private val aisleRepository: IAisleRepository,
    private val categoryRepository: ICategoryRepository,
    private val themeRepository: ThemeRepository,
    private val getDefaultListUseCase: GetDefaultListUseCase
) : ViewModel() {

    private val TAG = "ProductListViewModel"

    // ID de la lista actual (obtenido dinámicamente)
    private var currentListId: Long = 0
    
    // Supermercado seleccionado
    private val _selectedSupermarketId = MutableStateFlow<Long?>(null)
    val selectedSupermarketId: StateFlow<Long?> = _selectedSupermarketId.asStateFlow()
    
    // Supermercados disponibles
    private val _supermarkets = MutableStateFlow<List<Supermarket>>(emptyList())
    val supermarkets: StateFlow<List<Supermarket>> = _supermarkets.asStateFlow()
    
    // Pasillos del supermercado seleccionado
    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()
    
    // Categorías
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    // Productos agrupados por pasillo
    private val _productsByAisle = MutableStateFlow<Map<Aisle, List<Product>>>(emptyMap())
    val productsByAisle: StateFlow<Map<Aisle, List<Product>>> = _productsByAisle.asStateFlow()
    
    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Totales
    private val _totalPrice = MutableStateFlow(0f)
    val totalPrice: StateFlow<Float> = _totalPrice.asStateFlow()
    
    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems.asStateFlow()
    
    private val _purchasedItems = MutableStateFlow(0)
    val purchasedItems: StateFlow<Int> = _purchasedItems.asStateFlow()

    // Tema
    val isDarkTheme: StateFlow<Boolean> = themeRepository.isDarkTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    init {
        viewModelScope.launch {
            // Obtener/crear ID de lista por defecto
            currentListId = getDefaultListUseCase()
            Log.d(TAG, "Using list id: $currentListId")
            
            // Cargar datos iniciales
            loadSupermarkets()
            loadCategories()
            
            // Seleccionar supermercado por defecto
            val defaultSupermarket = _supermarkets.value.find { it.isDefault }
            if (defaultSupermarket != null) {
                selectSupermarket(defaultSupermarket.id)
            }
        }
    }

    fun selectSupermarket(supermarketId: Long) {
        viewModelScope.launch {
            _selectedSupermarketId.value = supermarketId
            loadAisles(supermarketId)
            loadProducts()
        }
    }

    private suspend fun loadSupermarkets() {
        try {
            val supermarketList = supermarketRepository.getAllSupermarkets()
            _supermarkets.value = supermarketList
        } catch (e: Exception) {
            Log.e(TAG, "Error loading supermarkets", e)
            _error.value = "Error al cargar supermercados"
        }
    }

    private suspend fun loadCategories() {
        try {
            val categoryList = categoryRepository.getAllCategories()
            _categories.value = categoryList
        } catch (e: Exception) {
            Log.e(TAG, "Error loading categories", e)
        }
    }

    private suspend fun loadAisles(supermarketId: Long) {
        try {
            val aisleList = aisleRepository.getAislesBySupermarket(supermarketId)
            _aisles.value = aisleList
        } catch (e: Exception) {
            Log.e(TAG, "Error loading aisles", e)
            _error.value = "Error al cargar pasillos"
        }
    }

    private suspend fun loadProducts() {
        if (currentListId == 0L) {
            Log.w(TAG, "currentListId is 0, skipping loadProducts")
            return
        }
        
        try {
            _isLoading.value = true
            val supermarketId = _selectedSupermarketId.value
            
            val products = if (supermarketId != null) {
                productRepository.getProductsBySupermarketFlow(currentListId, supermarketId)
            } else {
                productRepository.getProductsByListFlow(currentListId)
            }
            
            products.collect { productList ->
                groupProductsByAisle(productList)
                calculateTotals(productList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading products", e)
            _error.value = "Error al cargar productos"
        } finally {
            _isLoading.value = false
        }
    }

    private fun groupProductsByAisle(products: List<Product>) {
        val grouped = mutableMapOf<Aisle, List<Product>>()
        val unknownAisle = Aisle(id = -1, name = "Sin pasillo", icon = "📦", orderIndex = 999, supermarketId = 0)
        
        for (product in products) {
            val aisle = _aisles.value.find { it.id == product.aisleId } ?: unknownAisle
            val existingList = grouped[aisle] ?: emptyList()
            grouped[aisle] = existingList + product
        }
        
        // Ordenar por índice de pasillo
        val sortedGrouped = grouped.toSortedMap(compareBy { it.orderIndex })
        _productsByAisle.value = sortedGrouped
    }

    private fun calculateTotals(products: List<Product>) {
        _totalItems.value = products.size
        _purchasedItems.value = products.count { it.isPurchased }
        _totalPrice.value = products.sumOf { it.finalPrice?.toDouble() ?: 0.0 }.toFloat()
    }

    fun toggleProductPurchased(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.togglePurchased(product.id, !product.isPurchased)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling product purchased", e)
                _error.value = "Error al actualizar producto"
            }
        }
    }

    fun addProduct(
        name: String,
        quantity: Float = 1f,
        aisleId: Long? = null,
        price: Float? = null
    ) {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot add product: currentListId is 0")
                _error.value = "Error: no hay lista activa"
                return@launch
            }
            
            try {
                val product = Product(
                    shoppingListId = currentListId,
                    name = name,
                    quantity = quantity,
                    aisleId = aisleId ?: _aisles.value.firstOrNull()?.id,
                    supermarketId = _selectedSupermarketId.value ?: 1L,
                    finalPrice = price,
                    isPurchased = false
                )
                productRepository.insertProduct(product)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding product", e)
                _error.value = "Error al añadir producto: ${e.message}"
            }
        }
    }

    fun removeProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing product", e)
                _error.value = "Error al eliminar producto"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun toggleTheme() {
        viewModelScope.launch {
            themeRepository.toggleTheme()
        }
    }

    /**
     * Vacía todos los productos de la lista actual
     */
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
                _error.value = "Error al vaciar la lista"
            }
        }
    }
}
