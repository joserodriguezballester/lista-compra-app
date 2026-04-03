package com.jose.listacompra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.preferences.ThemePreferences
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.usecase.aisle.GetAislesBySupermarketUseCase
import com.jose.listacompra.domain.usecase.articulo.SearchArticulosUseCase
import com.jose.listacompra.domain.usecase.category.GetAllCategoriesFlowUseCase
import com.jose.listacompra.domain.usecase.history.*
import com.jose.listacompra.domain.usecase.list.GetDefaultListUseCase
import com.jose.listacompra.domain.usecase.offers.CalculatePriceUseCase
import com.jose.listacompra.domain.usecase.offers.GetAllOffersUseCase
import com.jose.listacompra.domain.usecase.product.*
import com.jose.listacompra.domain.usecase.supermarket.GetAllSupermarketsFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    val articleSuggestions: List<Articulo> = emptyList(),
    val collapsedAisles: Set<Long> = emptySet(),
    val historySuggestions: List<ProductFrequencyEntity> = emptyList(),
    val selectedPriceHistory: List<ProductPriceHistoryEntity> = emptyList(),
    val selectedPriceStats: PriceStats? = null
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsByListUseCase: GetProductsByListUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val toggleProductPurchasedUseCase: ToggleProductPurchasedUseCase,
    private val deletePurchasedProductsUseCase: DeletePurchasedProductsUseCase,
    private val getAllSupermarketsFlowUseCase: GetAllSupermarketsFlowUseCase,
    private val getAislesBySupermarketUseCase: GetAislesBySupermarketUseCase,
    private val getAllCategoriesFlowUseCase: GetAllCategoriesFlowUseCase,
    private val getAllOffersUseCase: GetAllOffersUseCase,
    private val searchArticulosUseCase: SearchArticulosUseCase,
    private val getDefaultListUseCase: GetDefaultListUseCase,
    private val calculatePriceUseCase: CalculatePriceUseCase,
    private val getProductHistorySuggestionsUseCase: GetProductHistorySuggestionsUseCase,
    private val updateProductFrequencyUseCase: UpdateProductFrequencyUseCase,
    private val savePriceHistoryUseCase: SavePriceHistoryUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val getPriceStatsUseCase: GetPriceStatsUseCase,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val TAG = "ProductListViewModel"
    private var currentListId: Long = 0

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()
    
    val primaryColor: Flow<Int> = themePreferences.primaryColor
    val isDarkTheme: StateFlow<Boolean> = themePreferences.themeMode.map { it == "dark" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                currentListId = getDefaultListUseCase()
                Log.d(TAG, "Using list id: $currentListId")

                loadSupermarkets()
                loadCategories()
                loadOffers()
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun loadSupermarkets() {
        try {
            getAllSupermarketsFlowUseCase().collect { supermarketList ->
                _uiState.update { 
                    it.copy(
                        supermarkets = supermarketList,
                        selectedSupermarketId = it.selectedSupermarketId ?: supermarketList.firstOrNull()?.id
                    )
                }
                
                supermarketList.firstOrNull()?.id?.let { supermarketId ->
                    loadAisles(supermarketId)
                    loadProductsForSupermarket(supermarketId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading supermarkets", e)
        }
    }

    private suspend fun loadCategories() {
        try {
            getAllCategoriesFlowUseCase().collect { categoryList ->
                _uiState.update { it.copy(categories = categoryList) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading categories", e)
        }
    }

    private suspend fun loadOffers() {
        try {
            val offerList = getAllOffersUseCase()
            _uiState.update { it.copy(offers = offerList) }
            Log.d(TAG, "Loaded ${offerList.size} offers")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading offers", e)
        }
    }

    private suspend fun loadAisles(supermarketId: Long) {
        try {
            val aisleList = getAislesBySupermarketUseCase(supermarketId)
            _uiState.update { it.copy(aisles = aisleList) }
            Log.d(TAG, "Loaded ${aisleList.size} aisles")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading aisles", e)
        }
    }

    private fun loadProductsForSupermarket(supermarketId: Long) {
        viewModelScope.launch {
            try {
                getProductsByListUseCase(currentListId, supermarketId).collect { productList ->
                    val aisleMap = productList.groupBy { product ->
                        _uiState.value.aisles.find { it.id == product.aisleId } 
                            ?: Aisle(id = 0, name = "Sin pasillo", emoji = "📦", supermarketId = 0)
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            productsByAisle = aisleMap,
                            totalItems = productList.size,
                            purchasedItems = productList.count { it.isPurchased },
                            totalPrice = productList.sumOf { (it.finalPrice ?: it.estimatedPrice ?: 0f).toDouble() }.toFloat()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading products", e)
            }
        }
    }

    fun selectSupermarket(supermarketId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSupermarketId = supermarketId) }
            loadAisles(supermarketId)
            loadProductsForSupermarket(supermarketId)
        }
    }

    fun addProduct(name: String, quantity: Float, aisleId: Long?, price: Float?, offerId: Long?, notes: String?, photoUri: String?) {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot add product: currentListId is 0")
                _uiState.update { it.copy(error = "Error: no hay lista activa") }
                return@launch
            }

            try {
                val finalPrice = calculateFinalPrice(quantity, price, offerId)
                val selectedAisleId = aisleId ?: _uiState.value.aisles.firstOrNull()?.id ?: 0L
                val selectedSupermarketId = _uiState.value.selectedSupermarketId ?: 1L
                
                val product = Product(
                    shoppingListId = currentListId,
                    name = name,
                    quantity = quantity,
                    aisleId = selectedAisleId,
                    supermarketId = selectedSupermarketId,
                    estimatedPrice = price,
                    finalPrice = finalPrice,
                    offerId = offerId,
                    notes = notes ?: "",
                    photoUri = photoUri
                )
                
                addProductUseCase(product)
                Log.d(TAG, "Product added: $name")
                
                updateProductFrequencyUseCase(
                    name = name,
                    aisleId = selectedAisleId,
                    quantity = quantity,
                    price = price,
                    supermarketId = selectedSupermarketId
                )
                Log.d(TAG, "History updated: $name -> aisle $selectedAisleId")
                
                if (price != null) {
                    savePriceHistoryUseCase(
                        productName = name,
                        price = price,
                        quantity = quantity.toInt()
                    )
                    Log.d(TAG, "Price history saved: $name -> $price €")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding product", e)
                _uiState.update { it.copy(error = "Error al añadir: ${e.message}") }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                val finalPrice = calculateFinalPrice(
                    product.quantity,
                    product.estimatedPrice,
                    product.offerId
                )
                
                val updated = product.copy(finalPrice = finalPrice)
                updateProductUseCase(updated)
                Log.d(TAG, "Product updated: ${product.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating product", e)
                _uiState.update { it.copy(error = "Error al actualizar: ${e.message}") }
            }
        }
    }

    fun removeProduct(product: Product) {
        viewModelScope.launch {
            try {
                deleteProductUseCase(product)
                Log.d(TAG, "Product removed: ${product.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing product", e)
                _uiState.update { it.copy(error = "Error al eliminar: ${e.message}") }
            }
        }
    }

    fun toggleProductPurchased(product: Product) {
        viewModelScope.launch {
            try {
                toggleProductPurchasedUseCase(product.id, !product.isPurchased)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling purchased", e)
            }
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            try {
                deletePurchasedProductsUseCase(currentListId)
                Log.d(TAG, "Cleared purchased products")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing purchased", e)
            }
        }
    }

    fun searchArticles(query: String) {
        viewModelScope.launch {
            try {
                if (query.length >= 2) {
                    val catalogResults = searchArticulosUseCase(query)
                    val historyResults = getProductHistorySuggestionsUseCase(query)
                    
                    _uiState.update { 
                        it.copy(
                            articleSuggestions = catalogResults,
                            historySuggestions = historyResults
                        )
                    }
                    
                    Log.d(TAG, "Found ${catalogResults.size} catalog + ${historyResults.size} history for '$query'")
                } else {
                    _uiState.update { 
                        it.copy(
                            articleSuggestions = emptyList(),
                            historySuggestions = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching articles", e)
            }
        }
    }

    fun clearSuggestions() {
        _uiState.update { 
            it.copy(
                articleSuggestions = emptyList(),
                historySuggestions = emptyList()
            )
        }
    }

    private fun calculateFinalPrice(quantity: Float, unitPrice: Float?, offerId: Long?): Float? {
        if (unitPrice == null) return null
        
        val offerCode = if (offerId != null && offerId > 0) {
            _uiState.value.offers.find { it.id == offerId }?.code
        } else null
        
        val result = calculatePriceUseCase(quantity, unitPrice, offerCode)
        return result.finalPrice
    }

    fun toggleAisleCollapse(aisleId: Long) {
        _uiState.update { currentState ->
            val newCollapsedAisles = if (aisleId in currentState.collapsedAisles) {
                currentState.collapsedAisles - aisleId
            } else {
                currentState.collapsedAisles + aisleId
            }
            currentState.copy(collapsedAisles = newCollapsedAisles)
        }
    }
    
    fun setPrimaryColor(color: Int) {
        viewModelScope.launch {
            themePreferences.setPrimaryColor(color)
        }
    }
    
    fun loadPriceHistory(productName: String) {
        viewModelScope.launch {
            try {
                val history = getPriceHistoryUseCase(productName)
                val stats = getPriceStatsUseCase(productName)
                
                _uiState.update {
                    it.copy(
                        selectedPriceHistory = history,
                        selectedPriceStats = stats
                    )
                }
                
                Log.d(TAG, "Loaded ${history.size} price records for '$productName'")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading price history", e)
            }
        }
    }
}