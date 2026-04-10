package com.jose.listacompra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.domain.usecase.history.GetPriceHistoryUseCase
import com.jose.listacompra.domain.usecase.history.GetPriceStatsUseCase
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import com.jose.listacompra.domain.usecase.history.UpdateProductFrequencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestión de historial de precios y estadísticas.
 * Extraído de ProductListViewModel para mejor separación de responsabilidades.
 */
@HiltViewModel
class PriceHistoryViewModel @Inject constructor(
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val getPriceStatsUseCase: GetPriceStatsUseCase,
    private val savePriceHistoryUseCase: SavePriceHistoryUseCase,
    private val updateProductFrequencyUseCase: UpdateProductFrequencyUseCase
) : ViewModel() {

    private val TAG = "PriceHistoryViewModel"

    private val _uiState = MutableStateFlow(PriceHistoryUiState())
    val uiState: StateFlow<PriceHistoryUiState> = _uiState.asStateFlow()

    /**
     * Carga el historial de precios de un producto
     */
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
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Guarda un registro de precio
     */
    fun savePriceHistory(
        productName: String,
        price: Float,
        quantity: Int
    ) {
        viewModelScope.launch {
            try {
                savePriceHistoryUseCase(productName, price, quantity)
                Log.d(TAG, "Saved price history for '$productName': $price €")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving price history", e)
            }
        }
    }

    /**
     * Actualiza la frecuencia de compra de un producto
     */
    fun updateProductFrequency(
        name: String,
        aisleId: Long,
        quantity: Float,
        price: Float?,
        supermarketId: Long
    ) {
        viewModelScope.launch {
            try {
                updateProductFrequencyUseCase(name, aisleId, quantity, price, supermarketId)
                Log.d(TAG, "Updated frequency for '$name'")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating frequency", e)
            }
        }
    }

    /**
     * Limpia el historial seleccionado
     */
    fun clearSelectedHistory() {
        _uiState.update { 
            it.copy(
                selectedPriceHistory = emptyList(),
                selectedPriceStats = null
            )
        }
    }
}

/**
 * Estado UI para historial de precios
 */
data class PriceHistoryUiState(
    val selectedPriceHistory: List<ProductPriceHistoryEntity> = emptyList(),
    val selectedPriceStats: PriceStats? = null,
    val error: String? = null
)
