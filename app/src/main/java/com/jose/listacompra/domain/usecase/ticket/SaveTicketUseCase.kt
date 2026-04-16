package com.jose.listacompra.domain.usecase.ticket

import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.repository.IHistoryRepository
import com.jose.listacompra.domain.repository.ITicketRepository
import com.jose.listacompra.domain.usecase.history.CompletePurchaseUseCase
import com.jose.listacompra.domain.usecase.history.ProductPurchaseData
import com.jose.listacompra.domain.usecase.history.SavePriceHistoryUseCase
import javax.inject.Inject

/**
 * Guarda un nuevo ticket importado.
 */
class SaveTicketUseCase @Inject constructor(
    private val ticketRepository: ITicketRepository,
    private val historyRepository: IHistoryRepository,
    private val completePurchaseUseCase: CompletePurchaseUseCase,
    private val savePriceHistoryUseCase: SavePriceHistoryUseCase
) {
    suspend operator fun invoke(ticket: Ticket): Long {
        val ticketId = ticketRepository.saveTicket(ticket)
        val purchaseDate = ticket.fecha.time

        val matchedLines = ticket.lines.filter { line ->
            !line.esDescuento && line.articuloId != null
        }

        val purchaseHistoryId = historyRepository.insertPurchaseHistory(
            PurchaseHistoryEntity(
                fecha = purchaseDate,
                total = ticket.total,
                tienda = ticket.supermarketName ?: "Carrefour",
                numProductos = ticket.numProductos,
                ahorroTotal = ticket.descuentos ?: 0f,
                ticketUrl = ticket.pdfPath
            )
        )

        matchedLines.forEach { line ->
            val productName = line.articuloNombre?.takeIf { it.isNotBlank() }
                ?: line.nombreOriginal

            savePriceHistoryUseCase(
                productName = productName,
                price = line.precioUnitario,
                quantity = line.cantidad,
                purchaseId = purchaseHistoryId,
                purchaseDate = purchaseDate
            )
        }

        completePurchaseUseCase(
            products = matchedLines.map { line ->
                ProductPurchaseData(
                    name = line.articuloNombre?.takeIf { it.isNotBlank() } ?: line.nombreOriginal,
                    quantity = line.cantidad.toFloat(),
                    price = line.precioUnitario,
                    supermarketId = ticket.supermarketId
                )
            },
            purchaseDate = purchaseDate
        )

        return ticketId
    }
}
