package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.preferences.ListPreferences
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.usecase.aisle.InitializeAislesUseCase
import com.jose.listacompra.domain.usecase.list.CreateListUseCase
import com.jose.listacompra.domain.usecase.list.DeleteListUseCase
import com.jose.listacompra.domain.usecase.list.GetActiveListsUseCase
import com.jose.listacompra.domain.usecase.list.GetArchivedListsUseCase
import com.jose.listacompra.domain.usecase.list.UnarchiveListUseCase
import com.jose.listacompra.domain.usecase.list.UpdateListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListsUiState(
    val activeLists: List<ShoppingList> = emptyList(),
    val archivedLists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val currentListId: Long = -1L
)

@HiltViewModel
class ListsManagementViewModel @Inject constructor(
    private val listPreferences: ListPreferences,
    private val getActiveListsUseCase: GetActiveListsUseCase,
    private val getArchivedListsUseCase: GetArchivedListsUseCase,
    private val createListUseCase: CreateListUseCase,
    private val unarchiveListUseCase: UnarchiveListUseCase,
    private val deleteListUseCase: DeleteListUseCase,
    private val updateListUseCase: UpdateListUseCase,
    private val initializeAislesUseCase: InitializeAislesUseCase

    ) : ViewModel() {
    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val activeLists = getActiveListsUseCase()
            val archivedLists = getArchivedListsUseCase()
            val currentId = listPreferences.selectedListId.first()
            _uiState.update {
                it.copy(
                    activeLists = activeLists,
                    archivedLists = archivedLists,
                    currentListId = currentId,
                    isLoading = false
                )
            }
        }
    }

    fun createList(name: String, useDefaultAisles: Boolean = true, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val listId = createListUseCase(name, useDefaultAisles)
            if (useDefaultAisles) {
                initializeAislesUseCase()
            }
            listPreferences.setSelectedListId(listId)
            loadLists()
            onCreated(listId)
        }
    }

    fun archiveList(listId: Long) {
        viewModelScope.launch {
            getArchivedListsUseCase()
            val currentId = listPreferences.selectedListId.first()
            if (currentId == listId) {
                val remainingActive = getActiveListsUseCase()
                val newCurrentId =
                    if (remainingActive.isNotEmpty()) remainingActive.first().id else createListUseCase(
                        "Mi Lista",
                        true
                    )
                listPreferences.setSelectedListId(newCurrentId)
            }
            loadLists()
        }
    }

    fun unarchiveList(listId: Long) {
        viewModelScope.launch { unarchiveListUseCase(listId); loadLists() }
    }

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            if (list.isArchived()) {
                deleteListUseCase(list); loadLists()
            }
        }
    }

    fun selectList(listId: Long) {
        viewModelScope.launch {
            listPreferences.setSelectedListId(listId); _uiState.update {
            it.copy(
                currentListId = listId
            )
        }
        }
    }

    fun renameList(list: ShoppingList, newName: String) {
        viewModelScope.launch { updateListUseCase(list.copy(name = newName)); loadLists() }
    }
}
