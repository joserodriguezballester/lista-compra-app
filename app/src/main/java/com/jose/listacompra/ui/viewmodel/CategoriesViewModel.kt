package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.usecase.category.GetAllCategoriesFlowUseCase
import com.jose.listacompra.domain.usecase.category.AddCategoryUseCase
import com.jose.listacompra.domain.usecase.category.UpdateCategoryUseCase
import com.jose.listacompra.domain.usecase.category.DeleteCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getAllCategoriesFlowUseCase: GetAllCategoriesFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            getAllCategoriesFlowUseCase()
                .catch { e ->
                    _uiState.value = CategoriesUiState(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { categoryList ->
                    _uiState.value = CategoriesUiState(
                        categories = categoryList,
                        isLoading = false
                    )
                }
        }
    }
    
    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                addCategoryUseCase(category)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                updateCategoryUseCase(category)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
