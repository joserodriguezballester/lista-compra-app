package com.jose.listacompra.domain.usecase.ticket

import android.content.Context
import android.net.Uri
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Ticket
import com.jose.listacompra.domain.repository.ITicketRepository
import com.jose.listacompra.utils.CarrefourTicketParser
import com.jose.listacompra.utils.PdfOcrExtractor
import com.jose.listacompra.utils.ProductMatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class ImportResult(
    val ticket: Ticket,
    val unmatchedCount: Int,
    val warnings: List<String> = emptyList(),
    val debugLog: List<String> = emptyList()
)

class ImportTicketUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ticketRepository: ITicketRepository
) {
    private val ocrExtractor = PdfOcrExtractor(context)

    suspend operator fun invoke(
        uri: Uri,
        articulos: List<Articulo>,
        categories: List<Category>
    ): Result<ImportResult> {
        val debug = mutableListOf<String>()
        return try {
            debug += "PDF OK"

            val ocrResult = ocrExtractor.extractTextFromPdf(uri)
            if (ocrResult.isFailure) {
                return Result.failure(Exception("PDF error"))
            }

            val rawText = ocrResult.getOrNull() ?: ""
            debug += "Texto: ${rawText.length} chars"

            val rawLines = rawText.lines()
            debug += "RAW_TRACE_RANGE=6..12"
            debug += buildRawWindowDebug(rawLines, 6, 12)

            val parseResult = CarrefourTicketParser.parse(rawText)
            debug += "PARSED_COUNT=${parseResult.ticket.lines.size}"
            debug += "TICKET_TOTAL=%.2f".format(parseResult.ticket.total)
            parseResult.ticket.lines.take(5).forEachIndexed { index: Int, line ->
                debug += "PARSED[$index]=${line.nombreOriginal} | total=${line.precioTotal} | unit=${line.precioUnitario}"
            }

            val matchedLines = parseResult.ticket.lines.map { line: com.jose.listacompra.domain.model.TicketLine ->
                val match = ProductMatcher.findBestMatch(
                    normalizedName = line.nombreNormalizado,
                    articulos = articulos
                )

                val suggestedCategory = if (match == null) {
                    ProductMatcher.assignCategory(line.nombreOriginal, categories)
                } else {
                    articulos.find { it.id == match.id }?.categoryId
                }

                line.copy(
                    articuloId = match?.id,
                    articuloNombre = match?.name,
                    categoriaId = suggestedCategory
                )
            }

            val unmatchedCount = matchedLines.count { line -> line.articuloId == null }
            val ticket = parseResult.ticket.copy(lines = matchedLines)
            debug += "UNMATCHED_COUNT=$unmatchedCount"
            debug += "FINAL_LINES=${ticket.lines.size}"

            if (ticket.lines.isEmpty() || ticket.total <= 0f) {
                return Result.failure(Exception(debug.joinToString(" | ")))
            }

            Result.success(
                ImportResult(
                    ticket = ticket,
                    unmatchedCount = unmatchedCount,
                    warnings = parseResult.warnings,
                    debugLog = debug
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: debug.joinToString(" | "), e))
        }
    }

    private fun buildRawWindowDebug(rawLines: List<String>, start: Int, end: Int): List<String> {
        val output = mutableListOf<String>()
        val currentNormLines = normalizeCurrentLines(rawLines)
        val softNormLines = normalizeSoftLines(rawLines)

        for (index in start..end) {
            val raw = rawLines.getOrNull(index) ?: ""
            val visible = toVisibleNoiseMap(raw)
            val soft = softNormLines.getOrNull(index) ?: ""
            val norm = currentNormLines.getOrNull(index) ?: ""
            output += "RAW[$index]=$raw"
            output += "RAW_VISIBLE[$index]=$visible"
            output += "SOFT[$index]=$soft"
            output += "NORM[$index]=$norm"
        }

        return output
    }

    private fun normalizeSoftLines(lines: List<String>): List<String> {
        return lines.map { rawLine ->
            rawLine
                .replace(Regex("""[\u200B-\u200D\u2060\uFEFF]"""), "")
                .replace(Regex("""[\u00A0\u202F\u2007]"""), " ")
                .replace(Regex("""[ \t]+"""), " ")
                .trim()
        }
    }

    private fun normalizeCurrentLines(lines: List<String>): List<String> {
        return lines.map { rawLine ->
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
        }
    }

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
                        val symbol = noiseMap.getOrPut(ch) {
                            noiseSymbols.getOrElse(noiseIndex) {
                                noiseSymbols.last()
                            }.also { noiseIndex += 1 }
                        }
                        append(symbol)
                    }
                }
            }
        }
    }

    suspend fun saveTicket(ticket: Ticket): Long {
        return ticketRepository.saveTicket(ticket)
    }

    fun close() {
        ocrExtractor.close()
    }
}
