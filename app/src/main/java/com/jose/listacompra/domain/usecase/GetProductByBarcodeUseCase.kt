package com.jose.listacompra.domain.usecase

import com.jose.listacompra.domain.model.ScannedProduct
import com.jose.listacompra.domain.repository.IOpenFoodFactsRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val openFoodFactsRepository: IOpenFoodFactsRepository
) {
    suspend operator fun invoke(barcode: String): ScannedProduct? {
        return openFoodFactsRepository.getProductByBarcode(barcode)
    }
}
