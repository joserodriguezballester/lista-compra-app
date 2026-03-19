package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.usecase.articulo.GetAllArticulosUseCase
import com.jose.listacompra.domain.usecase.articulo.GetArticuloByEanUseCase
import com.jose.listacompra.domain.usecase.articulo.SaveArticuloUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class ArticuloViewModel(
    private val getArticulosUseCase: GetAllArticulosUseCase,
    private val saveArticuloUseCase: SaveArticuloUseCase,
    private val getArticuloByEanUseCase: GetArticuloByEanUseCase
) : ViewModel() {

    // La UI observa este estado
    val listaArticulos = getArticulosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cuando escaneas algo:
    fun onScannerResult(barcode: String) {
        viewModelScope.launch {
            val articuloExistente = getArticuloByEanUseCase(barcode)
            if (articuloExistente != null) {
                // Lo encontramos, cargar datos para editar o añadir
            } else {
                // No existe, abrir formulario de "Nuevo Artículo" con ese EAN
            }
        }
    }
}