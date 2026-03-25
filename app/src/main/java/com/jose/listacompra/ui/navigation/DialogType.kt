package com.jose.listacompra.ui.navigation

import com.jose.listacompra.domain.model.Product

sealed class DialogType {
    object AddProduct : DialogType()
    object ManageAisles : DialogType()
    object ShowBarcodeScanner : DialogType()
    object ShowProductHistory : DialogType()
    object None : DialogType()  // Estado inicial

    data class EditProduct(val product: Product) : DialogType()
}