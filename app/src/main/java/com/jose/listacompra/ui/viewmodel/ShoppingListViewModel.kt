package com.jose.listacompra.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.local.InitialDataSeeder
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val aisles: List<Aisle> = emptyList(),
    val products: List<Product> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val totalEstimate: Float = 0f,           // Total con ofertas aplicadas
    val totalWithoutOffers: Float = 0f,      // Total sin ofertas
    val savings: Float = 0f,                 // Ahorro total
    val purchasedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val productSuggestions: List<com.jose.listacompra.domain.model.ProductSuggestion> = emptyList()
)

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ShoppingListRepository(application)
    
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // 1. Crear pasillos por defecto
            repository.initializeDefaultAisles()
            
            // 2. Crear ofertas por defecto
            repository.initializeDefaultOffers()

            // 3. Cargar datos iniciales de Carrefour (solo primera vez)
            InitialDataSeeder.seedIfNeeded(repository)

            // 4. Cargar datos en UI
            loadData()
        }
    }
    
    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        
        val aisles = repository.getAllAisles()
        val products = repository.getAllProducts()
        val offers = repository.getAllOffers()
        
        // Calcular totales
        val totalWithoutOffers = products.sumOf { it.totalPriceWithoutOffer().toDouble() }.toFloat()
        val totalWithOffers = products.sumOf { it.finalPriceToPay().toDouble() }.toFloat()
        val savings = totalWithoutOffers - totalWithOffers
        
        val purchased = products.count { it.isPurchased }
        
        _uiState.update {
            it.copy(
                aisles = aisles,
                products = products,
                offers = offers,
                totalEstimate = totalWithOffers,
                totalWithoutOffers = totalWithoutOffers,
                savings = savings,
                purchasedCount = purchased,
                totalCount = products.size,
                isLoading = false
            )
        }
    }
    
    /**
     * Añade un producto con posible oferta aplicada
     */
    fun addProduct(
        name: String, 
        aisleId: Long, 
        quantity: Float, 
        price: Float?,
        offerId: Long? = null
    ) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                aisleId = aisleId,
                quantity = quantity,
                estimatedPrice = price,
                offerId = offerId,
                isPurchased = false
            )
            repository.addProduct(product)
            loadData()
        }
    }
    
    /**
     * Actualiza un producto existente con nuevos datos
     */
    fun updateProduct(
        productId: Long,
        name: String,
        aisleId: Long,
        quantity: Float,
        price: Float?,
        offerId: Long?,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val product = Product(
                id = productId,
                name = name,
                aisleId = aisleId,
                quantity = quantity,
                estimatedPrice = price,
                offerId = offerId,
                notes = notes,
                isPurchased = false  // Se mantendrá el valor actual si se carga primero
            )
            repository.updateProduct(product)
            loadData()
        }
    }
    
    /**
     * Calcula el precio final y ahorro para mostrar en tiempo real
     */
    fun calculateOfferPreview(
        quantity: Float, 
        unitPrice: Float?, 
        offerId: Long?
    ): OfferPreviewResult? {
        if (unitPrice == null || unitPrice <= 0) return null
        
        val offer = uiState.value.offers.find { it.id == offerId }
        val finalPrice = repository.calculateFinalPrice(quantity, unitPrice, offer?.code)
        val totalWithoutOffer = quantity * unitPrice
        
        return OfferPreviewResult(
            finalPrice = finalPrice ?: totalWithoutOffer,
            savings = totalWithoutOffer - (finalPrice ?: totalWithoutOffer),
            hasOffer = offer != null
        )
    }
    
    data class OfferPreviewResult(
        val finalPrice: Float,
        val savings: Float,
        val hasOffer: Boolean
    )
    
    fun togglePurchased(product: Product) {
        viewModelScope.launch {
            repository.toggleProductPurchased(product)
            loadData()
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            loadData()
        }
    }
    
    fun addAisle(name: String, emoji: String) {
        viewModelScope.launch {
            val maxOrder = repository.getAllAisles().maxOfOrNull { it.orderIndex } ?: 0
            val aisle = Aisle(
                name = name,
                emoji = emoji.ifBlank { "📦" },
                orderIndex = maxOrder + 1,
                isDefault = false
            )
            repository.addAisle(aisle)
            loadData()
        }
    }
    
    fun deleteAisle(aisle: Aisle) {
        viewModelScope.launch {
            // No eliminar pasillos por defecto
            if (!aisle.isDefault) {
                repository.deleteAisle(aisle)
                loadData()
            }
        }
    }
    
    fun clearPurchased() {
        viewModelScope.launch {
            repository.deletePurchasedProducts()
            loadData()
        }
    }

    // ========== AUTOCOMPLETADO DE PRODUCTOS ==========

    fun searchProductSuggestions(query: String) {
        viewModelScope.launch {
            val suggestions = repository.findProductSuggestions(query)
            _uiState.update { it.copy(productSuggestions = suggestions) }
        }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(productSuggestions = emptyList()) }
    }

    /**
     * Añade producto y lo guarda en historial para futuras sugerencias
     */
    fun addProductWithHistory(
        name: String, 
        aisleId: Long, 
        quantity: Float, 
        price: Float?,
        offerId: Long? = null
    ) {
        viewModelScope.launch {
            // Guardar en historial para autocompletado futuro
            repository.saveToHistory(name, aisleId, quantity, price)

            // Añadir producto normal
            addProduct(name, aisleId, quantity, price, offerId)
        }
    }
}
