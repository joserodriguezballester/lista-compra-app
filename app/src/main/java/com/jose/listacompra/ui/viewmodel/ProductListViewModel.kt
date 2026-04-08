package com.jose.listacompra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.preferences.ThemePreferences
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.usecase.aisle.GetAislesBySupermarketUseCase
import com.jose.listacompra.domain.usecase.articulo.SearchArticulosUseCase
import com.jose.listacompra.domain.usecase.category.GetAllCategoriesFlowUseCase
import com.jose.listacompra.domain.usecase.history.GetPriceHistoryUseCase
import com.jose.listacompra.domain.usecase.history.GetPriceStatsUseCase
import com.jose.listacompra.domain.usecase.history.GetProductHistorySuggestionsUseCase
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import com.jose.listacompra.domain.usecase.history.UpdateProductFrequencyUseCase
import com.jose.listacompra.domain.usecase.list.GetDefaultListUseCase
import com.jose.listacompra.domain.usecase.offers.CalculatePriceUseCase
import com.jose.listacompra.domain.usecase.offers.GetAllOffersUseCase
import com.jose.listacompra.domain.usecase.product.AddProductUseCase
import com.jose.listacompra.domain.usecase.product.DeleteProductUseCase
import com.jose.listacompra.domain.usecase.product.DeletePurchasedProductsUseCase
import com.jose.listacompra.domain.usecase.product.GetProductsByListUseCase
import com.jose.listacompra.domain.usecase.product.ToggleProductPurchasedUseCase
import com.jose.listacompra.domain.usecase.product.UpdateProductUseCase
import com.jose.listacompra.domain.usecase.supermarket.GetAllSupermarketsFlowUseCase
import com.jose.listacompra.ui.state.ProductListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    
    val primaryColor: StateFlow<Int> = themePreferences.primaryColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePreferences.DEFAULT_COLOR)

    val isDarkTheme: StateFlow<Boolean> = themePreferences.themeMode.map { it == "dark" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                currentListId = getDefaultListUseCase()
                Log.d(TAG, "Using list id: $currentListId")

                // Cargar ofertas EN PARALELO (no bloquea)
                launch {
                    try {
                        val offerList = getAllOffersUseCase()
                        _uiState.update { it.copy(offers = offerList) }
                        Log.d(TAG, "✅ Loaded ${offerList.size} offers: ${offerList.map { "${it.name}(id=${it.id})" }}")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error loading offers", e)
                    }
                }

                // Cargar categorías EN PARALELO (no bloquea)
                launch {
                    getAllCategoriesFlowUseCase()
                        .catch { e -> Log.e(TAG, "Error loading categories", e) }
                        .collect { categoryList ->
                            _uiState.update { it.copy(categories = categoryList) }
                        }
                }

                // Cargar supermercados (este sí usa collect, pero ya lanzamos lo demás antes)
                getAllSupermarketsFlowUseCase()
                    .catch { e -> Log.e(TAG, "Error loading supermarkets", e) }
                    .collect { supermarketList ->
                        _uiState.update { state ->
                            state.copy(
                                supermarkets = supermarketList,
                                selectedSupermarketId = state.selectedSupermarketId ?: supermarketList.firstOrNull()?.id,
                                isLoading = false
                            )
                        }

                        supermarketList.firstOrNull()?.id?.let { supermarketId ->
                            loadAislesAndProducts(supermarketId)
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadAislesAndProducts(supermarketId: Long?) {
        viewModelScope.launch {
            try {
                // Cargar pasillos del primer supermercado por defecto para mostrar
                val displaySupermarketId = supermarketId ?: _uiState.value.supermarkets.firstOrNull()?.id ?: 1L
                val aisleList = getAislesBySupermarketUseCase(displaySupermarketId)
                _uiState.update { it.copy(aisles = aisleList) }
                Log.d(TAG, "Loaded ${aisleList.size} aisles for supermarket $displaySupermarketId")

                // T4: null = todos los productos, >0 = filtrado
                getProductsByListUseCase(currentListId, supermarketId)
                    .catch { e -> Log.e(TAG, "Error loading products", e) }
                    .collect { productList ->
                        val aisleMap = productList.groupBy { product ->
                            aisleList.find { it.id == product.aisleId }
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
                Log.e(TAG, "Error loading aisles and products", e)
            }
        }
    }

    // T4: null = mostrar todos, Long = filtrar por supermercado
    fun selectSupermarket(supermarketId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSupermarketId = supermarketId) }
            loadAislesAndProducts(supermarketId)
        }
    }

    // T4: supermarketId ahora es parámetro, no sacado del estado
    fun addProduct(name: String, quantity: Float, aisleId: Long?, price: Float?, offerId: Long?, notes: String?, photoUri: String?, supermarketId: Long? = null) {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot add product: currentListId is 0")
                _uiState.update { it.copy(error = "Error: no hay lista activa") }
                return@launch
            }

            try {
                val finalPrice = calculateFinalPrice(quantity, price, offerId)
                val selectedAisleId = aisleId ?: _uiState.value.aisles.firstOrNull()?.id ?: 0L
                // T4: Usar el supermarketId pasado, o "Cualquiera" (0) si no se especifica
                val selectedSupermarketId = supermarketId ?: 0L

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

                if (price != null) {
                    savePriceHistoryUseCase(
                        productName = name,
                        price = price,
                        quantity = quantity.toInt()
                    )
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
                toggleProductPurchasedUseCase(product)
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

    fun clearAllProducts() {
        viewModelScope.launch {
            try {
                // Eliminar todos los productos de la lista actual
                _uiState.value.productsByAisle.values.flatten().forEach { product ->
                    deleteProductUseCase(product)
                }
                Log.d(TAG, "Cleared all products from list")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing all products", e)
                _uiState.update { it.copy(error = "Error al vaciar la lista: ${e.message}") }
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
    
    /**
     * Busca artículos por texto de voz y devuelve resultados para manejar:
     * - 1 coincidencia → añadir directo
     * - >1 coincidencias → mostrar diálogo
     * - 0 coincidencias → añadir genérico
     */
    suspend fun searchVoiceProducts(voiceText: String): List<com.jose.listacompra.domain.model.Articulo> {
        val parsed = com.jose.listacompra.ui.components.parseVoiceCommand(voiceText)
        val cleanName = parsed.productName
        
        if (cleanName.isBlank()) return emptyList()
        
        return searchArticulosUseCase(cleanName.lowercase())
    }
    
    /**
     * Añade un producto directamente desde el resultado de voz
     */
    fun addProductFromVoice(
        articulo: com.jose.listacompra.domain.model.Articulo,
        quantity: Float,
        parsedSupermarketName: String? = null // T4
    ) {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot add product: currentListId is 0")
                _uiState.update { it.copy(error = "Error: no hay lista activa") }
                return@launch
            }

            // T4: Prioridad: explícito > bottom bar > Cualquiera
            val supermarketId = when {
                // Si dijo supermercado explícitamente, buscarlo
                !parsedSupermarketName.isNullOrBlank() -> {
                    _uiState.value.supermarkets.find { 
                        it.name.contains(parsedSupermarketName, ignoreCase = true) 
                    }?.id ?: 0L // Si no encuentra, "Cualquiera"
                }
                // Si hay bottom bar (solo en Mi Lista), usarla
                _uiState.value.selectedSupermarketId != null -> _uiState.value.selectedSupermarketId!!
                // Si no hay contexto, "Cualquiera"
                else -> 0L
            }
            
            val product = Product(
                shoppingListId = currentListId,
                name = articulo.name,
                quantity = quantity,
                aisleId = 0L, // Sin supermercado específico por ahora
                supermarketId = supermarketId,
                estimatedPrice = articulo.finalPrice,
                finalPrice = articulo.finalPrice,
                offerId = null,
                notes = "",
                photoUri = articulo.photoUri
            )
            
            addProductUseCase(product)
            Log.d(TAG, "Product added from voice: ${articulo.name} x$quantity")
            
            updateProductFrequencyUseCase(
                name = articulo.name,
                aisleId = 0L,
                quantity = quantity,
                price = articulo.finalPrice,
                supermarketId = supermarketId // Usar el supermarketId ya calculado
            )
        }
    }
    
    /**
     * Añade un producto genérico cuando no hay coincidencias
     * TODO: Política de añadir producto sin artículo pendiente de definir
     */
    fun addGenericProductFromVoice(
        productName: String,
        quantity: Float,
        parsedSupermarketName: String? = null // T4
    ) {
        viewModelScope.launch {
            if (currentListId == 0L) {
                Log.e(TAG, "Cannot add product: currentListId is 0")
                return@launch
            }
            
            // T4: Prioridad: explícito > bottom bar > Cualquiera
            val supermarketId = when {
                !parsedSupermarketName.isNullOrBlank() -> {
                    _uiState.value.supermarkets.find { 
                        it.name.contains(parsedSupermarketName, ignoreCase = true) 
                    }?.id ?: 0L
                }
                _uiState.value.selectedSupermarketId != null -> _uiState.value.selectedSupermarketId!!
                else -> 0L
            }
            
            val product = Product(
                shoppingListId = currentListId,
                name = productName,
                quantity = quantity,
                aisleId = 0L,
                supermarketId = supermarketId,
                estimatedPrice = null,
                finalPrice = null,
                offerId = null,
                notes = "",
                photoUri = null
            )
            
            addProductUseCase(product)
            Log.d(TAG, "Generic product added from voice: $productName x$quantity")
        }
    }
}