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

/**
 * Resultado de la importación de un ticket.
 */
data class ImportResult(
    val ticket: Ticket,
    val unmatchedCount: Int,
    val warnings: List<String> = emptyList()
)

/**
 * Importa un ticket desde PDF, extrae texto con OCR, parsea productos
 * y hace matching con el catálogo existente.
 */
class ImportTicketUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ticketRepository: ITicketRepository
) {
    private val ocrExtractor = PdfOcrExtractor(context)

    /**
     * Importa un ticket desde un archivo PDF.
     * @param uri URI del archivo PDF
     * @param articulos Lista de artículos del catálogo para matching
     * @param categories Lista de categorías para asignación automática
     * @return Resultado de la importación con el ticket parseado
     */
    suspend operator fun invoke(
        uri: Uri,
        articulos: List<Articulo>,
        categories: List<Category>
    ): Result<ImportResult> {
        return try {
            // 1. Extraer texto del PDF con OCR
            val ocrResult = ocrExtractor.extractTextFromPdf(uri)
            if (ocrResult.isFailure) {
                return Result.failure(ocrResult.exceptionOrNull() ?: Exception("Error en OCR"))
            }

            val rawText = ocrResult.getOrNull() ?: ""

            // 2. Parsear el texto del ticket
            val parseResult = CarrefourTicketParser.parse(rawText)

            // 3. Hacer matching de productos con el catálogo
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

            // 4. Contar productos sin match
            val unmatchedCount = matchedLines.count { it.articuloId == null }

            // 5. Crear ticket con líneas actualizadas
            val ticket = parseResult.ticket.copy(lines = matchedLines)

            Result.success(
                ImportResult(
                    ticket = ticket,
                    unmatchedCount = unmatchedCount,
                    warnings = parseResult.warnings
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda el ticket importado después de que el usuario lo revise.
     */
    suspend fun saveTicket(ticket: Ticket): Long {
        return ticketRepository.saveTicket(ticket)
    }

    /**
     * Libera recursos del OCR.
     */
    fun close() {
        ocrExtractor.close()
    }
}
