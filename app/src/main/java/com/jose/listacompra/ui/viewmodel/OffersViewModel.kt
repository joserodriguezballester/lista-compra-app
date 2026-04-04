package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.usecase.offers.GetAllOffersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OffersUiState(
    val offers: List<Offer> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val getAllOffersUseCase: GetAllOffersUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OffersUiState())
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()
    
    init {
        loadOffers()
    }
    
    private fun loadOffers() {
        viewModelScope.launch {
            try {
                val offerList = getAllOffersUseCase()
                _uiState.value = OffersUiState(
                    offers = offerList,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = OffersUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}