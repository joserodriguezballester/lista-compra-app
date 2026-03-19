package com.jose.listacompra.ui.navigation

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Product

sealed class DialogType {
    object AddProduct : DialogType()
    object ManageAisles : DialogType()
 //   object ColorSettings : DialogType()
    object ImportTicket : DialogType()
    object ShowColorSettings : DialogType()
    object ShowProductHistory : DialogType()
    object ShowBarcodeScanner : DialogType()


    // Diálogos con datos (Estado transportado)
    data class EditProduct(val product: Product) : DialogType()
//    data class EditArticulo(val articulo: Articulo) : DialogType()

    // Podrías añadir confirmaciones
 //   data class ConfirmDelete(val msg: String, val onConfirm: () -> Unit) : DialogType()
}