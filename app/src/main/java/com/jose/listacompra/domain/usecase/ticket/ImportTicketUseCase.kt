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
            val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
            debug += "Texto: ${rawText.length} chars"

            val starsIndex = lines.indexOfFirst { line ->
                line.count { it == '*' } > 10
            }
            val equalsIndex = lines.indexOfFirst { line ->
                line.count { it == '=' } > 10
            }
            debug += "***: $starsIndex"
            debug += "===: $equalsIndex"

            val section = if (starsIndex >= 0 && equalsIndex > starsIndex) {
                lines.subList(starsIndex + 1, equalsIndex)
            } else {
                lines
            }

            val firstName = section.firstOrNull { isNameCandidate(it) } ?: "-"
            val firstPrice = section.firstOrNull { isPriceCandidate(it) || hasTrailingPrice(it) } ?: "-"
            debug += "FirstName: ${firstName.take(45)}"
            debug += "FirstPrice: ${firstPrice.take(45)}"

            section.take(20).forEachIndexed { index, line ->
                debug += "SEC[$index]: $line"
                debug += "SEC[$index]-isName=${isNameCandidate(line)} isPrice=${isPriceCandidate(line)} hasTrailing=${hasTrailingPrice(line)}"
                val visibleLine = line.replace(" ", "Â·")
                val tail = line.takeLast(minOf(20, line.length)).replace(" ", "Â·")
                val commaIndex = line.lastIndexOf(",")
                val afterComma = if (commaIndex >= 0) line.substring(maxOf(0, commaIndex - 6)).replace(" ", "Â·") else "NO_COMMA"
                debug += "SEC[$index]-visible=$visibleLine"
                debug += "SEC[$index]-tail=$tail"
                debug += "SEC[$index]-afterComma=$afterComma"
            }

            val parseResult = CarrefourTicketParser.parse(rawText)
            debug += "PARSED_COUNT=${parseResult.ticket.lines.size}"
            debug += "TICKET_TOTAL=%.2f".format(parseResult.ticket.total)
            parseResult.ticket.lines.take(5).forEachIndexed { index, line ->
                debug += "PARSED[$index]=${line.nombreOriginal} | total=${line.precioTotal} | unit=${line.precioUnitario}"
            }

            val matchedLines = parseResult.ticket.lines.map { line ->
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

            val unmatchedCount = matchedLines.count { it.articuloId == null }
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

    private fun isNameCandidate(line: String): Boolean {
        val upper = line.uppercase()
        if (line.length < 3) return false
        if (!line.any { it.isLetter() }) return false
        if (upper.contains("DESCUENTO")) return false
        if (upper.matches(Regex("""^[A-Z]{1,3}\d{2,4}$"""))) return false
        if (line.matches(Regex("""^-?\d+[,.]\d{1,2}$"""))) return false
        if (upper.matches(Regex("""^\d+\s*X\s*\($"""))) return false
        return true
    }

    private fun isPriceCandidate(line: String): Boolean {
        return line.matches(Regex("""^\d+[,.]\d{1,2}$"""))
    }

    private fun hasTrailingPrice(line: String): Boolean {
        return line.matches(Regex(""".+\s+\d+[,.]\d{1,2}$"""))
    }

    suspend fun saveTicket(ticket: Ticket): Long {
        return ticketRepository.saveTicket(ticket)
    }

    fun close() {
        ocrExtractor.close()
    }
}

