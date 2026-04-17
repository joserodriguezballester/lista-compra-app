package com.jose.listacompra

import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.SpendingStats
import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import com.jose.listacompra.domain.repository.IHistoryRepository
import com.jose.listacompra.domain.repository.ITicketRepository
import com.jose.listacompra.domain.usecase.history.CompletePurchaseUseCase
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import com.jose.listacompra.domain.usecase.ticket.SaveTicketUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class SaveTicketUseCaseTest {

    @Test
    fun `saveTicket uses ticket date and line unit price for matched products`() = runBlocking {
        val ticketRepository = FakeTicketRepository()
        val historyRepository = FakeHistoryRepository()
        val useCase = buildUseCase(ticketRepository, historyRepository)

        val ticketDate = 1_710_000_123_000L
        val ticket = Ticket(
            fecha = Date(ticketDate),
            supermarketId = 7L,
            supermarketName = "Carrefour Campanar",
            total = 5.85f,
            subtotal = 6.10f,
            descuentos = 0.25f,
            numProductos = 3,
            socioClub = null,
            formaPago = null,
            pdfPath = "/tickets/demo.pdf",
            importado = true,
            lines = listOf(
                matchedLine(
                    nombreOriginal = "LECHE ENTERA",
                    nombreNormalizado = "leche entera",
                    cantidad = 2,
                    precioUnitario = 1.35f,
                    precioTotal = 2.70f,
                    articuloId = 100L,
                    articuloNombre = "Leche entera"
                ),
                unmatchedLine(
                    nombreOriginal = "PAN",
                    nombreNormalizado = "pan",
                    cantidad = 1,
                    precioUnitario = 0.99f,
                    precioTotal = 0.99f
                ),
                discountLine(
                    nombreOriginal = "DTO CLUB",
                    nombreNormalizado = "dto club",
                    precioUnitario = -0.25f,
                    articuloId = 200L,
                    articuloNombre = "Descuento club"
                )
            )
        )

        val savedTicketId = useCase(ticket)

        assertEquals(999L, savedTicketId)
        assertEquals(1, historyRepository.purchaseHistoryRecords.size)
        assertEquals(1, historyRepository.priceHistoryRecords.size)
        assertEquals(1, historyRepository.frequencyRecords.size)

        val purchase = historyRepository.purchaseHistoryRecords.single()
        assertEquals(ticketDate, purchase.fecha)
        assertEquals(5.85f, purchase.total)
        assertEquals("Carrefour Campanar", purchase.tienda)
        assertEquals(3, purchase.numProductos)
        assertEquals(0.25f, purchase.ahorroTotal)
        assertEquals("/tickets/demo.pdf", purchase.ticketUrl)

        val priceHistory = historyRepository.priceHistoryRecords.single()
        assertEquals(321L, priceHistory.purchaseId)
        assertEquals("leche entera", priceHistory.productName)
        assertEquals(1.35f, priceHistory.price)
        assertEquals(2, priceHistory.quantity)
        assertEquals(ticketDate, priceHistory.fecha)

        val frequency = historyRepository.frequencyRecords.values.single()
        assertEquals("leche entera", frequency.productName)
        assertEquals("Leche entera", frequency.originalName)
        assertEquals(1, frequency.timesPurchased)
        assertEquals(2f, frequency.lastQuantity)
        assertEquals(1.35f, frequency.lastPrice)
        assertEquals(7L, frequency.lastSupermarketId)
        assertEquals(ticketDate, frequency.lastPurchaseDate)

        assertNull(historyRepository.getFrequency("pan"))
        assertNotNull(ticketRepository.savedTicket)
    }

    @Test
    fun `saveTicket stores one history entry per matched line`() = runBlocking {
        val ticketDate = 1_720_000_000_000L

        val historyRepository = FakeHistoryRepository()
        val localUseCase = buildUseCase(historyRepository = historyRepository)

        localUseCase(
            Ticket(
                fecha = Date(ticketDate),
                supermarketId = 11L,
                supermarketName = "Mercadona",
                total = 7.10f,
                subtotal = 7.10f,
                descuentos = 0f,
                numProductos = 2,
                socioClub = null,
                formaPago = null,
                pdfPath = null,
                importado = true,
                lines = listOf(
                    matchedLine(
                        nombreOriginal = "ARROZ REDONDO",
                        nombreNormalizado = "arroz redondo",
                        cantidad = 1,
                        precioUnitario = 1.20f,
                        precioTotal = 1.20f,
                        articuloId = 300L,
                        articuloNombre = "Arroz redondo"
                    ),
                    matchedLine(
                        nombreOriginal = "HUEVOS L",
                        nombreNormalizado = "huevos l",
                        cantidad = 2,
                        precioUnitario = 2.95f,
                        precioTotal = 5.90f,
                        articuloId = 301L,
                        articuloNombre = "Huevos L"
                    )
                )
            )
        )

        assertEquals(1, historyRepository.purchaseHistoryRecords.size)
        assertEquals(2, historyRepository.priceHistoryRecords.size)
        assertEquals(2, historyRepository.frequencyRecords.size)

        val first = historyRepository.priceHistoryRecords[0]
        val second = historyRepository.priceHistoryRecords[1]
        assertEquals(321L, first.purchaseId)
        assertEquals(321L, second.purchaseId)
        assertEquals(ticketDate, first.fecha)
        assertEquals(ticketDate, second.fecha)
        assertEquals("arroz redondo", first.productName)
        assertEquals("huevos l", second.productName)
    }

    @Test
    fun `saveTicket falls back to original name when article name is blank`() = runBlocking {
        val historyRepository = FakeHistoryRepository()
        val useCase = buildUseCase(historyRepository = historyRepository)
        val ticketDate = 1_730_000_000_000L

        useCase(
            Ticket(
                fecha = Date(ticketDate),
                supermarketId = 5L,
                supermarketName = null,
                total = 1.49f,
                subtotal = 1.49f,
                descuentos = 0f,
                numProductos = 1,
                socioClub = null,
                formaPago = null,
                pdfPath = null,
                importado = true,
                lines = listOf(
                    matchedLine(
                        nombreOriginal = "YOGUR NATURAL",
                        nombreNormalizado = "yogur natural",
                        cantidad = 1,
                        precioUnitario = 1.49f,
                        precioTotal = 1.49f,
                        articuloId = 400L,
                        articuloNombre = "   "
                    )
                )
            )
        )

        val priceHistory = historyRepository.priceHistoryRecords.single()
        assertEquals("yogur natural", priceHistory.productName)

        val frequency = historyRepository.frequencyRecords.values.single()
        assertEquals("yogur natural", frequency.productName)
        assertEquals("YOGUR NATURAL", frequency.originalName)

        val purchase = historyRepository.purchaseHistoryRecords.single()
        assertEquals("Carrefour", purchase.tienda)
    }

    @Test
    fun `saveTicket updates existing frequency instead of resetting it`() = runBlocking {
        val historyRepository = FakeHistoryRepository().apply {
            frequencyRecords["leche entera"] = ProductFrequencyEntity(
                productName = "leche entera",
                originalName = "Leche entera",
                timesPurchased = 4,
                lastQuantity = 1f,
                lastPrice = 1.10f,
                lastSupermarketId = 2L,
                lastPurchaseDate = 1_700_000_000_000L
            )
        }
        val useCase = buildUseCase(historyRepository = historyRepository)
        val ticketDate = 1_740_000_000_000L

        useCase(
            Ticket(
                fecha = Date(ticketDate),
                supermarketId = 9L,
                supermarketName = "Consum",
                total = 2.40f,
                subtotal = 2.40f,
                descuentos = 0f,
                numProductos = 1,
                socioClub = null,
                formaPago = null,
                pdfPath = null,
                importado = true,
                lines = listOf(
                    matchedLine(
                        nombreOriginal = "LECHE ENTERA",
                        nombreNormalizado = "leche entera",
                        cantidad = 2,
                        precioUnitario = 1.20f,
                        precioTotal = 2.40f,
                        articuloId = 500L,
                        articuloNombre = "Leche entera"
                    )
                )
            )
        )

        val frequency = historyRepository.frequencyRecords.getValue("leche entera")
        assertEquals(5, frequency.timesPurchased)
        assertEquals(2f, frequency.lastQuantity)
        assertEquals(1.20f, frequency.lastPrice)
        assertEquals(9L, frequency.lastSupermarketId)
        assertEquals(ticketDate, frequency.lastPurchaseDate)
    }


    private fun buildUseCase(
        ticketRepository: FakeTicketRepository = FakeTicketRepository(),
        historyRepository: FakeHistoryRepository = FakeHistoryRepository()
    ) = SaveTicketUseCase(
        ticketRepository = ticketRepository,
        historyRepository = historyRepository,
        completePurchaseUseCase = CompletePurchaseUseCase(historyRepository),
        savePriceHistoryUseCase = SavePriceHistoryUseCase(historyRepository)
    )

    private fun matchedLine(
        nombreOriginal: String,
        nombreNormalizado: String,
        cantidad: Int,
        precioUnitario: Float,
        precioTotal: Float,
        articuloId: Long,
        articuloNombre: String?
    ) = TicketLine(
        ticketId = 0,
        nombreOriginal = nombreOriginal,
        nombreNormalizado = nombreNormalizado,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        precioTotal = precioTotal,
        articuloId = articuloId,
        articuloNombre = articuloNombre,
        confirmado = true
    )

    private fun unmatchedLine(
        nombreOriginal: String,
        nombreNormalizado: String,
        cantidad: Int,
        precioUnitario: Float,
        precioTotal: Float
    ) = TicketLine(
        ticketId = 0,
        nombreOriginal = nombreOriginal,
        nombreNormalizado = nombreNormalizado,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        precioTotal = precioTotal,
        articuloId = null,
        articuloNombre = null,
        confirmado = false
    )

    private fun discountLine(
        nombreOriginal: String,
        nombreNormalizado: String,
        precioUnitario: Float,
        articuloId: Long,
        articuloNombre: String
    ) = TicketLine(
        ticketId = 0,
        nombreOriginal = nombreOriginal,
        nombreNormalizado = nombreNormalizado,
        cantidad = 1,
        precioUnitario = precioUnitario,
        precioTotal = precioUnitario,
        articuloId = articuloId,
        articuloNombre = articuloNombre,
        esDescuento = true,
        confirmado = true
    )

    private class FakeTicketRepository : ITicketRepository {
        var savedTicket: Ticket? = null

        override fun getAllTickets() = throw UnsupportedOperationException()
        override suspend fun getTicketWithLines(ticketId: Long): Ticket? = throw UnsupportedOperationException()
        override fun getPendingTickets() = throw UnsupportedOperationException()
        override suspend fun saveTicket(ticket: Ticket): Long {
            savedTicket = ticket
            return 999L
        }
        override suspend fun updateTicket(ticket: Ticket) = throw UnsupportedOperationException()
        override suspend fun deleteTicket(ticketId: Long) = throw UnsupportedOperationException()
        override suspend fun getLinesForTicket(ticketId: Long): List<TicketLine> = throw UnsupportedOperationException()
        override suspend fun getUnmatchedLines(ticketId: Long): List<TicketLine> = throw UnsupportedOperationException()
        override suspend fun confirmMatch(lineId: Long, articuloId: Long) = throw UnsupportedOperationException()
        override suspend fun setCategory(lineId: Long, categoriaId: Long) = throw UnsupportedOperationException()
        override suspend fun updateLine(line: TicketLine) = throw UnsupportedOperationException()
        override suspend fun getTotalSpent(startDate: Long, endDate: Long): Float = throw UnsupportedOperationException()
        override suspend fun getTicketCount(): Int = throw UnsupportedOperationException()
    }

    private class FakeHistoryRepository : IHistoryRepository {
        val purchaseHistoryRecords = mutableListOf<PurchaseHistoryEntity>()
        val priceHistoryRecords = mutableListOf<ProductPriceHistoryEntity>()
        val frequencyRecords = mutableMapOf<String, ProductFrequencyEntity>()

        override suspend fun getFrequency(productName: String): ProductFrequencyEntity? {
            return frequencyRecords[productName]
        }

        override suspend fun updateFrequency(entity: ProductFrequencyEntity) {
            frequencyRecords[entity.productName] = entity
        }

        override suspend fun insertFrequency(entity: ProductFrequencyEntity) {
            frequencyRecords[entity.productName] = entity
        }

        override suspend fun getAllFrequencies(): List<ProductFrequencyEntity> {
            return frequencyRecords.values.toList()
        }

        override suspend fun getPriceHistory(productName: String): List<ProductPriceHistoryEntity> {
            return priceHistoryRecords.filter { it.productName == productName }
        }

        override suspend fun getPriceStats(productName: String): PriceStats? = null

        override suspend fun savePriceHistory(priceHistory: ProductPriceHistoryEntity) {
            priceHistoryRecords += priceHistory
        }

        override suspend fun insertPurchaseHistory(purchaseHistory: PurchaseHistoryEntity): Long {
            purchaseHistoryRecords += purchaseHistory
            return 321L
        }

        override suspend fun getProductSuggestions(query: String): List<ProductFrequencyEntity> = emptyList()

        override suspend fun getSpendingStats(): SpendingStats {
            return SpendingStats(0f, 0f, 0)
        }
    }
}
