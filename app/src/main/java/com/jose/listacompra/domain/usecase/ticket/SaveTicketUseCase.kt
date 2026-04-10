package com.jose.listacompra.domain.usecase.ticket

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.repository.ITicketRepository
import javax.inject.Inject

/**
 * Guarda un nuevo ticket importado.
 */
class SaveTicketUseCase @Inject constructor(
    private val ticketRepository: ITicketRepository
) {
    suspend operator fun invoke(ticket: Ticket): Long {
        return ticketRepository.saveTicket(ticket)
    }
}
