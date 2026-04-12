package com.jose.listacompra.utils

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parser para tickets de Carrefour.
 * Soporta tickets donde los nombres de producto y los precios aparecen en bloques separados.
 */
object CarrefourTicketParser {

    private val priceOnlyPattern = Regex("""^-?\d+[,\.]\d{2}$""")
    private val pricePattern = Regex("""(\d+[,\.]\d{2})""")
    private val datePattern = Regex("""(\d{2}/\d{2}/\d{4})\s+(\d{2}:\d{2}:\d{2})""")
    private val socioPattern = Regex("""SOCIO\s*CLUB.*?:\s*(\d+)""", RegexOption.IGNORE_CASE)

    fun parse(rawText: String): ParseResult {
        val normalizedText = normalizeText(rawText)
        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val fecha = extractDate(lines)
        val total = extractTotal(lines)
        val socioClub = extractSocioClub(lines)
        val productLines = extractProducts(lines)

        return ParseResult(
            ticket = Ticket(
                fecha = fecha,
                supermarketId = 1L,
                supermarketName = "Carrefour",
                total = total,
                subtotal = null,
                descuentos = null,
                formaPago = null,
                pdfPath = null,
                numProductos = productLines.size,
                socioClub = socioClub,
                lines = productLines
            ),
            warnings = emptyList()
        )
    }

    private fun normalizeText(text: String): String {
        val hasSpacedLetters = Regex("""[A-Z]\s+[A-Z]""").containsMatchIn(text)
        return if (hasSpacedLetters) {
            text.replace(Regex("""([A-Z])\s+(?=[A-Z])"""), "$1")
                .replace(Regex("""([a-z])\s+(?=[a-z])"""), "$1")
        } else {
            text
        }
    }

    private fun extractDate(lines: List<String>): Date {
        for (line in lines) {
            val match = datePattern.find(line)
            if (match != null) {
                val dateTimeStr = "${match.groupValues[1]} ${match.groupValues[2]}"
                return try {
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(dateTimeStr) ?: Date()
                } catch (_: Exception) {
                    Date()
                }
            }
        }
        return Date()
    }

    private fun extractTotal(lines: List<String>): Float {
        for ((index, line) in lines.withIndex()) {
            if (line.contains("TOTAL A PAGAR", ignoreCase = true)) {
                // a veces el total está en la línea siguiente
                extractPrice(line)?.let { return it }
                if (index + 1 < lines.size) {
                    extractPrice(lines[index + 1])?.let { return it }
                }
            }
        }
        return 0f
    }

    private fun extractProducts(lines: List<String>): List<TicketLine> {
        val subtotalIndex = lines.indexOfFirst { it.contains("SUBTOTAL", ignoreCase = true) }
        val productSection = if (subtotalIndex > 0) lines.take(subtotalIndex) else lines

        val nameCandidates = mutableListOf<String>()
        val positivePrices = mutableListOf<Float>()

        for (line in productSection) {
            when {
                isSkippableLine(line) -> continue
                isNegativePriceLine(line) -> continue
                isPositivePriceLine(line) -> {
                    extractPrice(line)?.let { positivePrices.add(it) }
                }
                isProductNameLine(line) -> nameCandidates.add(cleanProductName(line))
            }
        }

        val count = minOf(nameCandidates.size, positivePrices.size)
        val result = mutableListOf<TicketLine>()

        for (i in 0 until count) {
            val nombre = nameCandidates[i]
            val precio = positivePrices[i]
            result.add(
                TicketLine(
                    ticketId = 0,
                    nombreOriginal = nombre,
                    nombreNormalizado = normalizeProductName(nombre),
                    cantidad = 1,
                    precioUnitario = precio,
                    precioTotal = precio,
                    esDescuento = false
                )
            )
        }

        return result
    }

    private fun isSkippableLine(line: String): Boolean {
        val upper = line.uppercase()
        return upper.startsWith("***") ||
            upper.contains("CARREFOUR S.A") ||
            upper.contains("CAMPANAR") ||
            upper.startsWith("CIF:") ||
            upper.startsWith("TELF") ||
            upper.startsWith("TELÉFONO") ||
            upper.startsWith("TELEFONO") ||
            upper.contains("ATENCIÓN AL CLIENTE") ||
            upper.contains("SUBTOTAL") ||
            upper.contains("TOTAL A PAGAR") ||
            upper.contains("VENTAJAS OBTENIDAS") ||
            upper.contains("TIPO") ||
            upper.contains("BASE") ||
            upper.contains("CUOTA") ||
            upper.contains("VENTA") ||
            upper.contains("MASTERCARD") ||
            upper.contains("CONTACTLESS") ||
            upper.contains("CAMBIO RECIBIDO") ||
            upper.contains("SOCIO CLUB") ||
            upper.contains("SALDO") ||
            upper.contains("SUPERFAMILIAS") ||
            upper.contains("DÍAS PARA CAMBIOS") ||
            upper.contains("DIAS PARA CAMBIOS") ||
            upper.matches(Regex("""^[A-Z]{1,3}\d{2,4}$""")) ||
            upper.matches(Regex("""^\d+\s*x\s*\($""", RegexOption.IGNORE_CASE)) ||
            upper.matches(Regex("""^\d+\)$""")) ||
            upper.matches(Regex("""^\d+$""")) ||
            upper.matches(Regex("""^=+$""")) ||
            upper == "50%"
    }

    private fun isNegativePriceLine(line: String): Boolean = line.matches(Regex("""^-\d+[,\.]\d{2}$"""))

    private fun isPositivePriceLine(line: String): Boolean = line.matches(priceOnlyPattern)

    private fun isProductNameLine(line: String): Boolean {
        if (line.length < 3) return false
        if (extractPrice(line) != null) return false
        if (line.contains("DESCUENTO", ignoreCase = true)) return false
        if (line.contains("DTO.", ignoreCase = true)) return false
        if (line.contains("DCTO.", ignoreCase = true)) return false
        return line.any { it.isLetter() }
    }

    private fun cleanProductName(line: String): String {
        return line.replace(Regex("""\s+"""), " ")
            .replace(Regex("""\($"""), "")
            .trim()
    }

    fun normalizeProductName(name: String): String {
        return name.lowercase()
            .replace(Regex("""\s+(de|del|la|el|las|los|y|con|sin)\s+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun extractPrice(line: String): Float? {
        val match = pricePattern.find(line) ?: return null
        return match.groupValues[1].replace(",", ".").toFloatOrNull()
    }

    private fun extractSocioClub(lines: List<String>): String? {
        for (line in lines) {
            val match = socioPattern.find(line)
            if (match != null) return match.groupValues[1]
        }
        return null
    }
}

data class ParseResult(
    val ticket: Ticket,
    val warnings: List<String> = emptyList()
)
