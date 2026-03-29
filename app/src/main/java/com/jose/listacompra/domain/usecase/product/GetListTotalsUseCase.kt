package com.jose.listacompra.domain.usecase.product

import com.jose.listacompra.domain.model.TotalsResult
import com.jose.listacompra.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetListTotalsUseCase @Inject constructor(
    private val repository: IProductRepository
) {
    // Retornamos un Flow para que la UI se actualice sola si un precio cambia
    operator fun invoke(listId: Long): Flow<TotalsResult> {
        return repository.getProductsByListFlow(listId).map { products ->
// LOG PARA DEPURAR:
            println("DEBUG: Se han encontrado ${products.size} productos para la lista $listId")

            // Calculamos los totales usando la lista de productos actual
            val totalWithoutOffers = products.sumOf {
                (it.estimatedPrice ?: 0f) * it.quantity.toDouble()
            }.toFloat()

            val totalWithOffers = products.sumOf {
                (it.finalPrice ?: 0f).toDouble()
            }.toFloat()

            TotalsResult(
                totalWithoutOffers = totalWithoutOffers,
                totalWithOffers = totalWithOffers,
                savings = totalWithoutOffers - totalWithOffers
            )
        }
    }
}
