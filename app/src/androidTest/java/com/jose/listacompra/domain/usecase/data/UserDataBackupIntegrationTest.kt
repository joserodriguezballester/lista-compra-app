package com.jose.listacompra.domain.usecase.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.jose.listacompra.data.local.ShoppingListDatabase
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.ArticuloEntity
import com.jose.listacompra.data.local.entities.ArticuloSupermarketDefaultEntity
import com.jose.listacompra.data.local.entities.CategoryEntity
import com.jose.listacompra.data.local.entities.CategorySupermarketOrderEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductHistoryEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.data.local.entities.SupermarketEntity
import com.jose.listacompra.data.local.entities.TicketEntity
import com.jose.listacompra.data.local.entities.TicketLineEntity
import com.jose.listacompra.domain.model.UserDataBackup
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDataBackupIntegrationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val openedDbs = mutableListOf<ShoppingListDatabase>()

    @After
    fun tearDown() {
        openedDbs.forEach { it.close() }
        openedDbs.clear()
    }

    @Test
    fun export_includes_complete_blocks_and_omits_fragile_paths() = runBlocking {
        val db = newDb()
        seedDataset(db)

        val json = ExportUserDataBackupUseCase(
            context = context,
            articuloDao = db.articuloDao(),
            shoppingListDao = db.shoppingListDao(),
            productDao = db.productDao(),
            aisleDao = db.aisleDao(),
            ticketDao = db.ticketDao(),
            purchaseHistoryDao = db.purchaseHistoryDao(),
            productPriceHistoryDao = db.productPriceHistoryDao(),
            productFrequencyDao = db.productFrequencyDao(),
            productHistoryDao = db.productHistoryDao(),
            articuloSupermarketDefaultDao = db.articuloSupermarketDefaultDao(),
            categorySupermarketOrderDao = db.categorySupermarketOrderDao(),
            categoryDao = db.categoryDao(),
            supermarketDao = db.supermarketDao(),
            offerDao = db.offerDao()
        )()

        val backup = Gson().fromJson(json, UserDataBackup::class.java)

        assertEquals(listOf(1L, 50L), backup.supermarkets.map { it.id }.sorted())
        assertEquals(listOf(1L, 50L), backup.categories.map { it.id }.sorted())
        assertEquals(listOf(1L, 50L), backup.offers.map { it.id }.sorted())
        assertEquals(listOf(1L, 50L), backup.aisles.map { it.id }.sorted())
        assertEquals(1, backup.articulos.size)
        assertEquals(1, backup.listas.size)
        assertEquals(1, backup.productos.size)
        assertEquals(1, backup.tickets.size)
        assertEquals(1, backup.ticketLines.size)
        assertEquals(1, backup.purchaseHistory.size)
        assertEquals(1, backup.productPriceHistory.size)
        assertEquals(1, backup.productFrequency.size)
        assertEquals(1, backup.productHistory.size)
        assertEquals(1, backup.articuloSupermarketDefaults.size)
        assertEquals(1, backup.categorySupermarketOrders.size)

        assertFalse(json.contains("pdfPath"))
        assertFalse(json.contains("ticketUrl"))
    }

    @Test
    fun import_roundtrip_restores_all_blocks_and_replaces_previous_data() = runBlocking {
        val sourceDb = newDb()
        seedDataset(sourceDb)

        val json = ExportUserDataBackupUseCase(
            context = context,
            articuloDao = sourceDb.articuloDao(),
            shoppingListDao = sourceDb.shoppingListDao(),
            productDao = sourceDb.productDao(),
            aisleDao = sourceDb.aisleDao(),
            ticketDao = sourceDb.ticketDao(),
            purchaseHistoryDao = sourceDb.purchaseHistoryDao(),
            productPriceHistoryDao = sourceDb.productPriceHistoryDao(),
            productFrequencyDao = sourceDb.productFrequencyDao(),
            productHistoryDao = sourceDb.productHistoryDao(),
            articuloSupermarketDefaultDao = sourceDb.articuloSupermarketDefaultDao(),
            categorySupermarketOrderDao = sourceDb.categorySupermarketOrderDao(),
            categoryDao = sourceDb.categoryDao(),
            supermarketDao = sourceDb.supermarketDao(),
            offerDao = sourceDb.offerDao()
        )()

        val targetDb = newDb()
        targetDb.supermarketDao().insertSupermarket(SupermarketEntity(id = 999, name = "Basura previa", emoji = "🗑️", isDefault = false))
        targetDb.categoryDao().insertCategory(CategoryEntity(id = 999, name = "Basura previa", icon = "🗑️"))

        ImportUserDataBackupUseCase(
            database = targetDb,
            articuloDao = targetDb.articuloDao(),
            shoppingListDao = targetDb.shoppingListDao(),
            productDao = targetDb.productDao(),
            aisleDao = targetDb.aisleDao(),
            ticketDao = targetDb.ticketDao(),
            purchaseHistoryDao = targetDb.purchaseHistoryDao(),
            productPriceHistoryDao = targetDb.productPriceHistoryDao(),
            productFrequencyDao = targetDb.productFrequencyDao(),
            productHistoryDao = targetDb.productHistoryDao(),
            articuloSupermarketDefaultDao = targetDb.articuloSupermarketDefaultDao(),
            categorySupermarketOrderDao = targetDb.categorySupermarketOrderDao(),
            categoryDao = targetDb.categoryDao(),
            supermarketDao = targetDb.supermarketDao(),
            offerDao = targetDb.offerDao()
        )(json)

        assertEquals(listOf(1L, 50L), targetDb.supermarketDao().getAllSupermarketsOnce().map { it.id }.sorted())
        assertEquals(listOf(1L, 50L), targetDb.categoryDao().getAllCategoriesOnce().map { it.id }.sorted())
        assertEquals(listOf(1L, 50L), targetDb.offerDao().getAllOffers().map { it.id }.sorted())
        assertEquals(listOf(1L, 50L), targetDb.aisleDao().getAllAisles().map { it.id }.sorted())
        assertEquals(listOf(100L), targetDb.articuloDao().getAllArticulosOnce().map { it.id })
        assertEquals(listOf(200L), targetDb.shoppingListDao().getAllLists().map { it.id })
        assertEquals(listOf(300L), targetDb.productDao().getAllProductsOnce().map { it.id })
        assertEquals(1, targetDb.ticketDao().getAllTicketsWithLinesOnce().size)
        assertEquals(1, targetDb.purchaseHistoryDao().getAllPurchases().size)
        assertEquals(1, targetDb.productPriceHistoryDao().getAll().size)
        assertEquals(1, targetDb.productFrequencyDao().getAllFrequencies().size)
        assertEquals(1, targetDb.productHistoryDao().getAll().size)
        assertEquals(1, targetDb.articuloSupermarketDefaultDao().getAllOnce().size)
        assertEquals(1, targetDb.categorySupermarketOrderDao().getAllOnce().size)

        val importedTicket = targetDb.ticketDao().getAllTicketsWithLinesOnce().single().ticket
        val importedPurchase = targetDb.purchaseHistoryDao().getAllPurchases().single()

        assertNull(importedTicket.pdfPath)
        assertNull(importedPurchase.ticketUrl)
    }

    @Test
    fun import_roundtrip_preserves_cross_table_relationships() = runBlocking {
        val sourceDb = newDb()
        seedDataset(sourceDb)

        val json = ExportUserDataBackupUseCase(
            context = context,
            articuloDao = sourceDb.articuloDao(),
            shoppingListDao = sourceDb.shoppingListDao(),
            productDao = sourceDb.productDao(),
            aisleDao = sourceDb.aisleDao(),
            ticketDao = sourceDb.ticketDao(),
            purchaseHistoryDao = sourceDb.purchaseHistoryDao(),
            productPriceHistoryDao = sourceDb.productPriceHistoryDao(),
            productFrequencyDao = sourceDb.productFrequencyDao(),
            productHistoryDao = sourceDb.productHistoryDao(),
            articuloSupermarketDefaultDao = sourceDb.articuloSupermarketDefaultDao(),
            categorySupermarketOrderDao = sourceDb.categorySupermarketOrderDao(),
            categoryDao = sourceDb.categoryDao(),
            supermarketDao = sourceDb.supermarketDao(),
            offerDao = sourceDb.offerDao()
        )()

        val targetDb = newDb()

        ImportUserDataBackupUseCase(
            database = targetDb,
            articuloDao = targetDb.articuloDao(),
            shoppingListDao = targetDb.shoppingListDao(),
            productDao = targetDb.productDao(),
            aisleDao = targetDb.aisleDao(),
            ticketDao = targetDb.ticketDao(),
            purchaseHistoryDao = targetDb.purchaseHistoryDao(),
            productPriceHistoryDao = targetDb.productPriceHistoryDao(),
            productFrequencyDao = targetDb.productFrequencyDao(),
            productHistoryDao = targetDb.productHistoryDao(),
            articuloSupermarketDefaultDao = targetDb.articuloSupermarketDefaultDao(),
            categorySupermarketOrderDao = targetDb.categorySupermarketOrderDao(),
            categoryDao = targetDb.categoryDao(),
            supermarketDao = targetDb.supermarketDao(),
            offerDao = targetDb.offerDao()
        )(json)

        val product = targetDb.productDao().getAllProductsOnce().single()
        assertEquals(200L, product.shoppingListId)
        assertEquals(100L, product.articuloId)
        assertEquals(50L, product.supermarketId)
        assertEquals(50L, product.aisleId)
        assertEquals(50L, product.offerId)

        val ticketWithLines = targetDb.ticketDao().getAllTicketsWithLinesOnce().single()
        val line = ticketWithLines.lines.single()
        assertEquals(400L, ticketWithLines.ticket.id)
        assertEquals(400L, line.ticketId)
        assertEquals(100L, line.articuloId)
        assertEquals(50L, line.categoriaId)

        val priceHistory = targetDb.productPriceHistoryDao().getAll().single()
        assertEquals(500L, priceHistory.purchaseId)

        val frequency = targetDb.productFrequencyDao().getAllFrequencies().single()
        assertEquals(50L, frequency.lastAisleId)
        assertEquals(50L, frequency.preferredAisleId)
        assertEquals(50L, frequency.lastSupermarketId)

        val productHistory = targetDb.productHistoryDao().getAll().single()
        assertEquals(50L, productHistory.aisleId)

        val articuloDefault = targetDb.articuloSupermarketDefaultDao().getAllOnce().single()
        assertEquals(100L, articuloDefault.articuloId)
        assertEquals(50L, articuloDefault.supermarketId)
        assertEquals(50L, articuloDefault.aisleId)

        val categoryOrder = targetDb.categorySupermarketOrderDao().getAllOnce().single()
        assertEquals(50L, categoryOrder.categoryId)
        assertEquals(50L, categoryOrder.supermarketId)
    }

    private fun newDb(): ShoppingListDatabase {
        val db = Room.inMemoryDatabaseBuilder(
            context,
            ShoppingListDatabase::class.java
        ).allowMainThreadQueries().build()
        openedDbs += db
        return db
    }

    private suspend fun seedDataset(db: ShoppingListDatabase) {
        db.supermarketDao().insertAll(
            listOf(
                SupermarketEntity(id = 1, name = "Base Market", emoji = "🏪", isDefault = true),
                SupermarketEntity(id = 50, name = "Mi Súper", emoji = "🛒", isDefault = false)
            )
        )

        db.categoryDao().insertAll(
            listOf(
                CategoryEntity(id = 1, name = "Base Category", icon = "📦"),
                CategoryEntity(id = 50, name = "Categoría Real", icon = "🥦")
            )
        )

        db.offerDao().insertAll(
            listOf(
                OfferEntity(id = 1, code = "3x2", name = "3x2", description = "Oferta base", isDefault = true, formula = "3x2"),
                OfferEntity(id = 50, code = "custom", name = "Oferta real", description = "Oferta del usuario", isDefault = false, formula = "custom")
            )
        )

        db.aisleDao().insertAll(
            listOf(
                AisleEntity(id = 1, name = "Base Aisle", emoji = "📍", orderIndex = 0, supermarketId = 1, isDefault = true),
                AisleEntity(id = 50, name = "Pasillo real", emoji = "🥬", orderIndex = 1, supermarketId = 50, isDefault = false)
            )
        )

        db.articuloDao().insertAll(
            listOf(
                ArticuloEntity(
                    id = 100,
                    name = "Tomate pera",
                    basePrice = 1.95f,
                    photoUri = "content://articulo/tomate",
                    ean = "1234567890123",
                    categoryId = 50,
                    size = 1f,
                    unit = "kg"
                )
            )
        )

        db.shoppingListDao().insertAll(
            listOf(
                ShoppingListEntity(
                    id = 200,
                    name = "Lista real",
                    supermarketId = 50,
                    fechaCreacion = 1_700_000_000_000,
                    estado = "ACTIVA"
                )
            )
        )

        db.productDao().insertAll(
            listOf(
                ProductEntity(
                    id = 300,
                    name = "Tomate pera",
                    aisleId = 50,
                    shoppingListId = 200,
                    articuloId = 100,
                    supermarketId = 50,
                    quantity = 2f,
                    estimatedPrice = 2.20f,
                    offerId = 50,
                    finalPrice = 1.95f,
                    isPurchased = false,
                    notes = "Bio",
                    orderIndex = 0,
                    photoUri = "content://producto/tomate",
                    ean = "1234567890123"
                )
            )
        )

        db.ticketDao().insertTicketWithLinesPreservingIds(
            ticket = TicketEntity(
                id = 400,
                fecha = 1_700_000_100_000,
                supermarketId = 50,
                supermarketName = "Mi Súper",
                total = 12.34f,
                subtotal = 13.00f,
                descuentos = 0.66f,
                numProductos = 1,
                socioClub = "9999",
                formaPago = "Tarjeta",
                pdfPath = "content://tickets/original.pdf",
                importado = true,
                createdAt = 1_700_000_100_100
            ),
            lines = listOf(
                TicketLineEntity(
                    id = 401,
                    ticketId = 400,
                    nombreOriginal = "TOMATE PERA",
                    nombreNormalizado = "tomate pera",
                    cantidad = 2,
                    precioUnitario = 0.975f,
                    precioTotal = 1.95f,
                    articuloId = 100,
                    categoriaId = 50,
                    esDescuento = false,
                    codigoPromocion = null,
                    notas = "OK",
                    confirmado = true
                )
            )
        )

        db.purchaseHistoryDao().insertAll(
            listOf(
                PurchaseHistoryEntity(
                    id = 500,
                    fecha = 1_700_000_100_000,
                    total = 12.34f,
                    tienda = "Mi Súper",
                    numProductos = 1,
                    ahorroTotal = 0.66f,
                    ticketUrl = "content://tickets/original.pdf"
                )
            )
        )

        db.productPriceHistoryDao().insertAll(
            listOf(
                ProductPriceHistoryEntity(
                    id = 600,
                    purchaseId = 500,
                    productName = "tomate pera",
                    price = 1.95f,
                    quantity = 2,
                    aisle = "Pasillo real",
                    fecha = 1_700_000_100_000
                )
            )
        )

        db.productFrequencyDao().insertOrUpdateFrequency(
            ProductFrequencyEntity(
                id = 700,
                productName = "tomate pera",
                originalName = "Tomate pera",
                timesPurchased = 3,
                lastPurchaseDate = 1_700_000_100_000,
                averageDaysBetween = 7f,
                estimatedNextDate = 1_700_604_800_000,
                category = "Categoría Real",
                lastAisleId = 50,
                lastQuantity = 2f,
                lastPrice = 1.95f,
                lastSupermarketId = 50,
                preferredAisleId = 50
            )
        )

        db.productHistoryDao().insertAll(
            listOf(
                ProductHistoryEntity(
                    id = 800,
                    name = "tomate pera",
                    originalName = "Tomate pera",
                    aisleId = 50,
                    lastQuantity = 2f,
                    lastPrice = 1.95f,
                    usageCount = 4,
                    lastUsed = 1_700_000_100_000
                )
            )
        )

        db.articuloSupermarketDefaultDao().insertAll(
            listOf(
                ArticuloSupermarketDefaultEntity(
                    id = 900,
                    articuloId = 100,
                    supermarketId = 50,
                    aisleId = 50
                )
            )
        )

        db.categorySupermarketOrderDao().insertAll(
            listOf(
                CategorySupermarketOrderEntity(
                    id = 1000,
                    categoryId = 50,
                    supermarketId = 50,
                    orderIndex = 1
                )
            )
        )
    }
}
