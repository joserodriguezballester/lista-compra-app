package com.jose.listacompra.utils

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CarrefourTicketParser {

    private val compactPricePattern = """-?\d+[,.]\d{1,2}"""
    private val spacedPricePattern = """-?\d+\s*[,.]\s*\d(?:\s*\d)?"""
    private val priceLinePattern = Regex("""^$compactPricePattern$|^$spacedPricePattern$""")
    private val trailingPricePattern = Regex("""(.+?)\s+($compactPricePattern|$spacedPricePattern)$""")
    private val embeddedPricePattern = Regex("""(?<!\d)($compactPricePattern|$spacedPricePattern)(?!\d)""")
    private val datePattern = Regex("""(\d{2}/\d{2}/\d{4})\s+(\d{2}:\d{2}:\d{2})""")
    private val socioPattern = Regex("""SOCIO\s*CLUB.*?:\s*(\d+)""", RegexOption.IGNORE_CASE)
    private val subtotalBlockPattern = Regex(
        """SUBTOTAL\s*:?[\s\n\r]*([0-9]+[,.][0-9]{2})""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val totalBlockPattern = Regex(
        """TOTAL\s*A\s*PAGAR\s*:?[\s\n\r]*([0-9]+[,.][0-9]{2})""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    fun parse(rawText: String): ParseResult {
        val normalizedText = normalizeText(rawText)
        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val fecha = extractDate(lines)
        val socioClub = extractSocioClub(lines)
        val productLines = extractProducts(lines)
        val total = extractTotal(normalizedText, lines, productLines)

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
        val normalizedLetters = if (hasSpacedLetters) {
            text.replace(Regex("""([A-Z])\s+(?=[A-Z])"""), "$1")
                .replace(Regex("""([a-z])\s+(?=[a-z])"""), "$1")
        } else text

        return normalizedLetters
            .replace(Regex("""(?<=\d)\s*([,.])\s*(?=\d)"""), "$1")
            .replace(Regex("""(?<=[,.]\d)\s+(?=\d\b)"""), "")
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

    private fun extractTotal(rawText: String, lines: List<String>, productLines: List<TicketLine>): Float {
        subtotalBlockPattern.find(rawText)?.groupValues?.getOrNull(1)?.let {
            return it.replace(',', '.').toFloatOrNull() ?: 0f
        }
        for ((i, line) in lines.withIndex()) {
            if (line.contains("SUBTOTAL", ignoreCase = true)) {
                extractPrice(line)?.let { return it }
                for (j in i + 1..minOf(i + 3, lines.lastIndex)) {
                    extractPrice(lines[j])?.let { return it }
                }
            }
        }
        totalBlockPattern.find(rawText)?.groupValues?.getOrNull(1)?.let {
            return it.replace(',', '.').toFloatOrNull() ?: 0f
        }
        for ((i, line) in lines.withIndex()) {
            if (line.contains("TOTAL A PAGAR", ignoreCase = true)) {
                extractPrice(line)?.let { return it }
                for (j in i + 1..minOf(i + 3, lines.lastIndex)) {
                    extractPrice(lines[j])?.let { return it }
                }
            }
        }
        return productLines.sumOf { it.precioTotal.toDouble() }.toFloat()
    }

    private fun extractProducts(lines: List<String>): List<TicketLine> {
        val startIndex = lines.indexOfFirst { line -> line.count { it == '*' } > 10 }
        val endIndex = lines.indexOfFirst { line -> line.count { it == '=' } > 10 }

        val section = when {
            startIndex >= 0 && endIndex > startIndex -> lines.subList(startIndex + 1, endIndex)
            else -> lines
        }

        val result = mutableListOf<TicketLine>()
        val pendingNames = mutableListOf<String>()
        val pendingPrices = mutableListOf<Float>()

        for (line in section) {
            val normalizedLine = line
                .replace(Regex("""[^\p{L}\p{N},.\- ]+"""), " ")
                .replace(Regex("""\s+"""), " ")
                .trim()
                .replace(Regex("""(\d+)\s*,\s*(\d)\s*(\d)"""), "$1,$2$3")
                .replace(Regex("""(?<!\d)(\d{2,})\s+(\d{2})(?!\d)"""), "$1,$2")

            if (isSkippableLine(normalizedLine)) continue

            val trailing = trailingPricePattern.find(normalizedLine)
            if (trailing != null) {
                val name = cleanProductName(trailing.groupValues[1])
                val price = normalizePriceToken(trailing.groupValues[2])?.replace(',', '.')?.toFloatOrNull()
                if (name.isNotBlank() && price != null && price > 0f) {
                    result.add(
                        TicketLine(
                            ticketId = 0,
                            nombreOriginal = name,
                            nombreNormalizado = normalizeProductName(name),
                            cantidad = 1,
                            precioUnitario = price,
                            precioTotal = price,
                            esDescuento = false
                        )
                    )
                    continue
                }
            }

            if (isNegativePriceLine(normalizedLine)) continue
            if (isPositivePriceLine(normalizedLine)) {
                extractPrice(normalizedLine)?.let { pendingPrices.add(it) }
                continue
            }
            if (isProductNameLine(normalizedLine)) {
                pendingNames.add(cleanProductName(normalizedLine))
            }
        }

        val pairCount = minOf(pendingNames.size, pendingPrices.size)
        for (i in 0 until pairCount) {
            val name = pendingNames[i]
            val price = pendingPrices[i]
            result.add(
                TicketLine(
                    ticketId = 0,
                    nombreOriginal = name,
                    nombreNormalizado = normalizeProductName(name),
                    cantidad = 1,
                    precioUnitario = price,
                    precioTotal = price,
                    esDescuento = false
                )
            )
        }

        return result
    }

    private fun isSkippableLine(line: String): Boolean {
        val upper = line.uppercase()
        return upper.contains("DESCUENTO EN") ||
            upper.contains("DTO. CUPON") ||
            upper.contains("GRATIS FOXY") ||
            upper.contains("VENTAJAS") ||
            upper.contains("ACUMULADO") ||
            upper.contains("DESCUENTOS:") ||
            upper.contains("TOTAL VENTAJAS") ||
            upper.contains("SOCIO CLUB") ||
            upper.matches(Regex("""^[A-Z]{1,3}\d{2,4}$""")) ||
            upper.matches(Regex("""^\d+\s*X\s*\($""")) ||
            upper.matches(Regex("""^[0-9]{6,}$""")) ||
            upper == "50%"
    }

    private fun isNegativePriceLine(line: String): Boolean = normalizePriceToken(line)?.startsWith("-") == true
    private fun isPositivePriceLine(line: String): Boolean = line.matches(priceLinePattern)

    private fun isProductNameLine(line: String): Boolean {
        if (line.length < 3) return false
        if (isPositivePriceLine(line) || isNegativePriceLine(line)) return false
        if (trailingPricePattern.containsMatchIn(line)) return false
        if (!line.any { it.isLetter() }) return false
        return true
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
        val candidate = line.trim()

        normalizePriceToken(candidate)?.replace(',', '.')?.toFloatOrNull()?.let { return it }

        val allMatches = embeddedPricePattern.findAll(candidate)
            .mapNotNull { normalizePriceToken(it.groupValues[1])?.replace(',', '.')?.toFloatOrNull() }
            .toList()

        return allMatches.maxByOrNull { it.toString().length }
    }

    private fun normalizePriceToken(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null

        val negative = trimmed.startsWith('-')
        val unsigned = if (negative) trimmed.drop(1) else trimmed
        val collapsed = unsigned.replace(Regex("""\s+"""), "")
        val normalized = collapsed.replace(',', '.')

        if (!normalized.matches(Regex("""\d+\.\d{1,2}|\d+"""))) return null

        return (if (negative) "-" else "") + normalized.replace('.', ',')
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
