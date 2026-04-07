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
    val usesCategories: Boolean = false,
    val isLoading: Boolean = true,
    val isReordering: Boolean = false,
    val reorderedAisles: List<Aisle> = emptyList()
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
                    usesCategories = aisles.isEmpty(),
                    isLoading = false,
                    isReordering = false,
                    reorderedAisles = emptyList()
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
    
    fun startReordering() {
        _uiState.update { state ->
            state.copy(
                isReordering = true,
                reorderedAisles = state.aisles.mapIndexed { index, aisle -> 
                    aisle.copy(orderIndex = index)
                }
            )
        }
    }
    
    fun moveAisleUp(aisle: Aisle) {
        val aisles = _uiState.value.reorderedAisles.toMutableList()
        val currentIndex = aisles.indexOfFirst { it.id == aisle.id }
        
        if (currentIndex > 0) {
            // Swap with previous
            val temp = aisles[currentIndex]
            aisles[currentIndex] = aisles[currentIndex - 1].copy(orderIndex = currentIndex)
            aisles[currentIndex - 1] = temp.copy(orderIndex = currentIndex - 1)
            
            _uiState.update { it.copy(reorderedAisles = aisles) }
        }
    }
    
    fun moveAisleDown(aisle: Aisle) {
        val aisles = _uiState.value.reorderedAisles.toMutableList()
        val currentIndex = aisles.indexOfFirst { it.id == aisle.id }
        
        if (currentIndex < aisles.size - 1) {
            // Swap with next
            val temp = aisles[currentIndex]
            aisles[currentIndex] = aisles[currentIndex + 1].copy(orderIndex = currentIndex)
            aisles[currentIndex + 1] = temp.copy(orderIndex = currentIndex + 1)
            
            _uiState.update { it.copy(reorderedAisles = aisles) }
        }
    }
    
    fun saveReorder() {
        val reorderedAisles = _uiState.value.reorderedAisles
        
        viewModelScope.launch {
            aisleRepository.updateAisles(reorderedAisles)
            val supermarketId = _uiState.value.supermarket?.id ?: return@launch
            loadSupermarket(supermarketId)
        }
    }
}
