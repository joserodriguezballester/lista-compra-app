package com.jose.listacompra.data.local.dao

import androidx.room.*
import com.jose.listacompra.data.local.entities.TicketEntity
import com.jose.listacompra.data.local.entities.TicketLineEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestión de tickets importados y sus líneas.
 */
@Dao
interface TicketDao {

    // ========== TICKETS ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)

    @Query("SELECT * FROM tickets ORDER BY fecha DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    suspend fun getTicketById(ticketId: Long): TicketEntity?

    @Query("SELECT * FROM tickets WHERE importado = 0 ORDER BY createdAt DESC")
    fun getPendingTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE supermarketId = :supermarketId ORDER BY fecha DESC")
    fun getTicketsBySupermarket(supermarketId: Long): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE fecha BETWEEN :startTime AND :endTime ORDER BY fecha DESC")
    fun getTicketsBetween(startTime: Long, endTime: Long): Flow<List<TicketEntity>>

    // ========== TICKET LINES ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicketLine(line: TicketLineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicketLines(lines: List<TicketLineEntity>)

    @Update
    suspend fun updateTicketLine(line: TicketLineEntity)

    @Delete
    suspend fun deleteTicketLine(line: TicketLineEntity)

    @Query("SELECT * FROM ticket_lines WHERE ticketId = :ticketId ORDER BY id")
    suspend fun getLinesForTicket(ticketId: Long): List<TicketLineEntity>

    @Query("SELECT * FROM ticket_lines WHERE ticketId = :ticketId AND articuloId IS NULL")
    suspend fun getUnmatchedLines(ticketId: Long): List<TicketLineEntity>

    @Query("SELECT * FROM ticket_lines WHERE articuloId = :articuloId ORDER BY ticketId DESC")
    fun getLinesForArticulo(articuloId: Long): Flow<List<TicketLineEntity>>

    @Query("UPDATE ticket_lines SET articuloId = :articuloId, confirmado = 1 WHERE id = :lineId")
    suspend fun confirmMatch(lineId: Long, articuloId: Long)

    @Query("UPDATE ticket_lines SET categoriaId = :categoriaId WHERE id = :lineId")
    suspend fun setCategory(lineId: Long, categoriaId: Long)

    // ========== AGGREGATES ==========

    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun getTicketCount(): Int

    @Query("SELECT SUM(total) FROM tickets WHERE fecha BETWEEN :startTime AND :endTime")
    suspend fun getTotalSpentBetween(startTime: Long, endTime: Long): Float?

    @Query("SELECT AVG(total) FROM tickets")
    suspend fun getAverageTicketTotal(): Float?

    @Transaction
    suspend fun insertTicketWithLines(ticket: TicketEntity, lines: List<TicketLineEntity>): Long {
        val ticketId = insertTicket(ticket)
        val linesWithTicketId = lines.map { it.copy(ticketId = ticketId) }
        insertTicketLines(linesWithTicketId)
        return ticketId
    }

    @Transaction
    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    suspend fun getTicketWithLines(ticketId: Long): TicketWithLines?

    @Transaction
    @Query("SELECT * FROM tickets ORDER BY fecha DESC")
    fun getAllTicketsWithLines(): Flow<List<TicketWithLines>>
}

/**
 * Ticket con sus líneas.
 */
data class TicketWithLines(
    @Embedded val ticket: TicketEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ticketId"
    )
    val lines: List<TicketLineEntity>
)
