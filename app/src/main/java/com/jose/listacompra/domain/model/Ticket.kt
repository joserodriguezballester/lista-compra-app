package com.jose.listacompra.domain.model

import java.util.Date

/**
 * Ticket importado desde PDF/imagen de supermercado.
 */
data class Ticket(
    val id: Long = 0,
    val fecha: Date,
    val supermarketId: Long?,
    val supermarketName: String?,
    val total: Float,
    val subtotal: Float?,
    val descuentos: Float?,
    val numProductos: Int,
    val socioClub: String?,
    val formaPago: String?,
    val pdfPath: String?,
    val importado: Boolean = false,
    val lines: List<TicketLine> = emptyList()
)

/**
 * Línea de producto de un ticket.
 */
data class TicketLine(
    val id: Long = 0,
    val ticketId: Long,
    val nombreOriginal: String,
    val nombreNormalizado: String,
    val cantidad: Int,
    val precioUnitario: Float,
    val precioTotal: Float,
    val articuloId: Long? = null,
    val articuloNombre: String? = null, // Para UI
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null, // Para UI
    val esDescuento: Boolean = false,
    val codigoPromocion: String? = null,
    val notas: String? = null,
    val confirmado: Boolean = false
)
