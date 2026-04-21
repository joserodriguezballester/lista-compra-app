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
    ): Result<ImportResult> = processTextResult(
        textResult = ocrExtractor.extractTextFromPdf(uri),
        sourceLabel = "PDF: $uri",
        articulos = articulos,
        categories = categories
    )

    suspend fun invokeDebugAsset(
        assetName: String,
        articulos: List<Articulo>,
        categories: List<Category>
    ): Result<ImportResult> = processTextResult(
        textResult = ocrExtractor.extractTextFromAsset(assetName),
        sourceLabel = "ASSET: $assetName",
        articulos = articulos,
        categories = categories
    )

    private fun processTextResult(
        textResult: Result<String>,
        sourceLabel: String,
        articulos: List<Articulo>,
        categories: List<Category>
    ): Result<ImportResult> {
        val debug = mutableListOf<String>()
        return try {
            debug += "SOURCE=$sourceLabel"

            if (textResult.isFailure) {
                val cause = textResult.exceptionOrNull()
                val causeMessage = cause?.message ?: "Error desconocido al leer el PDF"
                debug += "TEXT_EXTRACTION_ERROR=$causeMessage"
                return Result.failure(Exception(debug.joinToString(" | "), cause))
            }

            val rawText = textResult.getOrNull() ?: ""
            debug += "Texto: ${rawText.length} chars"

            val parseResult = CarrefourTicketParser.parse(rawText)
            debug += parseResult.debugDate
            debug += parseResult.warnings
            debug += "PARSED_COUNT=${parseResult.ticket.lines.size}"
            debug += "TICKET_TOTAL=%.2f".format(parseResult.ticket.total)

            val matchedLines = parseResult.ticket.lines.mapIndexed { index, line ->
                val match = ProductMatcher.findBestMatch(
                    normalizedName = line.nombreNormalizado,
                    articulos = articulos
                )

                val suggestedCategory = if (match == null) {
                    ProductMatcher.assignCategory(line.nombreOriginal, categories)
                } else {
                    articulos.find { it.id == match.id }?.categoryId
                }

                debug += "MATCH_INPUT[$index]={name=${line.nombreOriginal}, normalized=${line.nombreNormalizado}, qty=${line.cantidad}, unit=${"%.2f".format(line.precioUnitario)}, total=${"%.2f".format(line.precioTotal)}}"
                debug += if (match != null) {
                    "MATCH_RESULT[$index]={articuloId=${match.id}, articulo=${match.name}}"
                } else {
                    "MATCH_RESULT[$index]={articuloId=null, articulo=null}"
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

    suspend fun saveTicket(ticket: Ticket): Long = ticketRepository.saveTicket(ticket)
    fun close() { ocrExtractor.close() }
}
