package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.usecase.offers.GetAllOffersUseCase
import com.jose.listacompra.domain.usecase.offers.SaveOfferUseCase
import com.jose.listacompra.domain.usecase.offers.DeleteOfferUseCase
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
    private val getAllOffersUseCase: GetAllOffersUseCase,
    private val saveOfferUseCase: SaveOfferUseCase,
    private val deleteOfferUseCase: DeleteOfferUseCase
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
    
    fun addOffer(offer: Offer) {
        viewModelScope.launch {
            try {
                saveOfferUseCase(offer)
                loadOffers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateOffer(offer: Offer) {
        viewModelScope.launch {
            try {
                saveOfferUseCase(offer)
                loadOffers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteOffer(offer: Offer) {
        viewModelScope.launch {
            try {
                deleteOfferUseCase(offer)
                loadOffers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
