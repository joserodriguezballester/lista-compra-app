package com.jose.listacompra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.domain.repository.IHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val selectedTab: Int = 0,
    val frequencyData: List<ProductFrequencyEntity> = emptyList(),
    val selectedProduct: ProductFrequencyEntity? = null,
    val priceHistory: List<ProductPriceHistoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: IHistoryRepository
) : ViewModel() {
    
    private val TAG = "HistoryViewModel"
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadFrequencyData()
    }
    
    private fun loadFrequencyData() {
        viewModelScope.launch {
            try {
                // Obtener todos los productos con frecuencia
                val data = historyRepository.getAllFrequencies()
                _uiState.update { 
                    it.copy(
                        frequencyData = data,
                        isLoading = false
                    )
                }
                Log.d(TAG, "Loaded ${data.size} products with frequency data")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading frequency data", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun selectProduct(product: ProductFrequencyEntity) {
        _uiState.update { it.copy(selectedProduct = product) }
        loadPriceHistory(product.productName)
    }
    
    private fun loadPriceHistory(productName: String) {
        viewModelScope.launch {
            try {
                val history = historyRepository.getPriceHistory(productName)
                _uiState.update { it.copy(priceHistory = history) }
                Log.d(TAG, "Loaded ${history.size} price records for $productName")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading price history", e)
                _uiState.update { it.copy(priceHistory = emptyList()) }
            }
        }
    }
}