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
            debug += "1. URI recibido: $uri"
            debug += "2. Artículos cargados: ${articulos.size}"
            debug += "3. Categorías cargadas: ${categories.size}"

            val ocrResult = ocrExtractor.extractTextFromPdf(uri)
            if (ocrResult.isFailure) {
                debug += "4. Error extrayendo texto: ${ocrResult.exceptionOrNull()?.message ?: "desconocido"}"
                return Result.failure(ocrResult.exceptionOrNull() ?: Exception("Error al extraer texto del PDF"))
            }

            val rawText = ocrResult.getOrNull() ?: ""
            debug += "4. Texto extraído: ${rawText.length} caracteres"
            debug += "5. Primeras líneas: ${rawText.lines().filter { it.isNotBlank() }.take(5).joinToString(" | ")}" 

            if (rawText.isBlank()) {
                return Result.failure(Exception("No se ha podido extraer texto del PDF"))
            }

            val parseResult = CarrefourTicketParser.parse(rawText)
            debug += "6. Parser -> productos: ${parseResult.ticket.lines.size}, total: ${parseResult.ticket.total}, fecha: ${parseResult.ticket.fecha}"

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
            debug += "7. Matching -> sin match: $unmatchedCount"

            if (ticket.lines.isEmpty() || ticket.total <= 0f) {
                debug += "8. Resultado inválido: sin productos o total 0"
                return Result.failure(Exception("Importación vacía: no se han detectado productos o total del ticket"))
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
            debug += "X. Excepción: ${e.message ?: "desconocida"}"
            Result.failure(Exception(debug.joinToString("\n"), e))
        }
    }

    suspend fun saveTicket(ticket: Ticket): Long {
        return ticketRepository.saveTicket(ticket)
    }

    fun close() {
        ocrExtractor.close()
    }
}
