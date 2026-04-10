package com.jose.listacompra.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import com.jose.listacompra.domain.usecase.articulo.GetArticulosUseCase
import com.jose.listacompra.domain.usecase.category.GetCategoriesUseCase
import com.jose.listacompra.domain.usecase.ticket.ImportResult
import com.jose.listacompra.domain.usecase.ticket.ImportTicketUseCase
import com.jose.listacompra.domain.usecase.ticket.SaveTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketImportViewModel @Inject constructor(
    private val importTicketUseCase: ImportTicketUseCase,
    private val saveTicketUseCase: SaveTicketUseCase,
    private val getArticulosUseCase: GetArticulosUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketImportUiState())
    val uiState: StateFlow<TicketImportUiState> = _uiState.asStateFlow()

    init {
        loadCatalogData()
    }

    private fun loadCatalogData() {
        viewModelScope.launch {
            getArticulosUseCase().collect { articulos ->
                _uiState.update { it.copy(articulos = articulos) }
            }
        }
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    /**
     * Importa un ticket desde un archivo PDF.
     */
    fun importTicket(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = importTicketUseCase(
                uri = uri,
                articulos = _uiState.value.articulos,
                categories = _uiState.value.categories
            )

            result.fold(
                onSuccess = { importResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ticket = importResult.ticket,
                            unmatchedCount = importResult.unmatchedCount,
                            warnings = importResult.warnings,
                            step = ImportStep.REVIEW
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error desconocido"
                        )
                    }
                }
            )
        }
    }

    /**
     * Confirma el match de una línea con un artículo.
     */
    fun confirmMatch(lineId: Int, articuloId: Long) {
        val ticket = _uiState.value.ticket ?: return
        val articulo = _uiState.value.articulos.find { it.id == articuloId } ?: return

        val updatedLines = ticket.lines.mapIndexed { index, line ->
            if (index == lineId) {
                line.copy(
                    articuloId = articuloId,
                    articuloNombre = articulo.name,
                    confirmado = true
                )
            } else {
                line
            }
        }

        _uiState.update {
            it.copy(
                ticket = ticket.copy(lines = updatedLines),
                unmatchedCount = updatedLines.count { l -> l.articuloId == null }
            )
        }
    }

    /**
     * Crea un nuevo artículo para una línea sin match.
     */
    fun createArticuloForLine(lineId: Int, name: String, categoryId: Long?) {
        val ticket = _uiState.value.ticket ?: return
        val line = ticket.lines.getOrNull(lineId) ?: return

        // El artículo se creará al guardar el ticket
        val updatedLines = ticket.lines.mapIndexed { index, l ->
            if (index == lineId) {
                l.copy(
                    articuloNombre = name,
                    categoriaId = categoryId,
                    confirmado = true
                )
            } else {
                l
            }
        }

        _uiState.update {
            it.copy(
                ticket = ticket.copy(lines = updatedLines),
                unmatchedCount = updatedLines.count { l -> l.articuloId == null }
            )
        }
    }

    /**
     * Guarda el ticket importado.
     */
    fun saveTicket() {
        val ticket = _uiState.value.ticket ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val ticketId = saveTicketUseCase(ticket)

            _uiState.update {
                it.copy(
                    isSaving = false,
                    savedTicketId = ticketId,
                    step = ImportStep.COMPLETE
                )
            }
        }
    }

    /**
     * Cancela la importación actual.
     */
    fun cancel() {
        _uiState.value = TicketImportUiState()
    }

    /**
     * Reinicia para importar otro ticket.
     */
    fun reset() {
        _uiState.value = TicketImportUiState(step = ImportStep.SELECT_FILE)
    }
}

data class TicketImportUiState(
    val step: ImportStep = ImportStep.SELECT_FILE,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val ticket: Ticket? = null,
    val articulos: List<Articulo> = emptyList(),
    val categories: List<Category> = emptyList(),
    val unmatchedCount: Int = 0,
    val warnings: List<String> = emptyList(),
    val savedTicketId: Long? = null
)

enum class ImportStep {
    SELECT_FILE,  // Seleccionar PDF
    LOADING,      // OCR + parsing
    REVIEW,       // Revisar matches
    COMPLETE      // Guardado
}
