package com.jose.listacompra.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ticket importado desde PDF/imagen de supermercado.
 * Almacena la información completa de un ticket escaneado.
 */
@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = SupermarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["supermarketId"],
            onDelete = ForeignKey.Companion.SET_NULL
        )
    ],
    indices = [Index("supermarketId"), Index("fecha")]
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long,                          // Fecha del ticket (epoch millis)
    val supermarketId: Long?,                 // ID del supermercado (null si no identificado)
    val supermarketName: String?,             // Nombre original del ticket
    val total: Float,                         // Total pagado
    val subtotal: Float?,                     // Subtotal antes de descuentos
    val descuentos: Float?,                   // Total descuentos
    val numProductos: Int,                    // Número de productos
    val socioClub: String?,                   // Número de socio si existe
    val formaPago: String?,                   // Forma de pago
    val pdfPath: String?,                     // Ruta al archivo PDF original
    val importado: Boolean = false,           // Si se ha importado completamente
    val createdAt: Long = System.currentTimeMillis()
)
