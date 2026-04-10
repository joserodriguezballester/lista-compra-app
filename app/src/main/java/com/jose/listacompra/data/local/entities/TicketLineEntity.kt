package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Línea de producto de un ticket importado.
 * Cada línea representa un producto comprado en el ticket.
 */
@Entity(
    tableName = "ticket_lines",
    foreignKeys = [
        ForeignKey(
            entity = TicketEntity::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = ArticuloEntity::class,
            parentColumns = ["id"],
            childColumns = ["articuloId"],
            onDelete = ForeignKey.Companion.SET_NULL
        )
    ],
    indices = [Index("ticketId"), Index("articuloId"), Index("nombreNormalizado")]
)
data class TicketLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,                       // ID del ticket padre
    val nombreOriginal: String,               // Nombre exacto del ticket
    val nombreNormalizado: String,            // Nombre normalizado para matching
    val cantidad: Int,                        // Cantidad comprada
    val precioUnitario: Float,                // Precio por unidad
    val precioTotal: Float,                   // Precio total de la línea
    val articuloId: Long? = null,             // ID del artículo matcheado (null si no hay)
    val categoriaId: Long? = null,            // Categoría asignada automáticamente
    val esDescuento: Boolean = false,         // Si es una línea de descuento
    val codigoPromocion: String? = null,      // Código de promoción si existe
    val notas: String? = null,                // Notas adicionales
    val confirmado: Boolean = false           // Si el usuario confirmó el match
)
