package com.jose.listacompra

import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.SpendingStats
import com.jose.listacompra.domain.model.Supermarket
import com.jose.listacompra.domain.repository.IHistoryRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import com.jose.listacompra.domain.usecase.history.CompletePurchaseUseCase
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import com.jose.listacompra.domain.usecase.product.ToggleProductPurchasedUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleProductPurchasedUseCaseTest {

    @Test
    fun `marking product as purchased stores manual purchase, price history and frequency`() = runBlocking {
        val productRepository = FakeProductRepository()
        val historyRepository = FakeHistoryRepository()
        val supermarketRepository = FakeSupermarketRepository()
        val useCase = ToggleProductPurchasedUseCase(
            productRepository = productRepository,
            historyRepository = historyRepository,
            supermarketRepository = supermarketRepository,
            savePriceHistoryUseCase = SavePriceHistoryUseCase(historyRepository),
            completePurchaseUseCase = CompletePurchaseUseCase(historyRepository)
        )

        useCase(
            Product(
                id = 42L,
                name = "Tomate frito",
                aisleId = 3L,
                supermarketId = 2L,
                quantity = 2f,
                estimatedPrice = 1.50f,
                finalPrice = 3.00f,
                isPurchased = false
            )
        )

        assertEquals(listOf(42L to true), productRepository.toggles)
        assertEquals(1, historyRepository.purchaseHistoryRecords.size)
        assertEquals(1, historyRepository.priceHistoryRecords.size)

        val purchase = historyRepository.purchaseHistoryRecords.single()
        assertEquals("Mercadona Mislata", purchase.tienda)
        assertEquals(3.00f, purchase.total)
        assertEquals(2, purchase.numProductos)

        val priceHistory = historyRepository.priceHistoryRecords.single()
        assertEquals(777L, priceHistory.purchaseId)
        assertEquals("tomate frito", priceHistory.productName)
        assertEquals(1.50f, priceHistory.price)
        assertEquals(2, priceHistory.quantity)

        val frequency = historyRepository.frequencyRecords["tomate frito"]
        assertNotNull(frequency)
        assertEquals(1, frequency?.timesPurchased)
        assertEquals(2f, frequency?.lastQuantity)
        assertEquals(1.50f, frequency?.lastPrice)
        assertEquals(3L, frequency?.lastAisleId)
        assertEquals(2L, frequency?.lastSupermarketId)
    }

    @Test
    fun `marking product without price still updates frequency but not price history`() = runBlocking {
        val productRepository = FakeProductRepository()
        val historyRepository = FakeHistoryRepository()
        val supermarketRepository = FakeSupermarketRepository()
        val useCase = ToggleProductPurchasedUseCase(
            productRepository = productRepository,
            historyRepository = historyRepository,
            supermarketRepository = supermarketRepository,
            savePriceHistoryUseCase = SavePriceHistoryUseCase(historyRepository),
            completePurchaseUseCase = CompletePurchaseUseCase(historyRepository)
        )

        useCase(
            Product(
                id = 7L,
                name = "Pan",
                aisleId = 1L,
                supermarketId = 0L,
                quantity = 1f,
                estimatedPrice = null,
                finalPrice = null,
                isPurchased = false
            )
        )

        assertEquals(listOf(7L to true), productRepository.toggles)
        assertTrue(historyRepository.purchaseHistoryRecords.isEmpty())
        assertTrue(historyRepository.priceHistoryRecords.isEmpty())

        val frequency = historyRepository.frequencyRecords["pan"]
        assertNotNull(frequency)
        assertEquals(1, frequency?.timesPurchased)
        assertEquals(1f, frequency?.lastQuantity)
        assertEquals(0f, frequency?.lastPrice)
    }

    @Test
    fun `unmarking purchased product does not write history or frequency again`() = runBlocking {
        val productRepository = FakeProductRepository()
        val historyRepository = FakeHistoryRepository().apply {
            frequencyRecords["leche"] = ProductFrequencyEntity(
                productName = "leche",
                originalName = "Leche",
                timesPurchased = 3,
                lastQuantity = 1f,
                lastPrice = 1.25f,
                lastAisleId = 2L,
                lastSupermarketId = 1L,
                lastPurchaseDate = 1_700_000_000_000L
            )
        }
        val supermarketRepository = FakeSupermarketRepository()
        val useCase = ToggleProductPurchasedUseCase(
            productRepository = productRepository,
            historyRepository = historyRepository,
            supermarketRepository = supermarketRepository,
            savePriceHistoryUseCase = SavePriceHistoryUseCase(historyRepository),
            completePurchaseUseCase = CompletePurchaseUseCase(historyRepository)
        )

        useCase(
            Product(
                id = 9L,
                name = "Leche",
                aisleId = 2L,
                supermarketId = 1L,
                quantity = 1f,
                estimatedPrice = 1.25f,
                finalPrice = 1.25f,
                isPurchased = true
            )
        )

        assertEquals(listOf(9L to false), productRepository.toggles)
        assertTrue(historyRepository.purchaseHistoryRecords.isEmpty())
        assertTrue(historyRepository.priceHistoryRecords.isEmpty())
        assertEquals(3, historyRepository.frequencyRecords["leche"]?.timesPurchased)
    }

    private class FakeProductRepository : IProductRepository {
        val toggles = mutableListOf<Pair<Long, Boolean>>()

        override suspend fun getAllProducts(listId: Long): List<Product> = emptyList()
        override suspend fun getProductById(id: Long): Product? = null
        override suspend fun insertProduct(product: Product): Long = 0L
        override suspend fun updateProduct(product: Product) = Unit
        override suspend fun deleteProduct(product: Product) = Unit
        override suspend fun togglePurchased(productId: Long, isPurchased: Boolean) {
            toggles += productId to isPurchased
        }
        override fun getProductsByListFlow(listId: Long): Flow<List<Product>> = emptyFlow()
        override fun getProductsBySupermarketFlow(listId: Long, supermarketId: Long): Flow<List<Product>> = emptyFlow()
        override fun getProductsBySupermarketOrAnyFlow(listId: Long, supermarketId: Long): Flow<List<Product>> = emptyFlow()
        override suspend fun updatePhoto(productId: Long, photoUri: String?) = Unit
        override suspend fun updateEan(productId: Long, ean: String?) = Unit
        override suspend fun getProductsByList(listId: Long): List<Product> = emptyList()
        override suspend fun getNextOrderIndex(listId: Long): Int? = null
        override suspend fun deletePurchasedProducts(listId: Long) = Unit
        override suspend fun deleteAllProductsFromList(listId: Long) = Unit
        override suspend fun getProductsByAisle(listId: Long, aisleId: Long): List<Product> = emptyList()
    }

    private class FakeHistoryRepository : IHistoryRepository {
        val purchaseHistoryRecords = mutableListOf<PurchaseHistoryEntity>()
        val priceHistoryRecords = mutableListOf<ProductPriceHistoryEntity>()
        val frequencyRecords = mutableMapOf<String, ProductFrequencyEntity>()

        override suspend fun getFrequency(productName: String): ProductFrequencyEntity? = frequencyRecords[productName]
        override suspend fun updateFrequency(entity: ProductFrequencyEntity) {
            frequencyRecords[entity.productName] = entity
        }
        override suspend fun insertFrequency(entity: ProductFrequencyEntity) {
            frequencyRecords[entity.productName] = entity
        }
        override suspend fun getAllFrequencies(): List<ProductFrequencyEntity> = frequencyRecords.values.toList()
        override suspend fun getPriceHistory(productName: String): List<ProductPriceHistoryEntity> = emptyList()
        override suspend fun getPriceStats(productName: String): PriceStats? = null
        override suspend fun savePriceHistory(priceHistory: ProductPriceHistoryEntity) {
            priceHistoryRecords += priceHistory
        }
        override suspend fun insertPurchaseHistory(purchaseHistory: PurchaseHistoryEntity): Long {
            purchaseHistoryRecords += purchaseHistory
            return 777L
        }
        override suspend fun getProductSuggestions(query: String): List<ProductFrequencyEntity> = emptyList()
        override suspend fun getSpendingStats(): SpendingStats = SpendingStats(0f, 0f, 0)
    }

    private class FakeSupermarketRepository : ISupermarketRepository {
        override fun getAllSupermarkets(): Flow<List<Supermarket>> = emptyFlow()
        override suspend fun getSupermarketById(id: Long): Supermarket? = when (id) {
            1L -> Supermarket(1L, "Carrefour La Alberca")
            2L -> Supermarket(2L, "Mercadona Mislata")
            else -> null
        }
        override suspend fun getDefaultSupermarket(): Supermarket? = null
        override suspend fun insertSupermarket(supermarket: Supermarket): Long = 0L
        override suspend fun insertAll(supermarkets: List<Supermarket>) = Unit
        override suspend fun ensureBuiltinSupermarkets() = Unit
        override suspend fun deleteSupermarket(id: Long) = Unit
    }
}
