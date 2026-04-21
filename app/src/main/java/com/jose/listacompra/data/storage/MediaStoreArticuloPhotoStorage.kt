package com.jose.listacompra.data.storage

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.jose.listacompra.domain.storage.ArticuloPhotoStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.Normalizer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreArticuloPhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : ArticuloPhotoStorage {

    companion object {
        private const val DIRECTORY_SEGMENT = "ListaCompra/Articulos"
        private const val DEFAULT_MIME = "image/jpeg"
        private const val RELATIVE_PATH = "Pictures/ListaCompra/Articulos/"
    }

    override suspend fun centralizeIfNeeded(photoUri: String, articuloName: String): String = withContext(Dispatchers.IO) {
        if (photoUri.isBlank() || isAlreadyCentralized(photoUri)) {
            return@withContext photoUri
        }

        val uri = Uri.parse(photoUri)
        return@withContext when (uri.scheme?.lowercase()) {
            "http", "https" -> downloadRemoteImage(photoUri, articuloName)
            "content", "file" -> copyLocalImage(uri, articuloName)
            null -> copyLocalPath(photoUri, articuloName)
            else -> copyLocalImage(uri, articuloName)
        }
    }

    private fun copyLocalPath(path: String, articuloName: String): String {
        val file = File(path)
        require(file.exists()) { "No se pudo abrir la imagen local: $path" }

        FileInputStream(file).use { input ->
            return writeToCanonicalLocation(
                input = input,
                mimeType = guessMimeTypeFromName(file.name) ?: DEFAULT_MIME,
                articuloName = articuloName
            )
        }
    }

    private fun copyLocalImage(uri: Uri, articuloName: String): String {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
            ?: guessMimeTypeFromName(uri.lastPathSegment)
            ?: DEFAULT_MIME

        val inputStream = when (uri.scheme?.lowercase()) {
            "content" -> resolver.openInputStream(uri)
            "file" -> uri.path?.let { FileInputStream(File(it)) }
            else -> null
        } ?: throw IllegalArgumentException("No se pudo abrir la imagen local: $uri")

        inputStream.use { input ->
            return writeToCanonicalLocation(input, mimeType, articuloName)
        }
    }

    private fun downloadRemoteImage(url: String, articuloName: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            requestMethod = "GET"
            doInput = true
        }

        return try {
            val mimeType = connection.contentType
                ?.substringBefore(';')
                ?.takeIf { it.startsWith("image/") }
                ?: guessMimeTypeFromName(url)
                ?: DEFAULT_MIME

            connection.inputStream.use { input ->
                writeToCanonicalLocation(input, mimeType, articuloName)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun writeToCanonicalLocation(input: InputStream, mimeType: String, articuloName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToMediaStore(input, sanitizeMimeType(mimeType), articuloName)
        } else {
            writeToPublicPicturesDirectory(input, sanitizeMimeType(mimeType), articuloName)
        }
    }

    private fun writeToMediaStore(input: InputStream, mimeType: String, articuloName: String): String {
        val resolver = context.contentResolver
        val targetUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, buildDisplayName(mimeType, articuloName))
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, RELATIVE_PATH)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        ) ?: throw IllegalStateException("No se pudo crear la imagen en MediaStore")

        return try {
            resolver.openOutputStream(targetUri)?.use { output ->
                input.copyTo(output)
            } ?: throw IllegalStateException("No se pudo abrir la salida para $targetUri")

            resolver.update(
                targetUri,
                ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                },
                null,
                null
            )

            targetUri.toString()
        } catch (e: Exception) {
            resolver.delete(targetUri, null, null)
            throw e
        }
    }

    @Suppress("DEPRECATION")
    private fun writeToPublicPicturesDirectory(input: InputStream, mimeType: String, articuloName: String): String {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val targetDir = File(picturesDir, DIRECTORY_SEGMENT)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        require(targetDir.exists()) { "No se pudo crear la carpeta de fotos de artículos" }

        val targetFile = File(targetDir, buildDisplayName(mimeType, articuloName))
        FileOutputStream(targetFile).use { output ->
            input.copyTo(output)
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(targetFile.absolutePath),
            arrayOf(mimeType),
            null
        )

        return Uri.fromFile(targetFile).toString()
    }

    private fun isAlreadyCentralized(photoUri: String): Boolean {
        val uri = Uri.parse(photoUri)
        return when (uri.scheme?.lowercase()) {
            "content" -> isManagedMediaStoreUri(uri)
            "file" -> uri.path?.let(::isCanonicalFilePath) == true
            null -> isCanonicalFilePath(photoUri)
            else -> false
        }
    }

    private fun isManagedMediaStoreUri(uri: Uri): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        if (uri.authority != MediaStore.AUTHORITY) return false

        val resolver = context.contentResolver
        val relativePath = resolver.query(
            uri,
            arrayOf(MediaStore.Images.Media.RELATIVE_PATH),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            if (index == -1) null else cursor.getString(index)
        }

        return relativePath == RELATIVE_PATH
    }

    private fun isCanonicalFilePath(path: String): Boolean {
        return path.contains("/Pictures/$DIRECTORY_SEGMENT/")
    }

    private fun sanitizeMimeType(mimeType: String): String {
        return mimeType.takeIf { it.startsWith("image/") } ?: DEFAULT_MIME
    }

    private fun buildDisplayName(mimeType: String, articuloName: String): String {
        val extension = when (mimeType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
        val shortId = UUID.randomUUID().toString().substringBefore('-')
        val slug = slugifyName(articuloName)
        return "$slug-$shortId.$extension"
    }

    private fun slugifyName(articuloName: String): String {
        val normalized = Normalizer.normalize(articuloName.trim(), Normalizer.Form.NFD)
        return normalized
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .replace("[^a-z0-9]+".toRegex(), "-")
            .trim('-')
            .take(40)
            .ifBlank { "articulo" }
    }

    private fun guessMimeTypeFromName(name: String?): String? {
        if (name.isNullOrBlank()) return null
        return URLConnection.guessContentTypeFromName(name)?.substringBefore(';')
    }
}
