package com.jose.listacompra.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PdfOcrExtractor(private val context: Context) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractTextFromPdf(uri: Uri): Result<String> {
        return try {
            val directText = extractTextDirectly(uri)
            if (directText.isSuccess && directText.getOrNull()?.isNotBlank() == true) {
                val text = directText.getOrNull()!!
                if (text.lines().filter { it.isNotBlank() }.size >= 5) {
                    return Result.success(text)
                }
            }
            extractTextWithOcr(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractTextFromAsset(assetName: String): Result<String> {
        return try {
            val directText = extractTextDirectlyFromAsset(assetName)
            if (directText.isSuccess && directText.getOrNull()?.isNotBlank() == true) {
                val text = directText.getOrNull()!!
                if (text.lines().filter { it.isNotBlank() }.size >= 5) {
                    return Result.success(text)
                }
            }

            val assetFile = copyAssetToCache(assetName)
            extractTextWithOcr(Uri.fromFile(assetFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractTextDirectly(uri: Uri): Result<String> {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("No se pudo abrir el archivo"))
            inputStream.use { stream ->
                val document = PDDocument.load(stream)
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                val text = stripper.getText(document)
                document.close()
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractTextDirectlyFromAsset(assetName: String): Result<String> {
        return try {
            context.assets.open(assetName).use { stream ->
                val document = PDDocument.load(stream)
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                val text = stripper.getText(document)
                document.close()
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyAssetToCache(assetName: String): File {
        val outFile = File(context.cacheDir, assetName.substringAfterLast('/'))
        context.assets.open(assetName).use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
        return outFile
    }

    private suspend fun extractTextWithOcr(uri: Uri): Result<String> {
        return try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return Result.failure(Exception("No se pudo abrir el archivo"))
            val text = extractTextWithOcrInternal(parcelFileDescriptor)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun extractTextWithOcrInternal(pfd: ParcelFileDescriptor): String {
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

    private fun renderPageToBitmap(page: PdfRenderer.Page): Bitmap {
        val scale = 2.0f
        val width = (page.width * scale).toInt()
        val height = (page.height * scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    private suspend fun extractTextFromBitmap(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(inputImage)
            .addOnSuccessListener { result -> continuation.resume(result.text) }
            .addOnFailureListener { e -> continuation.resumeWithException(e) }
    }

    fun close() { textRecognizer.close() }
}
