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
        val useCase = SaveTicketUseCase(
            ticketRepository = ticketRepository,
            historyRepository = historyRepository,
            completePurchaseUseCase = CompletePurchaseUseCase(historyRepository),
            savePriceHistoryUseCase = SavePriceHistoryUseCase(historyRepository)
        )

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
                TicketLine(
                    ticketId = 0,
                    nombreOriginal = "LECHE ENTERA",
                    nombreNormalizado = "leche entera",
                    cantidad = 2,
                    precioUnitario = 1.35f,
                    precioTotal = 2.70f,
                    articuloId = 100L,
                    articuloNombre = "Leche entera",
                    confirmado = true
                ),
                TicketLine(
                    ticketId = 0,
                    nombreOriginal = "PAN",
                    nombreNormalizado = "pan",
                    cantidad = 1,
                    precioUnitario = 0.99f,
                    precioTotal = 0.99f,
                    articuloId = null,
                    articuloNombre = null,
                    confirmado = false
                ),
                TicketLine(
                    ticketId = 0,
                    nombreOriginal = "DTO CLUB",
                    nombreNormalizado = "dto club",
                    cantidad = 1,
                    precioUnitario = -0.25f,
                    precioTotal = -0.25f,
                    articuloId = 200L,
                    articuloNombre = "Descuento club",
                    esDescuento = true,
                    confirmado = true
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
