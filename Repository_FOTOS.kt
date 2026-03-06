package com.jose.listacompra.data.repository

import com.jose.listacompra.domain.model.Product

/**
 * METODOS para anadir al ShoppingListRepository.kt
 * Seccion de fotos para productos
 */
class RepositoryFotosMethods {
    
    /*
     * Guardar esto en ShoppingListRepository.kt, despues de los metodos existentes
     */
    
    /**
     * Actualiza la foto de un producto
     * @param productId ID del producto
     * @param photoUri URI de la imagen (null para eliminar)
     */
    suspend fun updateProductPhoto(productId: Long, photoUri: String?) {
        val product = productDao.getProductById(productId) ?: return
        
        val updatedEntity = product.copy(
            photoUri = photoUri,
            photoTimestamp = if (photoUri != null) System.currentTimeMillis() else null,
            isPhotoUserSelected = photoUri != null
        )
        
        productDao.updateProduct(updatedEntity)
    }
    
    /**
     * Elimina la foto de un producto
     */
    suspend fun deleteProductPhoto(productId: Long) {
        updateProductPhoto(productId, null)
    }
    
    /**
     * Guarda una foto para un producto nuevo
     * Usar despues de insertar el producto
     */
    suspend fun saveProductWithPhoto(
        productId: Long,
        photoUri: String
    ) {
        updateProductPhoto(productId, photoUri)
    }
}

/**
 * Importante: asegurate de tener estos imports en Repository.kt:
 * 
 * import android.content.Context
 * import android.net.Uri
 * import java.io.File
 * import java.io.FileOutputStream
 */

/**
 * Metodo adicional para guardar archivo de imagen en almacenamiento interno
 * (opcional, si quieres copiar la imagen a tu app)
 */
fun saveImageToInternalStorageExample(context: Context, sourceUri: Uri, filename: String): String {
    val imagesDir = File(context.filesDir, "product_images").apply {
        if (!exists()) mkdirs()
    }
    
    val destFile = File(imagesDir, filename)
    
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(destFile).use { output ->
            input.copyTo(output)
        }
    }
    
    return destFile.absolutePath
}
