package com.jose.listacompra.utils

import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.model.TicketLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParseResult(
    val ticket: Ticket,
    val warnings: List<String> = emptyList(),
    val debugDate: List<String> = emptyList()
)

object CarrefourTicketParser {

    private val compactPricePattern = """-?\d+[,.]\d{1,2}"""
    private val spacedPricePattern = """-?(?:\d\s*){1,3}[,.]\s*(?:\d\s*){1,2}"""
    private val embeddedPricePattern = Regex("""(?<!\d)($compactPricePattern|$spacedPricePattern)(?!\d)""")
    private val datePattern = Regex("""(\d{1,2}/\d{1,2}/\d{2,4})\s+(\d{1,2}:\d{1,2}:\d{1,2})""")
    private val socioPattern = Regex("""SOCIO\s*CLUB.*?:\s*(\d+)""", RegexOption.IGNORE_CASE)
    private val subtotalBlockPattern = Regex("""SUBTOTAL\s*:?\s*([0-9]+[,.][0-9]{2})""", setOf(RegexOption.IGNORE_CASE))
    private val totalBlockPattern = Regex("""TOTAL\s*A\s*PAGAR\s*:?\s*([0-9]+[,.][0-9]{2})""", setOf(RegexOption.IGNORE_CASE))

    fun parse(rawText: String): ParseResult {
        val normalizedText = normalizeText(rawText)
        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val dateDebug = mutableListOf<String>()
        val fecha = extractDate(lines, dateDebug)
        val socioClub = extractSocioClub(lines)
        val extraction = extractProducts(rawText.lines())
        val total = extractTotal(normalizedText, lines, extraction.lines)
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
                numProductos = extraction.lines.size,
                socioClub = socioClub,
                lines = extraction.lines
            ),
            warnings = extraction.debug,
            debugDate = dateDebug
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

    private fun extractDate(lines: List<String>, debug: MutableList<String>): Date {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        for ((index, line) in lines.withIndex()) {
            val directMatch = datePattern.find(line)
            if (directMatch != null) {
                val dateTimeStr = "${directMatch.groupValues[1]} ${directMatch.groupValues[2]}"
                debug += "FECHA_RAW[$index]=$line"
                debug += "FECHA_CANDIDATA[$index]=$dateTimeStr"
                return try {
                    val parsed = formatter.parse(dateTimeStr) ?: Date()
                    debug += "FECHA_PARSEADA=${formatter.format(parsed)}"
                    debug += "FECHA_FALLBACK=NO"
                    parsed
                } catch (_: Exception) {
                    debug += "FECHA_PARSE_ERROR[$index]=$dateTimeStr"
                    Date()
                }
            }

            val structured = extractStructuredDateTime(line)
            if (structured != null) {
                debug += "FECHA_RAW[$index]=$line"
                debug += "FECHA_CANDIDATA[$index]=$structured"
                return try {
                    val parsed = formatter.parse(structured) ?: Date()
                    debug += "FECHA_PARSEADA=${formatter.format(parsed)}"
                    debug += "FECHA_FALLBACK=NO"
                    parsed
                } catch (_: Exception) {
                    debug += "FECHA_PARSE_ERROR[$index]=$structured"
                    Date()
                }
            }

            val flexible = extractFlexibleDateTime(line)
            if (flexible != null) {
                debug += "FECHA_RAW[$index]=$line"
                debug += "FECHA_CANDIDATA_FLEX[$index]=$flexible"
                return try {
                    val parsed = formatter.parse(flexible) ?: Date()
                    debug += "FECHA_PARSEADA=${formatter.format(parsed)}"
                    debug += "FECHA_FALLBACK=NO"
                    parsed
                } catch (_: Exception) {
                    debug += "FECHA_PARSE_ERROR_FLEX[$index]=$flexible"
                    Date()
                }
            }
        }
        debug += "FECHA_CANDIDATA=NONE"
        debug += "FECHA_FALLBACK=SI"
        return Date()
    }

    private fun extractFlexibleDateTime(line: String): String? {
        val raw = line.trim()
        if (raw.none { it.isDigit() }) return null

        val window = raw.take(100)
        val match = Regex("""(\d{1,2})\D+(\d{1,2})\D+(\d{2,4}).*?(\d{1,2})\D+(\d{1,2})\D+(\d{1,2})""").find(window)
            ?: return null

        val dd = match.groupValues[1].toIntOrNull() ?: return null
        val mm = match.groupValues[2].toIntOrNull() ?: return null
        val yyRaw = match.groupValues[3]
        val hh = match.groupValues[4].toIntOrNull() ?: return null
        val mi = match.groupValues[5].toIntOrNull() ?: return null
        val ss = match.groupValues[6].toIntOrNull() ?: return null

        if (dd !in 1..31 || mm !in 1..12 || hh !in 0..23 || mi !in 0..59 || ss !in 0..59) return null

        val year = when (yyRaw.length) {
            2 -> "20$yyRaw"
            4 -> yyRaw
            else -> yyRaw.padStart(4, '0')
        }

        return "%02d/%02d/%s %02d:%02d:%02d".format(dd, mm, year, hh, mi, ss)
    }

    private fun extractStructuredDateTime(line: String): String? {
        val raw = line.trim()
        if ('/' !in raw || ':' !in raw) return null

        val window = raw.take(80)
        val tokens = Regex("""\d+|[/:]""").findAll(window).map { it.value }.toList()
        val slashIdxs = tokens.mapIndexedNotNull { idx, v -> idx.takeIf { v == "/" } }
        val colonIdxs = tokens.mapIndexedNotNull { idx, v -> idx.takeIf { v == ":" } }
        if (slashIdxs.size < 2 || colonIdxs.size < 2) return null

        for (si in 0 until slashIdxs.size - 1) {
            val s1 = slashIdxs[si]
            val s2 = slashIdxs[si + 1]
            if (s1 < 1 || s2 != s1 + 2) continue

            val c1 = colonIdxs.firstOrNull { it > s2 } ?: continue
            val c2 = colonIdxs.firstOrNull { it == c1 + 2 } ?: continue
            if (c1 != s2 + 2) continue

            val dd = tokens.getOrNull(s1 - 1) ?: continue
            val mm = tokens.getOrNull(s1 + 1) ?: continue
            val yy = tokens.getOrNull(s2 + 1) ?: continue
            val hh = tokens.getOrNull(c1 - 1) ?: continue
            val mi = tokens.getOrNull(c1 + 1) ?: continue
            val ss = tokens.getOrNull(c2 + 1) ?: continue

            if (dd.length !in 1..2 || mm.length !in 1..2 || yy.length !in 2..4 || hh.length !in 1..2 || mi.length !in 1..2 || ss.length !in 1..2) continue

            val year = if (yy.length == 2) "20$yy" else yy.padStart(4, '0')
            return "%02d/%02d/%s %02d:%02d:%02d".format(dd.toInt(), mm.toInt(), year, hh.toInt(), mi.toInt(), ss.toInt())
        }

        return null
    }

    private fun extractTotal(rawText: String, lines: List<String>, productLines: List<TicketLine>): Float {
        subtotalBlockPattern.find(rawText)?.groupValues?.getOrNull(1)?.let { return it.replace(',', '.').toFloatOrNull() ?: 0f }
        for ((i, line) in lines.withIndex()) {
            if (line.contains("SUBTOTAL", ignoreCase = true)) {
                extractPrice(line)?.let { return it }
                for (j in i + 1..minOf(i + 3, lines.lastIndex)) extractPrice(lines[j])?.let { return it }
            }
        }
        totalBlockPattern.find(rawText)?.groupValues?.getOrNull(1)?.let { return it.replace(',', '.').toFloatOrNull() ?: 0f }
        for ((i, line) in lines.withIndex()) {
            if (line.contains("TOTAL A PAGAR", ignoreCase = true)) {
                extractPrice(line)?.let { return it }
                for (j in i + 1..minOf(i + 3, lines.lastIndex)) extractPrice(lines[j])?.let { return it }
            }
        }
        return productLines.filterNot { it.esDescuento }.sumOf { it.precioTotal.toDouble() }.toFloat()
    }

    private data class ExtractionResult(val lines: List<TicketLine>, val debug: List<String>)
    private data class RawMultiUnit(val quantity: String, val unitPriceRaw: String)

    private fun extractProducts(rawLines: List<String>): ExtractionResult {
        val startIndex = rawLines.indexOfFirst { line -> line.count { it == '*' } > 10 }
        val endIndex = rawLines.indexOfFirst { line -> line.count { it == '=' } > 10 }
        val section = when {
            startIndex >= 0 && endIndex > startIndex -> rawLines.subList(startIndex + 1, endIndex)
            else -> rawLines
        }

        val result = mutableListOf<TicketLine>()
        val debug = mutableListOf<String>()
        var i = 0

        while (i < section.size) {
            val raw = section[i]
            val soft = softNormalizeLine(raw)
            if (soft.isBlank()) { debug += "SKIPPED[$i]=blank"; i += 1; continue }
            if (isSkippableLine(soft)) { debug += "SKIPPED[$i]=skip_rule"; i += 1; continue }

            val trailingAmount = extractTrailingAmountRaw(raw)
            if (trailingAmount != null && trailingAmount.trim().startsWith("-")) {
                debug += "CASO[$i]=3"
                debug += "RAW[$i]=$raw"
                debug += "SKIPPED[$i]=discount"
                i += 1
                continue
            }

            if (trailingAmount != null) {
                val productRaw = extractProductPartRaw(raw)
                val productClean = buildNameClean(buildNamePreclean(productRaw))
                val total = normalizePriceToken(trailingAmount)?.replace(',', '.')?.toFloatOrNull()
                if (productClean.isNotBlank() && total != null && total > 0f) {
                    result += TicketLine(ticketId = 0, nombreOriginal = productClean, nombreNormalizado = normalizeProductName(productClean), cantidad = 1, precioUnitario = total, precioTotal = total, esDescuento = false)
                    val out = result.lastIndex
                    debug += "CASO[$out]=1"
                    debug += "RAW[$out]=$raw"
                    debug += "PARSE_ARRAY[$out]={nombre=$productClean, qty=1, unit=${format2(total)}, total=${format2(total)}}"
                    i += 1
                    continue
                }
            }

            val nextRaw = section.getOrNull(i + 1)
            val nextHasParens = nextRaw?.let { it.contains("(") && it.contains(")") } == true
            val nextTotalRaw = nextRaw?.let { extractTrailingAmountRaw(it) }
            val nextDetail = nextRaw?.let { extractMultiUnitRaw(it) }
            if (trailingAmount == null && nextRaw != null && nextHasParens && nextTotalRaw != null && nextDetail != null) {
                val productRaw = extractLeadingNameByWideGapRaw(raw)
                val productClean = buildNameClean(buildNamePreclean(productRaw))
                val unit = normalizePriceToken(nextDetail.unitPriceRaw)?.replace(',', '.')?.toFloatOrNull()
                val total = normalizePriceToken(nextTotalRaw)?.replace(',', '.')?.toFloatOrNull()
                val qty = nextDetail.quantity.toIntOrNull()
                if (productClean.isNotBlank() && unit != null && total != null && qty != null && qty > 0) {
                    result += TicketLine(ticketId = 0, nombreOriginal = productClean, nombreNormalizado = normalizeProductName(productClean), cantidad = qty, precioUnitario = unit, precioTotal = total, esDescuento = false)
                    val out = result.lastIndex
                    debug += "CASO[$out]=2"
                    debug += "RAW[$out]=$raw"
                    debug += "RAW_NEXT[$out]=$nextRaw"
                    debug += "PARSE_ARRAY[$out]={nombre=$productClean, qty=$qty, unit=${format2(unit)}, total=${format2(total)}}"
                    i += 2
                    continue
                }
            }

            debug += "CASO[$i]=?"
            debug += "RAW[$i]=$raw"
            debug += "SKIPPED[$i]=unknown"
            i += 1
        }

        return ExtractionResult(result, debug)
    }

    private fun extractTrailingAmountRaw(line: String): String? {
        val visible = toVisibleNoiseMap(line).trimEnd('#')
        val lastComma = visible.lastIndexOf(',')
        if (lastComma < 0) return null

        var end = visible.length - 1
        while (end >= 0 && visible[end] == '#') end--
        if (end <= lastComma) return null

        val tail = visible.substring(0, end + 1)
        var start = lastComma
        var maxGap = 1
        var seenDigit = 0

        var j = lastComma - 1
        while (j >= 0) {
            val ch = tail[j]
            if (ch.isDigit()) {
                seenDigit++
                start = j
                j--
                continue
            }
            if (ch == ',' || ch == '-') {
                start = j
                j--
                continue
            }
            if (ch == '#') {
                var k = j
                while (k >= 0 && tail[k] == '#') k--
                val gap = j - k
                if (gap > maxGap + 1) break
                maxGap = maxOf(maxGap, gap)
                start = k + 1
                j = k
                continue
            }
            break
        }

        val candidate = tail.substring(start, end + 1).replace("#", " ").trim()
        return if (normalizePriceToken(candidate) != null) candidate else null
    }

    private fun extractProductPartRaw(line: String): String {
        val visible = toVisibleNoiseMap(line).trimEnd('#')
        val trailingAmount = extractTrailingAmountRaw(line) ?: return line.trimEnd()
        val visiblePrice = toVisibleNoiseMap(trailingAmount).trimEnd('#')
        val idx = visible.lastIndexOf(visiblePrice)
        return if (idx >= 0) line.take(idx).trimEnd() else line.trimEnd()
    }

    private fun extractLeadingNameByWideGapRaw(line: String): String {
        val visible = toVisibleNoiseMap(line).trimEnd()
        val firstPartVisible = Regex("""#{5,}""").split(visible).firstOrNull { it.isNotBlank() }?.trim('#') ?: return ""
        val rawPrefixLen = visible.indexOf(firstPartVisible) + firstPartVisible.length
        return line.take(rawPrefixLen).trimEnd()
    }

    private fun buildNamePreclean(value: String): String {
        val trimmed = value.trim()
        val firstChunk = trimmed
            .split(Regex(""" {3,}"""))
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?: ""

        return firstChunk
            .replace(Regex(""" {2,}"""), "+")
            .replace(" ", "")
            .trim()
    }

    private fun buildNameClean(value: String): String {
        return value.replace("+", " ").trim()
    }
    private fun format2(value: Float): String = "%.2f".format(value)

    private fun toVisibleNoiseMap(line: String): String {
        if (line.isEmpty()) return "∅"
        val noiseMap = linkedMapOf<Char, Char>()
        val noiseSymbols = listOf('&', '@', '€', '§', '%', '!', '?', '£', '¥', '¤', '†', '‡')
        var noiseIndex = 0
        return buildString {
            line.forEach { ch ->
                when {
                    ch == ' ' -> append('#')
                    ch.isLetterOrDigit() || ch in setOf(',', '.', '-', '/', ':', '(', ')', '*', '=') -> append(ch)
                    ch.isWhitespace() -> append('#')
                    else -> {
                        val symbol = noiseMap.getOrPut(ch) { noiseSymbols.getOrElse(noiseIndex) { noiseSymbols.last() }.also { noiseIndex += 1 } }
                        append(symbol)
                    }
                }
            }
        }
    }

    private fun extractMultiUnitRaw(line: String): RawMultiUnit? {
        val visible = toVisibleNoiseMap(line)
        val total = extractTrailingAmountRaw(line) ?: return null
        val visibleTotal = toVisibleNoiseMap(total).trimEnd('#')
        val idxTotal = visible.lastIndexOf(visibleTotal)
        val prefix = if (idxTotal > 0) visible.substring(0, idxTotal).trimEnd('#') else visible
        val idxX = prefix.indexOf('x').takeIf { it >= 0 } ?: prefix.indexOf('X').takeIf { it >= 0 } ?: return null
        val quantityPart = prefix.substring(0, idxX)
        val quantity = quantityPart.replace("#", "").replace(Regex("""[^\d]"""), "").trim()
        val openParen = prefix.indexOf('(')
        val closeParen = prefix.indexOf(')')
        if (openParen < 0 || closeParen <= openParen) return null
        val unitRaw = prefix.substring(openParen + 1, closeParen).replace("#", " ").replace(Regex("""[^\d, .-]"""), "").trim()
        if (quantity.isBlank() || unitRaw.isBlank()) return null
        return RawMultiUnit(quantity = quantity, unitPriceRaw = unitRaw)
    }

    private fun softNormalizeLine(line: String): String = line.replace(Regex("""[\u200B-\u200D\u2060\uFEFF]"""), "").replace(Regex("""[\u00A0\u202F\u2007]"""), " ").replace(Regex("""[ \t]+"""), " ").trim()

    private fun isSkippableLine(line: String): Boolean {
        val upper = line.uppercase()
        return upper.contains("DESCUENTO EN") || upper.contains("DTO. CUPON") || upper.contains("GRATIS FOXY") || upper.contains("VENTAJAS") || upper.contains("ACUMULADO") || upper.contains("DESCUENTOS:") || upper.contains("TOTAL VENTAJAS") || upper.contains("SOCIO CLUB") || upper.matches(Regex("""^[A-Z]{1,3}\d{2,4}$""")) || upper.matches(Regex("""^[0-9]{6,}$""")) || upper == "50%"
    }

    fun normalizeProductName(name: String): String = name.lowercase().replace(Regex("""\s+(de|del|la|el|las|los|y|con|sin)\s+"""), " ").replace(Regex("""\s+"""), " ").trim()

    private fun extractPrice(line: String): Float? {
        val candidate = line.trim()
        normalizePriceToken(candidate)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        val allMatches = embeddedPricePattern.findAll(candidate).mapNotNull { normalizePriceToken(it.groupValues[1])?.replace(',', '.')?.toFloatOrNull() }.toList()
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
