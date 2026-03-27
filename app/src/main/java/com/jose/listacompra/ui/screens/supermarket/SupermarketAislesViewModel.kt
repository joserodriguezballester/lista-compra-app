package com.jose.listacompra.ui.screens.supermarket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupermarketAislesUiState(
    val supermarket: Supermarket? = null,
    val aisles: List<Aisle> = emptyList(),
    val usesCategories: Boolean = false
)

@HiltViewModel
class SupermarketAislesViewModel @Inject constructor(
    private val supermarketRepository: ISupermarketRepository,
    private val aisleRepository: IAisleRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SupermarketAislesUiState())
    val uiState: StateFlow<SupermarketAislesUiState> = _uiState.asStateFlow()
    
    fun loadSupermarket(supermarketId: Long) {
        viewModelScope.launch {
            val supermarket = supermarketRepository.getSupermarketById(supermarketId)
            val aisles = aisleRepository.getAislesBySupermarket(supermarketId)
            
            _uiState.update { state ->
                state.copy(
                    supermarket = supermarket,
                    aisles = aisles.sortedBy { it.orderIndex },
                    usesCategories = aisles.isEmpty()
                )
            }
        }
    }
    
    fun addAisle(name: String, emoji: String, orderIndex: Int) {
        val supermarketId = _uiState.value.supermarket?.id ?: return
        
        viewModelScope.launch {
            val aisle = Aisle(
                name = name,
                emoji = emoji,
                orderIndex = orderIndex,
                supermarketId = supermarketId,
                isDefault = false
            )
            aisleRepository.addAisle(aisle)
            loadSupermarket(supermarketId)
        }
    }
    
    fun updateAisle(aisle: Aisle) {
        viewModelScope.launch {
            aisleRepository.updateAisle(aisle)
            loadSupermarket(aisle.supermarketId)
        }
    }
    
    fun deleteAisle(aisle: Aisle) {
        viewModelScope.launch {
            aisleRepository.deleteAisle(aisle)
            loadSupermarket(aisle.supermarketId)
        }
    }
}
