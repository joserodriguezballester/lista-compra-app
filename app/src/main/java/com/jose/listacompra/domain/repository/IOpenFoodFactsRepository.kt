package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.ScannedProduct

interface IOpenFoodFactsRepository {
    suspend fun getProductByBarcode(barcode: String): ScannedProduct?
}
