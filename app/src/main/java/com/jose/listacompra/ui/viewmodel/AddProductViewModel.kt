package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.usecase.articulo.SearchArticulosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val searchArticulosUseCase: SearchArticulosUseCase,
    private val aisleRepository: IAisleRepository
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<Articulo>>(emptyList())
    val suggestions: StateFlow<List<Articulo>> = _suggestions.asStateFlow()
    
    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()
    
    private val searchQuery = MutableStateFlow("")

    init {
        // Debounce para no buscar en cada letra
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.length >= 2) {
                        _suggestions.value = searchArticulosUseCase(query)
                    } else {
                        _suggestions.value = emptyList()
                    }
                }
        }
    }

    fun searchArticulos(query: String) {
        searchQuery.value = query
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    fun loadAisles(supermarketId: Long) {
        viewModelScope.launch {
            _aisles.value = aisleRepository.getAislesBySupermarket(supermarketId)
        }
    }
}
