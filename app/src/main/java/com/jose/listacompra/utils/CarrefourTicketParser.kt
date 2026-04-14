package com.jose.listacompra.utils

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParseResult(
    val ticket: Ticket,
    val warnings: List<String> = emptyList()
)

object CarrefourTicketParser {

    private val compactPricePattern = """-?\d+[,.]\d{1,2}"""
    private val spacedPricePattern = """-?(?:\d\s*){1,3}[,.]\s*(?:\d\s*){1,2}"""
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
    private val wideGapPattern = Regex("""(?:\s|[#@&€§%!£¥¤†‡]){5,}""")
    private val trailingAmountPattern = Regex("""(-?(?:\s|[#@&€§%!£¥¤†‡])*(?:\d(?:\s|[#@&€§%!£¥¤†‡])*){1,3}[,.](?:\s|[#@&€§%!£¥¤†‡])*(?:\d(?:\s|[#@&€§%!£¥¤†‡])*){2})$""")
    private val quantityLeadPattern = Regex("""^\s*(\d+)\s*[xX]\s*\(?\s*(-?(?:\d\s*)+[,.](?:\d\s*){1,2})""")

    fun parse(rawText: String): ParseResult {
        val normalizedText = normalizeText(rawText)
        val debugRawLines = rawText.lines().mapIndexed { index, line -> "RAW[$index] = \"$line\"" }
        val debugNormalizedLines = normalizedText.lines().mapIndexed { index, line -> "NORM[$index] = \"$line\"" }
        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val fecha = extractDate(lines)
        val socioClub = extractSocioClub(lines)
        val productLines = extractProducts(rawText.lines())
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
            warnings = debugRawLines + debugNormalizedLines
        )
    }

    private fun normalizeText(text: String): String {
        return text.lines().joinToString("\n") { rawLine ->
            val sanitized = rawLine
                .replace(Regex("""[\u200B-\u200D\u2060\uFEFF]"""), "")
                .replace(Regex("""[\u00A0\u202F\u2007]"""), " ")
                .replace(Regex("""[\u00C2\u00C3\uFFFD]"""), " ")

            val hasSpacedLetters = Regex("""[A-Z]\s+[A-Z]""").containsMatchIn(sanitized)
            val normalizedLetters = if (hasSpacedLetters) {
                sanitized.replace(Regex("""([A-Z])\s+(?=[A-Z])"""), "$1")
                    .replace(Regex("""([a-z])\s+(?=[a-z])"""), "$1")
            } else sanitized

            normalizedLetters
                .replace(Regex("""(?<=\d)\s*([,.])\s*(?=\d)"""), "$1")
                .replace(Regex("""(?<=[,.]\d)\s+(?=\d\b)"""), "")
                .replace(Regex("""[ \t]+"""), " ")
                .trim()
        }.trim()
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
        return productLines.filterNot { it.esDescuento }.sumOf { it.precioTotal.toDouble() }.toFloat()
    }

    private fun extractProducts(rawLines: List<String>): List<TicketLine> {
        val startIndex = rawLines.indexOfFirst { line -> line.count { it == '*' } > 10 }
        val endIndex = rawLines.indexOfFirst { line -> line.count { it == '=' } > 10 }

        val section = when {
            startIndex >= 0 && endIndex > startIndex -> rawLines.subList(startIndex + 1, endIndex)
            else -> rawLines
        }

        val result = mutableListOf<TicketLine>()
        var index = 0

        while (index < section.size) {
            val rawLine = section[index]
            val softLine = softNormalizeLine(rawLine)
            if (softLine.isBlank() || isSkippableLine(softLine)) {
                index++
                continue
            }

            val trailingAmount = extractTrailingAmount(rawLine)
            if (trailingAmount != null) {
                if (trailingAmount < 0f) {
                    index++
                    continue
                }

                val multiDetail = parseMultiUnitLead(rawLine)
                if (multiDetail != null) {
                    val name = cleanProductName(extractLeadingNameByWideGap(section.getOrNull(index - 1).orEmpty()))
                    if (name.isNotBlank()) {
                        result.add(
                            TicketLine(
                                ticketId = 0,
                                nombreOriginal = name,
                                nombreNormalizado = normalizeProductName(name),
                                cantidad = multiDetail.quantity,
                                precioUnitario = multiDetail.unitPrice,
                                precioTotal = trailingAmount,
                                esDescuento = false
                            )
                        )
                    }
                    index++
                    continue
                }

                val name = cleanProductName(removeTrailingAmount(rawLine))
                if (name.isNotBlank()) {
                    result.add(
                        TicketLine(
                            ticketId = 0,
                            nombreOriginal = name,
                            nombreNormalizado = normalizeProductName(name),
                            cantidad = 1,
                            precioUnitario = trailingAmount,
                            precioTotal = trailingAmount,
                            esDescuento = false
                        )
                    )
                }
                index++
                continue
            }

            if (endsWithSeparator(rawLine)) {
                val name = cleanProductName(extractLeadingNameByWideGap(rawLine))
                val nextLine = section.getOrNull(index + 1)
                val nextAmount = nextLine?.let { extractTrailingAmount(it) }
                val nextDetail = nextLine?.let { parseMultiUnitLead(it) }

                if (name.isNotBlank() && nextLine != null && nextAmount != null && nextAmount > 0f && nextDetail != null) {
                    result.add(
                        TicketLine(
                            ticketId = 0,
                            nombreOriginal = name,
                            nombreNormalizado = normalizeProductName(name),
                            cantidad = nextDetail.quantity,
                            precioUnitario = nextDetail.unitPrice,
                            precioTotal = nextAmount,
                            esDescuento = false
                        )
                    )
                    index += 2
                    continue
                }
            }

            index++
        }

        return result
    }

    private fun softNormalizeLine(line: String): String {
        return line
            .replace(Regex("""[\u200B-\u200D\u2060\uFEFF]"""), "")
            .replace(Regex("""[\u00A0\u202F\u2007]"""), " ")
            .replace(Regex("""[ \t]+"""), " ")
            .trim()
    }

    private fun endsWithSeparator(line: String): Boolean {
        if (line.isEmpty()) return false
        val last = line.last()
        return last.isWhitespace() || last in setOf('#', '@', '&', '€', '§', '%', '!', '?', '£', '¥', '¤', '†', '‡')
    }

    private fun extractTrailingAmount(line: String): Float? {
        val match = trailingAmountPattern.find(line) ?: return null
        val token = match.groupValues[1]
            .replace(Regex("""[^\d,.-]"""), "")
        val normalized = normalizePriceToken(token) ?: return null
        return normalized.replace(',', '.').toFloatOrNull()
    }

    private fun removeTrailingAmount(line: String): String {
        val match = trailingAmountPattern.find(line) ?: return line
        return line.removeRange(match.range).trimEnd()
    }

    private fun extractLeadingNameByWideGap(line: String): String {
        val sanitized = line
            .replace(Regex("""[\u200B-\u200D\u2060\uFEFF]"""), "")
            .replace(Regex("""[\u00A0\u202F\u2007]"""), " ")
            .trimEnd()

        return wideGapPattern.split(sanitized)
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?: sanitized.trim()
    }

    private data class QuantityDetail(
        val quantity: Int,
        val unitPrice: Float
    )

    private fun parseMultiUnitLead(line: String): QuantityDetail? {
        val soft = softNormalizeLine(line)
        val match = quantityLeadPattern.find(soft) ?: return null
        val quantity = match.groupValues[1].toIntOrNull() ?: return null
        val unitPrice = normalizePriceToken(match.groupValues[2])?.replace(',', '.')?.toFloatOrNull() ?: return null
        return QuantityDetail(quantity, unitPrice)
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
            upper.matches(Regex("""^[0-9]{6,}$""")) ||
            upper == "50%"
    }

    private fun cleanProductName(line: String): String {
        return line
            .replace(Regex("""[^\p{L}\p{N} ]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""\($"""), "")
            .replace(Regex("""\s+[A-Z0-9]{4}$"""), "")
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
