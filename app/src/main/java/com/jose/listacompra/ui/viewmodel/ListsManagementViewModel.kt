package com.jose.listacompra.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.data.preferences.ListPreferences
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.ShoppingList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ListsUiState(
    val activeLists: List<ShoppingList> = emptyList(),
    val archivedLists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val currentListId: Long = -1L
)

class ListsManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShoppingListRepository(application)
    private val listPreferences = ListPreferences(application)
    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()
    
    init { loadLists() }
    
    fun loadLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val activeLists = repository.getActiveLists()
            val archivedLists = repository.getArchivedLists()
            val currentId = listPreferences.selectedListId.first()
            _uiState.update { it.copy(activeLists = activeLists, archivedLists = archivedLists, currentListId = currentId, isLoading = false) }
        }
    }
    
    fun createList(name: String, useDefaultAisles: Boolean = true, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val listId = repository.createList(name, useDefaultAisles)
            
            // Si se eligió usar pasillos por defecto, inicializarlos
            if (useDefaultAisles) {
                repository.initializeDefaultAisles()
            }
            
            listPreferences.setSelectedListId(listId)
            loadLists()
            onCreated(listId)
        }
    }
    
    fun archiveList(listId: Long) {
        viewModelScope.launch {
            repository.archiveList(listId)
            val currentId = listPreferences.selectedListId.first()
            if (currentId == listId) {
                val remainingActive = repository.getActiveLists()
                val newCurrentId = if (remainingActive.isNotEmpty()) remainingActive.first().id else repository.createList("Mi Lista", true)
                listPreferences.setSelectedListId(newCurrentId)
            }
            loadLists()
        }
    }
    
    fun unarchiveList(listId: Long) {
        viewModelScope.launch { repository.unarchiveList(listId); loadLists() }
    }
    
    fun deleteList(list: ShoppingList) {
        viewModelScope.launch { if (list.isArchived()) { repository.deleteList(list); loadLists() } }
    }
    
    fun selectList(listId: Long) {
        viewModelScope.launch { listPreferences.setSelectedListId(listId); _uiState.update { it.copy(currentListId = listId) } }
    }
    
    fun renameList(list: ShoppingList, newName: String) {
        viewModelScope.launch { repository.updateList(list.copy(name = newName)); loadLists() }
    }
}
