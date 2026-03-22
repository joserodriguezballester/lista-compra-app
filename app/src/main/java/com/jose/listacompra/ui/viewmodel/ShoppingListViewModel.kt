package com.jose.listacompra.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.local.InitialDataSeeder
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.preferences.ListPreferences
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.OfferPreviewResult
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.usecase.aisle.AddAisleUseCase
import com.jose.listacompra.domain.usecase.aisle.DeleteAisleUseCase
import com.jose.listacompra.domain.usecase.aisle.GetAllAislesUseCase
import com.jose.listacompra.domain.usecase.aisle.InitializeAislesUseCase
import com.jose.listacompra.domain.usecase.aisle.ReorderAislesUseCase
import com.jose.listacompra.domain.usecase.aisle.UpdateAisleUseCase
import com.jose.listacompra.domain.usecase.list.GetActiveListsUseCase
import com.jose.listacompra.domain.usecase.list.GetArchivedListsUseCase
import com.jose.listacompra.domain.usecase.list.GetDefaultListUseCase
import com.jose.listacompra.domain.usecase.list.GetListByIdUseCase
import com.jose.listacompra.domain.usecase.offers.CalculatePriceUseCase
import com.jose.listacompra.domain.usecase.offers.DeleteOfferUseCase
import com.jose.listacompra.domain.usecase.offers.GetAllOffersUseCase
import com.jose.listacompra.domain.usecase.offers.GetOfferByIdUseCase
import com.jose.listacompra.domain.usecase.offers.InitializeOffersUseCase
import com.jose.listacompra.domain.usecase.product.AddProductUseCase
import com.jose.listacompra.domain.usecase.product.DeleteAllProductsUseCase
import com.jose.listacompra.domain.usecase.product.DeleteProductUseCase
import com.jose.listacompra.domain.usecase.product.DeletePurchasedProductsUseCase
import com.jose.listacompra.domain.usecase.product.GetAllProductsUseCase
import com.jose.listacompra.domain.usecase.product.ToggleProductPurchasedUseCase
import com.jose.listacompra.domain.usecase.product.UpdateProductUseCase
import com.jose.listacompra.ui.state.ShoppingListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
      private val repository: ShoppingListRepository,

    //LISTAS
    private val listPreferences: ListPreferences,
    private val getListByIdUseCase: GetListByIdUseCase,
    private val getActiveListsUseCase: GetActiveListsUseCase,
    private val getArchivedListsUseCase: GetArchivedListsUseCase,
    private val getDefaulListUseCase: GetDefaultListUseCase,


    //OFFERS
    private val initializeOffersUseCase: InitializeOffersUseCase,
    private val getAllOffersUseCase: GetAllOffersUseCase,
    // private val addOfferUseCase: AddOfferUseCase,
    // private val updateOfferUseCase: UpdateOfferUseCase,
    private val deleteOfferUseCase: DeleteOfferUseCase,
    //  private val reorderOffersUseCase: ReorderOffersUseCase,
    private val getOfferByIdUseCase: GetOfferByIdUseCase,
    private val calculatePriceUseCase: CalculatePriceUseCase,

    // AISLE
    private val getAllAislesUseCase: GetAllAislesUseCase,
    private val addAisleUseCase: AddAisleUseCase,
    private val updateAisleUseCase: UpdateAisleUseCase,
    private val deleteAisleUseCase: DeleteAisleUseCase,
    private val reorderAislesUseCase: ReorderAislesUseCase,
    private val initializeAislesUseCase: InitializeAislesUseCase,

    //PRODUCT
    // --- CONSULTAS ---
    private val getAllProductsUseCase: GetAllProductsUseCase,

    // --- ACCIONES SOBRE PRODUCTOS ---
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val toggleProductPurchasedUseCase: ToggleProductPurchasedUseCase,

    // --- OPERACIONES MASIVAS ---
    private val deletePurchasedProductsUseCase: DeletePurchasedProductsUseCase,
    private val deleteAllProductsUseCase: DeleteAllProductsUseCase,

    // --- UTILIDADES ---
    // (Opcional, si al final decides no unificarlos en UpdateProductUseCase)
    // private val updateProductMediaUseCase: UpdateProductMediaUseCase
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
            //  repository.initializeDefaultAisles()
            initializeAislesUseCase
            // 2. Crear ofertas por defecto
            initializeOffersUseCase()

            // 3. Cargar lista guardada o crear una por defecto
            val savedListId = listPreferences.selectedListId.first()
            val listId = if (savedListId != -1L && getListByIdUseCase(savedListId) != null) {
                savedListId
            } else {
                // Crear lista por defecto si no hay ninguna
                getDefaulListUseCase()
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

        val currentList = getListByIdUseCase(listId)
        val aisles = getAllAislesUseCase()
        val products = getAllProductsUseCase(listId)
        val offers = getAllOffersUseCase()

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
        price: Float? = null,
        offerId: Long? = null,
        aisleId: Long = 1,
        photoUri: String? = null,
        ean: String? = null,
        notes: String = "",
        orderIndex: Int = 0,
        isPurchased: Boolean = false,
        finalPrice: Float? = null,
    ) {
        viewModelScope.launch {
            val currentListId = _uiState.value.currentList?.id ?: return@launch
            val newProduct = Product(
                name = name,
                quantity = quantity,
                estimatedPrice = price,
                offerId = offerId,
                aisleId = aisleId,
                photoUri = photoUri,
                ean = ean,
                notes = notes,
                orderIndex = orderIndex,
                shoppingListId = currentListId,
                isPurchased = isPurchased,
                finalPrice = finalPrice
            )
            addProductUseCase(newProduct)

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
        notes: String = "",
        photoUri: String? = null,
        ean: String? = null
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
                photoUri = photoUri,
                ean = ean,
                isPurchased = false  // Se mantendrá el valor actual si se carga primero
            )
            updateProductUseCase(product)
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
        val offerCode = uiState.value.offers.find { it.id == offerId }?.code
        // val finalPrice = calculatePriceUseCase(quantity, unitPrice, offer?.code)
        //   val totalWithoutOffer = quantity * unitPrice
// 3. Devolvemos directamente lo que el Use Case ya empaquetó
        return calculatePriceUseCase(quantity, unitPrice, offerCode)
//        return OfferPreviewResult(
//            finalPrice = finalPrice ?: totalWithoutOffer,
//            savings = totalWithoutOffer - (finalPrice ?: totalWithoutOffer),
//            hasOffer = offer != null
//        )
    }

    fun togglePurchased(product: Product) {
        viewModelScope.launch {
            toggleProductPurchasedUseCase(product)
            //  repository.toggleProductPurchased(product)
            loadData()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            deleteProductUseCase(product)
            loadData()
        }
    }

    fun addAisle(name: String, emoji: String) {
        viewModelScope.launch {
            val maxOrder = getAllAislesUseCase().maxOfOrNull { it.orderIndex } ?: 0
            val aisle = Aisle(
                name = name,
                emoji = emoji.ifBlank { "📦" },
                orderIndex = maxOrder + 1,
                isDefault = false
            )
            addAisleUseCase(aisle)
            loadData()
        }
    }

    fun deleteAisle(aisle: Aisle) {
        viewModelScope.launch {
            // No eliminar pasillos por defecto
            if (!aisle.isDefault) {
                deleteAisleUseCase(aisle)
                loadData()
            }
        }
    }

    /**
     * Reordena los pasillos y persiste el cambio en la base de datos
     */
    fun reorderAisles(reorderedAisles: List<Aisle>) {
        viewModelScope.launch {
            reorderAislesUseCase(reorderedAisles)
            loadData()
        }
    }

    fun clearPurchased() {
        viewModelScope.launch {
            val listId = _currentListId.value ?: return@launch
            deletePurchasedProductsUseCase(listId)
            loadData()
        }
    }

    fun clearAllProducts() {
        viewModelScope.launch {
            val listId = _currentListId.value ?: return@launch
            deleteAllProductsUseCase(listId)
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

    fun updateProductPhoto(product: Product, newPhotoUri: String?) {
        viewModelScope.launch {
            // Creamos la copia con el nuevo dato
            val updatedProduct = product.copy(photoUri = newPhotoUri)
            updateProductUseCase(updatedProduct)
            loadData()
        }
    }


    fun updateProductEan(product: Product, newEan: String?) {
        viewModelScope.launch {
            val updatedProduct = product.copy(ean = newEan)
            updateProductUseCase(updatedProduct)
        }
    }
    fun emptyCurrentList() {
        viewModelScope.launch {
            val listId = _uiState.value.currentList?.id ?: return@launch
            deleteAllProductsUseCase(listId)
            //  repository.deleteAllProductsFromList(listId)
            dismissEmptyListConfirmDialog()
            refreshData()
        }
    }

    fun categorizeProducts() {
        TODO("Not yet implemented")
    }

    fun toggleSortOrder() {
        TODO("Not yet implemented")
    }
}
