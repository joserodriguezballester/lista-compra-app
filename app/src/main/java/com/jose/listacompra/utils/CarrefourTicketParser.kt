package com.jose.listacompra.utils

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parser para tickets de Carrefour.
 * Extrae productos, precios, fecha y otros datos del texto del ticket.
 */
object CarrefourTicketParser {

    private val pricePattern = Regex("""(\d+[,\.]\d{2})\s*(?:€)?""")
    private val quantityPattern = Regex("""(\d+)\s*x\s*\(""")
    private val datePattern = Regex("""(\d{2}/\d{2}/\d{4})\s+(\d{2}:\d{2}:\d{2})""")
    private val discountPattern = Regex("""(DESCUENTO|DTO\.|DCTO\.)""", RegexOption.IGNORE_CASE)
    private val subtotalPattern = Regex("""SUBTOTAL""")
    private val totalPattern = Regex("""TOTAL\s*A\s*PAGAR""", RegexOption.IGNORE_CASE)
    private val socioPattern = Regex("""SOCIO\s*CLUB.*?:\s*(\d+)""", RegexOption.IGNORE_CASE)

    /**
     * Parsea el texto extraído de un ticket de Carrefour.
     * @param rawText Texto crudo del OCR
     * @return Ticket con líneas parseadas
     */
    fun parse(rawText: String): ParseResult {
        // Normalizar texto: quitar espacios extra entre letras
        val normalizedText = normalizeText(rawText)
        val lines = normalizedText.lines().filter { it.isNotBlank() }

        // Extraer fecha
        val fecha = extractDate(lines)

        // Extraer líneas de producto
        val productLines = mutableListOf<TicketLine>()
        var inProductsSection = true
        var lineNumber = 0

        for (line in lines) {
            val trimmedLine = line.trim()

            // Detectar secciones
            when {
                subtotalPattern.containsMatchIn(trimmedLine) -> inProductsSection = false
                totalPattern.containsMatchIn(trimmedLine) -> continue
                discountPattern.containsMatchIn(trimmedLine) -> {
                    // Es un descuento, no un producto
                    val discountAmount = extractPrice(trimmedLine)
                    // Por ahora ignoramos descuentos
                    continue
                }
            }

            if (!inProductsSection) continue

            // Intentar parsear como línea de producto
            val productLine = parseProductLine(trimmedLine, lineNumber++)
            if (productLine != null) {
                productLines.add(productLine)
            }
        }

        // Extraer total
        val total = extractTotal(lines)

        // Extraer número de socio
        val socioClub = extractSocioClub(lines)

        return ParseResult(
            ticket = Ticket(
                fecha = fecha.time,
                supermarketId = 1L, // Carrefour
                supermarketName = "Carrefour",
                total = total,
                numProductos = productLines.size,
                socioClub = socioClub,
                lines = productLines
            ),
            warnings = emptyList()
        )
    }

    /**
     * Normaliza el texto del ticket.
     * Los tickets escaneados suelen tener espacios entre letras.
     */
    private fun normalizeText(text: String): String {
        // Detectar si hay espacios entre letras (patrón de ticket escaneado)
        val hasSpacedLetters = Regex("""[A-Z]\s+[A-Z]""").containsMatchIn(text)

        return if (hasSpacedLetters) {
            // Quitar espacios entre letras mayúsculas
            text.replace(Regex("""([A-Z])\s+(?=[A-Z])"""), "$1")
                .replace(Regex("""([a-z])\s+(?=[a-z])"""), "$1")
        } else {
            text
        }
    }

    /**
     * Extrae la fecha del ticket.
     */
    private fun extractDate(lines: List<String>): Date {
        for (line in lines) {
            val match = datePattern.find(line)
            if (match != null) {
                val dateStr = match.groupValues[1]
                val timeStr = match.groupValues[2]
                val dateTimeStr = "$dateStr $timeStr"
                return try {
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(dateTimeStr) ?: Date()
                } catch (e: Exception) {
                    Date()
                }
            }
        }
        return Date()
    }

    /**
     * Parsea una línea individual de producto.
     */
    private fun parseProductLine(line: String, lineNumber: Int): TicketLine? {
        // Ignorar líneas muy cortas o que parecen headers
        if (line.length < 5) return null

        // Ignorar líneas que son claramente no-productos
        val ignorePatterns = listOf(
            "CARREFOUR", "CENTROS COMERCIALES", "CIF:", "Telf.",
            "CAMpanar", "Valencia", "ATENCIÓN", "NRF:",
            "VENTA JAS", "CAMBIO RECIBIDO", "Días para cambios"
        )

        if (ignorePatterns.any { line.contains(it, ignoreCase = true) }) {
            return null
        }

        // Buscar patrón de precio al final de la línea
        val priceMatches = pricePattern.findAll(line).toList()
        if (priceMatches.isEmpty()) return null

        // El último precio es el total de la línea
        val precioTotal = priceMatches.last().groupValues[1].replace(",", ".").toFloatOrNull() ?: return null

        // Buscar cantidad si existe (patrón: "2x(" o "3 x(")
        val quantityMatch = quantityPattern.find(line)
        val cantidad = quantityMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

        // Calcular precio unitario
        val precioUnitario = if (quantityMatch != null && cantidad > 1) {
            // Buscar el precio unitario entre paréntesis
            val unitPricePattern = Regex("""x\s*\((\d+[,\.]\d{2})\)""")
            val unitMatch = unitPricePattern.find(line)
            unitMatch?.groupValues?.get(1)?.replace(",", ".")?.toFloatOrNull() ?: (precioTotal / cantidad)
        } else {
            precioTotal
        }

        // Extraer nombre del producto
        val nombreOriginal = extractProductName(line, priceMatches)

        return TicketLine(
            ticketId = 0, // Se asignará al guardar
            nombreOriginal = nombreOriginal,
            nombreNormalizado = normalizeProductName(nombreOriginal),
            cantidad = cantidad,
            precioUnitario = precioUnitario,
            precioTotal = precioTotal,
            esDescuento = false
        )
    }

    /**
     * Extrae el nombre del producto de una línea.
     */
    private fun extractProductName(line: String, priceMatches: List<MatchResult>): String {
        // Quitar los precios de la línea para obtener el nombre
        var name = line
        for (match in priceMatches.sortedByDescending { it.range.first }) {
            name = name.removeRange(match.range)
        }

        // Quitar códigos de promoción (CE73, CR10, C699, etc.)
        name = name.replace(Regex("""[A-Z]{1,2}\d{2,3}"""), "")

        // Quitar patrones de cantidad
        name = name.replace(Regex("""\d+\s*x\s*\(.*?\)"""), "")

        // Quitar espacios extra y limpiar
        name = name.trim()
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""^\d+\s"""), "") // Números al inicio

        return name.trim()
    }

    /**
     * Normaliza el nombre del producto para matching.
     */
    fun normalizeProductName(name: String): String {
        return name.lowercase()
            .replace(Regex("""\s+(de|del|la|el|las|los|y|con|sin)\s+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    /**
     * Extrae un precio de una línea.
     */
    private fun extractPrice(line: String): Float? {
        val match = pricePattern.find(line) ?: return null
        return match.groupValues[1].replace(",", ".").toFloatOrNull()
    }

    /**
     * Extrae el total del ticket.
     */
    private fun extractTotal(lines: List<String>): Float {
        for (line in lines) {
            if (totalPattern.containsMatchIn(line)) {
                return extractPrice(line) ?: 0f
            }
        }
        return 0f
    }

    /**
     * Extrae el número de socio del club.
     */
    private fun extractSocioClub(lines: List<String>): String? {
        for (line in lines) {
            val match = socioPattern.find(line)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
}

/**
 * Resultado del parsing de un ticket.
 */
data class ParseResult(
    val ticket: Ticket,
    val warnings: List<String> = emptyList()
)
