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

            val starsIndex = lines.indexOfFirst { it.all { ch -> ch == '*' } || it.contains("***") }
            val equalsIndex = lines.indexOfFirst { it.all { ch -> ch == '=' } || it.contains("===") }
            debug += "***: $starsIndex"
            debug += "===: $equalsIndex"

            val section = if (starsIndex >= 0 && equalsIndex > starsIndex) {
                lines.subList(starsIndex + 1, equalsIndex)
            } else {
                lines
            }

            val firstName = section.firstOrNull { isNameCandidate(it) } ?: "-"
            val firstPrice = section.firstOrNull { isPriceCandidate(it) } ?: "-"
            debug += "FirstName: ${firstName.take(45)}"
            debug += "FirstPrice: ${firstPrice.take(45)}"

            val parseResult = CarrefourTicketParser.parse(rawText)
            debug += "Productos: ${parseResult.ticket.lines.size}"
            debug += "Total: %.2f".format(parseResult.ticket.total)

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

    suspend fun saveTicket(ticket: Ticket): Long {
        return ticketRepository.saveTicket(ticket)
    }

    fun close() {
        ocrExtractor.close()
    }
}
