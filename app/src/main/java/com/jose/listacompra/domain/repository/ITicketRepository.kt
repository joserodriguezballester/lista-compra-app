package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestión de tickets importados.
 */
interface ITicketRepository {

    // ========== TICKETS ==========

    /**
     * Obtiene todos los tickets ordenados por fecha descendente.
     */
    fun getAllTickets(): Flow<List<Ticket>>

    /**
     * Obtiene un ticket con sus líneas.
     */
    suspend fun getTicketWithLines(ticketId: Long): Ticket?

    /**
     * Obtiene tickets pendientes de importar.
     */
    fun getPendingTickets(): Flow<List<Ticket>>

    /**
     * Guarda un nuevo ticket con sus líneas.
     * @return ID del ticket creado.
     */
    suspend fun saveTicket(ticket: Ticket): Long

    /**
     * Actualiza un ticket existente.
     */
    suspend fun updateTicket(ticket: Ticket)

    /**
     * Elimina un ticket y sus líneas.
     */
    suspend fun deleteTicket(ticketId: Long)

    // ========== TICKET LINES ==========

    /**
     * Obtiene las líneas de un ticket.
     */
    suspend fun getLinesForTicket(ticketId: Long): List<TicketLine>

    /**
     * Obtiene líneas sin matchear (sin artículo asignado).
     */
    suspend fun getUnmatchedLines(ticketId: Long): List<TicketLine>

    /**
     * Confirma el match de una línea con un artículo.
     */
    suspend fun confirmMatch(lineId: Long, articuloId: Long)

    /**
     * Asigna categoría a una línea.
     */
    suspend fun setCategory(lineId: Long, categoriaId: Long)

    /**
     * Actualiza una línea.
     */
    suspend fun updateLine(line: TicketLine)

    // ========== AGGREGATES ==========

    /**
     * Total gastado en un período.
     */
    suspend fun getTotalSpent(startDate: Long, endDate: Long): Float

    /**
     * Número total de tickets.
     */
    suspend fun getTicketCount(): Int
}
