package com.jose.listacompra.utils.export

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.repository.ShoppingListRepository
import com.jose.listacompra.domain.model.ShoppingListExport
import com.jose.listacompra.domain.model.toExport
import javax.inject.Inject

class ShoppingListExporter @Inject constructor(
    private val gson: com.google.gson.Gson,
    private val productDao: ProductDao,
    private val aisleDao: AisleDao
) {

    suspend fun exportToJson(shoppingListRepository: ShoppingListRepository, listId: Long): String {
        val aisles = aisleDao.getAllAisles().map { it.toDomain() }
        val products = productDao.getAllProducts(listId).map { it.toDomain() }

        val export = ShoppingListExport(
            aisles = aisles.map { it.toExport() },
            products = products.map { it.toExport() }
        )

        return gson.toJson(export)
    }

    suspend fun importFromJson(json: String, listId: Long): Boolean {
        return try {
            val export = gson.fromJson(json, com.jose.listacompra.domain.model.ShoppingListExport::class.java)

            // Limpiar datos actuales de la lista
            productDao.deleteAllProducts(listId)

            // Importar pasillos (solo custom)
            export.aisles.filter { !it.isDefault }.forEach { aisle ->
                aisleDao.insertAisle(
                    AisleEntity(
                        id = aisle.id,
                        name = aisle.name,
                        emoji = aisle.emoji,
                        orderIndex = aisle.orderIndex,
                        isDefault = aisle.isDefault
                    )
                )
            }

            // Importar productos
            export.products.forEach { product ->
                productDao.insertProduct(
                    ProductEntity(
                        id = 0, // Nuevo ID autogenerado
                        name = product.name,
                        aisleId = product.aisleId,
                        shoppingListId = listId,
                        quantity = product.quantity,
                        estimatedPrice = product.estimatedPrice,
                        offerId = null, // Las ofertas no se exportan/importan
                        finalPrice = null,
                        isPurchased = product.isPurchased,
                        notes = product.notes,
                        orderIndex = product.orderIndex
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }


}