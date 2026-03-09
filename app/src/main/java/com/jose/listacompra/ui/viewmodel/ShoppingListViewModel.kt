package com.jose.listacompra.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.local.InitialDataSeeder
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.preferences.ListPreferences
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.usecase.AddProductUseCase
import com.jose.listacompra.ui.state.ShoppingListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val listPreferences: ListPreferences,
    private val addProductUseCase: AddProductUseCase, // ← AÑADIR
    //  private val calculatePriceUseCase: CalculateProductPriceUseCase // ← Si lo tienes
    application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _currentListId = MutableStateFlow<Long?>(null)
    val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Crear pasillos por defecto
            repository.initializeDefaultAisles()

            // 2. Crear ofertas por defecto
            repository.initializeDefaultOffers()

            // 3. Cargar lista guardada o crear una por defecto
            val savedListId = listPreferences.selectedListId.first()
            val listId = if (savedListId != -1L && repository.getListById(savedListId) != null) {
                savedListId
            } else {
                // Crear lista por defecto si no hay ninguna
                repository.createDefaultListIfNeeded()
            }

            _currentListId.value = listId
            listPreferences.setSelectedListId(listId)

            // 4. Cargar datos iniciales de Carrefour (solo primera vez)
            InitialDataSeeder.seedIfNeeded(repository)

            // 5. Cargar datos en UI
            loadData()
        }
    }

    /**
     * Cambia a otra lista de compras
     */
    fun switchToList(listId: Long) {
        viewModelScope.launch {
            _currentListId.value = listId
            listPreferences.setSelectedListId(listId)
            loadData()
        }
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        val listId = _currentListId.value ?: return

        val currentList = repository.getListById(listId)
        val aisles = repository.getAllAisles()
        val products = repository.getAllProducts(listId)
        val offers = repository.getAllOffers()

        // Calcular totales
        val totalWithoutOffers = products.sumOf { it.totalPriceWithoutOffer().toDouble() }.toFloat()
        val totalWithOffers = products.sumOf { it.finalPriceToPay().toDouble() }.toFloat()
        val savings = totalWithoutOffers - totalWithOffers

        val purchased = products.count { it.isPurchased }

        _uiState.update {
            it.copy(
                currentList = currentList,
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
     * Recarga los datos de la lista actual
     */
    fun refreshData() {
        viewModelScope.launch {
          //  val listId = _uiState.value.currentList?.id ?: return@launch
            loadData()
        }
    }

    /**
     * Añade un producto con posible oferta aplicada
     */
    fun addProduct(
        name: String,
        quantity: Float = 1f,
        price: Float? = null
    ) {
        viewModelScope.launch {
            val listId = _uiState.value.currentList?.id ?: return@launch

            // Usa el UseCase - asigna pasillo genérico automáticamente
            addProductUseCase(
                name = name,
                quantity = quantity,
                listId = listId,
                estimatedPrice = price
            )

            refreshData()
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
            val listId = _currentListId.value ?: return@launch
            val product = Product(
                id = productId,
                name = name,
                aisleId = aisleId,
                shoppingListId = listId,
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

    /**
     * Reordena los pasillos y persiste el cambio en la base de datos
     */
    fun reorderAisles(reorderedAisles: List<Aisle>) {
        viewModelScope.launch {
            repository.reorderAisles(reorderedAisles)
            loadData()
        }
    }

    fun clearPurchased() {
        viewModelScope.launch {
            val listId = _currentListId.value ?: return@launch
            repository.deletePurchasedProducts(listId)
            loadData()
        }
    }

    fun clearAllProducts() {
        viewModelScope.launch {
            val listId = _currentListId.value ?: return@launch
            repository.deleteAllProductsFromList(listId)
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
            addProduct(name, quantity, price)
        }
    }
    // Para historial
    fun addProductFromHistory(
        name: String,
        lastQuantity: Float
    ) {
        addProduct(
            name = name,
            quantity = lastQuantity
        )
    }
    // ========== HISTORIAL DE COMPRAS (TICKETS) ==========

    /**
     * Guarda una compra completa desde un ticket PDF
     */
    fun savePurchaseFromTicket(
        total: Float,
        numProductos: Int,
        tienda: String,
        ahorro: Float,
        products: List<Triple<String, Float, String?>>
    ) {
        viewModelScope.launch {
            try {
                repository.savePurchase(total, numProductos, tienda, ahorro, products)
                // No necesitamos recargar nada, solo guardar en BD
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Obtiene productos sugeridos basados en frecuencia de compra
     */
    fun getSuggestedProductsByFrequency(callback: (List<ProductFrequencyEntity>) -> Unit) {
        viewModelScope.launch {
            val products = repository.getSuggestedProductsByFrequency()
            callback(products)
        }
    }

    /**
     * Obtiene el precio promedio de un producto
     */
    fun getAveragePriceForProduct(name: String, callback: (Float?) -> Unit) {
        viewModelScope.launch {
            val avg = repository.getAveragePriceForProduct(name)
            callback(avg)
        }
    }

    fun showEmptyListConfirmDialog() {
        _uiState.update { it.copy(showEmptyListConfirmDialog = true) }
    }

    fun dismissEmptyListConfirmDialog() {
        _uiState.update { it.copy(showEmptyListConfirmDialog = false) }
    }

    fun emptyCurrentList() {
        viewModelScope.launch {
            val listId = _uiState.value.currentList?.id ?: return@launch
            repository.deleteAllProductsFromList(listId)
            dismissEmptyListConfirmDialog()
            refreshData()
        }
    }
}
