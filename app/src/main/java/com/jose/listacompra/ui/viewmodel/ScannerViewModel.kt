package com.jose.listacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.listacompra.domain.model.ScannedProduct
import com.jose.listacompra.domain.usecase.GetProductByBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase
) : ViewModel() {
    
    fun searchProduct(
        ean: String,
        onResult: (ScannedProduct?) -> Unit
    ) {
        viewModelScope.launch {
            val product = getProductByBarcodeUseCase(ean)
            onResult(product)
        }
    }
}
