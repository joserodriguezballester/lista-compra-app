package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.usecase.SeedAllDataUseCase
import com.jose.listacompra.domain.usecase.articulo.DeleteArticuloUseCase
import com.jose.listacompra.domain.usecase.articulo.GetAllArticulosUseCase
import com.jose.listacompra.domain.usecase.articulo.GetArticuloByEanUseCase
import com.jose.listacompra.domain.usecase.articulo.SaveArticuloUseCase
import com.jose.listacompra.domain.usecase.articulo.UpdateArticuloUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticuloViewModel @Inject constructor(
    private val getAllArticulosUseCase: GetAllArticulosUseCase,
    private val saveArticuloUseCase: SaveArticuloUseCase,
    private val deleteArticuloUseCase: DeleteArticuloUseCase,
    private val getArticuloByEanUseCase: GetArticuloByEanUseCase,
    private val updateArticuloUseCase: UpdateArticuloUseCase,
    private val seedAllDataUseCase: SeedAllDataUseCase,
    // private val getArticuloByIdUseCase: GetArticuloByIdUseCase,
    // private val searchArticulosUseCase: SearchArticulosUseCase
) : ViewModel() {

    // Estado observable para la UI
    val listaArticulos = getAllArticulosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    init {
        // Al iniciar el ViewModel, comprobamos si hay que poblar la BD
        viewModelScope.launch {
            seedAllDataUseCase()
        }
    }

    // Guardar artículo (crear o actualizar)
    fun addArticulo(articulo: Articulo) {
        viewModelScope.launch { saveArticuloUseCase(articulo) }
    }

    fun updateArticulo(nuevoArticulo: Articulo) {
        viewModelScope.launch { updateArticuloUseCase(nuevoArticulo) }
    }

    // Eliminar artículo
    fun deleteArticulo(articulo: Articulo) {
        viewModelScope.launch { deleteArticuloUseCase(articulo) }
    }

    // Buscar por EAN (para scanner)
    fun onScannerResult(
        barcode: String,
        onFound: (Articulo) -> Unit = {},
        onNotFound: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val articulo = getArticuloByEanUseCase(barcode)
            if (articulo != null) {
                onFound(articulo)
            } else {
                onNotFound()
            }
        }
    }

    // Buscar por ID (para edición)
//    suspend fun getArticuloById(id: Long): Articulo? {
//        return getArticuloByIdUseCase(id)
//    }

    // Búsqueda por texto (para catálogo)
//    suspend fun searchArticulos(query: String): List<Articulo> {
//        return searchArticulosUseCase(query)
//    }


}