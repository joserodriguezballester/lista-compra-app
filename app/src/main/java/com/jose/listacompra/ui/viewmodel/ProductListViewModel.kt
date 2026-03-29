package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.usecase.aisle.GetAislesBySupermarketUseCase
import com.jose.listacompra.domain.usecase.product.AddProductUseCase
import com.jose.listacompra.domain.usecase.product.DeleteProductUseCase
import com.jose.listacompra.domain.usecase.product.GetAllProductsUseCase
import com.jose.listacompra.domain.usecase.product.ToggleProductPurchasedUseCase
import com.jose.listacompra.domain.usecase.product.UpdateProductUseCase
import com.jose.listacompra.domain.usecase.supermarket.GetAllSupermarketsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val aisles: List<Aisle> = emptyList(),
    val supermarkets: List<Supermarket> = emptyList(),
    val selectedSupermarketId: Long = 1,
    val isLoading: Boolean = true
) {
    // Productos agrupados por pasillo
    val productsByAisle: Map<Aisle, List<Product>>
        get() {
            val aisleMap = aisles.associateBy { it.id }
            return products
                .filter { it.supermarketId == selectedSupermarketId }
                .groupBy { aisleMap[it.aisleId] ?: Aisle(name = "Sin pasillo") }
                .toSortedMap(compareBy { it.orderIndex })
        }
    
    // Productos comprados
    val purchasedProducts: List<Product>
        get() = products.filter { it.isPurchased }
    
    // Productos pendientes
    val pendingProducts: List<Product>
        get() = products.filter { !it.isPurchased }
}

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val toggleProductPurchasedUseCase: ToggleProductPurchasedUseCase,
    private val getAllSupermarketsUseCase: GetAllSupermarketsUseCase,
    private val getAislesBySupermarketUseCase: GetAislesBySupermarketUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()
    
    private var currentListId: Long = 1
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Cargar supermercados
            getAllSupermarketsUseCase()
                .catch { e -> 
                    // TODO: Manejar error
                }
                .collect { supermarkets ->
                    _uiState.update { state ->
                        state.copy(
                            supermarkets = supermarkets,
                            // Solo actualizamos el ID si no hay uno seleccionado
                            selectedSupermarketId = state.selectedSupermarketId
                        //    selectedSupermarketId = supermarkets.firstOrNull()?.id ?: 1
                        )
                    }
                    // Si es la primera vez, cargamos los pasillos del primer super
                    if (supermarkets.isNotEmpty()) {
                        loadAisles(supermarkets.first().id)
                    }
                }
        }
        // 2. Cargar Productos de forma independiente (¡ESTO AHORA SÍ SE EJECUTARÁ!)
        viewModelScope.launch {
            loadProducts()
        }
    }
    
    private suspend fun loadAisles(supermarketId: Long) {
        getAislesBySupermarketUseCase(supermarketId)
            .catch { e ->
                // TODO: Manejar error
            }
            .collect { aisles ->
                _uiState.update { it.copy(aisles = aisles) }
            }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getAllProductsUseCase(currentListId)
                .collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
        }
    }
    fun selectSupermarket(supermarketId: Long) {
        _uiState.update { it.copy(selectedSupermarketId = supermarketId) }
        
        viewModelScope.launch {
            loadAisles(supermarketId)
        }
    }
    
    fun addProduct(product: Product) {
        viewModelScope.launch {
            addProductUseCase(product)
            loadProducts()
        }
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            updateProductUseCase(product)
            loadProducts()
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            deleteProductUseCase(product)
            loadProducts()
        }
    }
    
    fun togglePurchased(product: Product) {
        viewModelScope.launch {
            toggleProductPurchasedUseCase(product)
            loadProducts()
        }
    }
    
    fun deletePurchasedProducts() {
        viewModelScope.launch {
            _uiState.value.purchasedProducts.forEach { product ->
                deleteProductUseCase(product)
            }
            loadProducts()
        }
    }
}
