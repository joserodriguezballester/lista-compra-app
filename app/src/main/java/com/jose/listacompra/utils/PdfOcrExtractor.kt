package com.jose.listacompra.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Utilidad para extraer texto de PDFs usando OCR.
 * Usa ML Kit de Google para reconocimiento de texto.
 */
class PdfOcrExtractor(private val context: Context) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extrae texto de un archivo PDF desde su URI.
     * @param uri URI del archivo PDF
     * @return Texto extraído de todas las páginas
     */
    suspend fun extractTextFromPdf(uri: Uri): Result<String> {
        return try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return Result.failure(Exception("No se pudo abrir el archivo"))

            val text = extractTextFromPdfInternal(parcelFileDescriptor)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extrae texto de un archivo PDF desde su ruta.
     */
    suspend fun extractTextFromPdf(filePath: String): Result<String> {
        val file = File(filePath)
        if (!file.exists()) {
            return Result.failure(Exception("Archivo no encontrado: $filePath"))
        }
        return extractTextFromPdf(Uri.fromFile(file))
    }

    /**
     * Implementación interna de extracción.
     */
    private suspend fun extractTextFromPdfInternal(pfd: ParcelFileDescriptor): String {
        val pdfRenderer = PdfRenderer(pfd)
        val textBuilder = StringBuilder()

        for (pageIndex in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(pageIndex)
            val bitmap = renderPageToBitmap(page)
            page.close()

            val pageText = extractTextFromBitmap(bitmap)
            textBuilder.append(pageText).append("\n")
        }

        pdfRenderer.close()
        pfd.close()

        return textBuilder.toString()
    }

    /**
     * Renderiza una página del PDF a Bitmap.
     */
    private fun renderPageToBitmap(page: PdfRenderer.Page): Bitmap {
        // Escalar para mejor OCR (2x para mejor precisión)
        val scale = 2.0f
        val width = (page.width * scale).toInt()
        val height = (page.height * scale).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    /**
     * Extrae texto de un Bitmap usando ML Kit.
     */
    private suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val result = textRecognizer.process(inputImage).await()
        return result.text
    }

    /**
     * Libera los recursos del reconocedor.
     */
    fun close() {
        textRecognizer.close()
    }
}
