package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.dao.TicketDao
import com.jose.listacompra.data.local.dao.TicketWithLines
import com.jose.listacompra.data.local.entities.TicketEntity
import com.jose.listacompra.data.local.entities.TicketLineEntity
import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import com.jose.listacompra.domain.repository.ITicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao
) : ITicketRepository {

    // ========== MAPPERS ==========

    private fun TicketEntity.toDomain(): Ticket = Ticket(
        id = id,
        fecha = Date(fecha),
        supermarketId = supermarketId,
        supermarketName = supermarketName,
        total = total,
        subtotal = subtotal,
        descuentos = descuentos,
        numProductos = numProductos,
        socioClub = socioClub,
        formaPago = formaPago,
        pdfPath = pdfPath,
        importado = importado
    )

    private fun Ticket.toEntity(): TicketEntity = TicketEntity(
        id = id,
        fecha = fecha.time,
        supermarketId = supermarketId,
        supermarketName = supermarketName,
        total = total,
        subtotal = subtotal,
        descuentos = descuentos,
        numProductos = numProductos,
        socioClub = socioClub,
        formaPago = formaPago,
        pdfPath = pdfPath,
        importado = importado
    )

    private fun TicketLineEntity.toDomain(): TicketLine = TicketLine(
        id = id,
        ticketId = ticketId,
        nombreOriginal = nombreOriginal,
        nombreNormalizado = nombreNormalizado,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        precioTotal = precioTotal,
        articuloId = articuloId,
        categoriaId = categoriaId,
        esDescuento = esDescuento,
        codigoPromocion = codigoPromocion,
        notas = notas,
        confirmado = confirmado
    )

    private fun TicketLine.toEntity(): TicketLineEntity = TicketLineEntity(
        id = id,
        ticketId = ticketId,
        nombreOriginal = nombreOriginal,
        nombreNormalizado = nombreNormalizado,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        precioTotal = precioTotal,
        articuloId = articuloId,
        categoriaId = categoriaId,
        esDescuento = esDescuento,
        codigoPromocion = codigoPromocion,
        notas = notas,
        confirmado = confirmado
    )

    private fun TicketWithLines.toDomain(): Ticket = Ticket(
        id = ticket.id,
        fecha = Date(ticket.fecha),
        supermarketId = ticket.supermarketId,
        supermarketName = ticket.supermarketName,
        total = ticket.total,
        subtotal = ticket.subtotal,
        descuentos = ticket.descuentos,
        numProductos = ticket.numProductos,
        socioClub = ticket.socioClub,
        formaPago = ticket.formaPago,
        pdfPath = ticket.pdfPath,
        importado = ticket.importado,
        lines = lines.map { it.toDomain() }
    )

    // ========== TICKETS ==========

    override fun getAllTickets(): Flow<List<Ticket>> {
        return ticketDao.getAllTickets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTicketWithLines(ticketId: Long): Ticket? {
        return ticketDao.getTicketWithLines(ticketId)?.toDomain()
    }

    override fun getPendingTickets(): Flow<List<Ticket>> {
        return ticketDao.getPendingTickets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTicket(ticket: Ticket): Long {
        val entity = ticket.toEntity()
        val lines = ticket.lines.map { it.toEntity() }
        return ticketDao.insertTicketWithLines(entity, lines)
    }

    override suspend fun updateTicket(ticket: Ticket) {
        ticketDao.updateTicket(ticket.toEntity())
    }

    override suspend fun deleteTicket(ticketId: Long) {
        val ticket = ticketDao.getTicketById(ticketId)
        if (ticket != null) {
            ticketDao.deleteTicket(ticket)
        }
    }

    // ========== TICKET LINES ==========

    override suspend fun getLinesForTicket(ticketId: Long): List<TicketLine> {
        return ticketDao.getLinesForTicket(ticketId).map { it.toDomain() }
    }

    override suspend fun getUnmatchedLines(ticketId: Long): List<TicketLine> {
        return ticketDao.getUnmatchedLines(ticketId).map { it.toDomain() }
    }

    override suspend fun confirmMatch(lineId: Long, articuloId: Long) {
        ticketDao.confirmMatch(lineId, articuloId)
    }

    override suspend fun setCategory(lineId: Long, categoriaId: Long) {
        ticketDao.setCategory(lineId, categoriaId)
    }

    override suspend fun updateLine(line: TicketLine) {
        ticketDao.updateTicketLine(line.toEntity())
    }

    // ========== AGGREGATES ==========

    override suspend fun getTotalSpent(startDate: Long, endDate: Long): Float {
        return ticketDao.getTotalSpentBetween(startDate, endDate) ?: 0f
    }

    override suspend fun getTicketCount(): Int {
        return ticketDao.getTicketCount()
    }
}
