package com.jose.listacompra.ui.screens.supermarket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.ISupermarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupermarketListViewModel @Inject constructor(
    private val supermarketRepository: ISupermarketRepository
) : ViewModel() {
    
    val supermarkets: StateFlow<List<Supermarket>> = supermarketRepository
        .getAllSupermarkets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addSupermarket(name: String, emoji: String) {
        viewModelScope.launch {
            supermarketRepository.insertSupermarket(
                Supermarket(
                    name = name,
                    emoji = emoji,
                    isDefault = supermarkets.value.isEmpty()
                )
            )
        }
    }
    
    fun updateSupermarket(supermarket: Supermarket) {
        viewModelScope.launch {
            supermarketRepository.insertSupermarket(supermarket)
        }
    }
    
    fun deleteSupermarket(supermarket: Supermarket) {
        viewModelScope.launch {
            supermarketRepository.deleteSupermarket(supermarket.id)
        }
    }
}
