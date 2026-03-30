package com.jose.listacompra.data.repository

import android.util.Log
import com.jose.listacompra.data.remote.OpenFoodFactsApi
import com.jose.listacompra.data.remote.ProductInfo
import com.jose.listacompra.domain.model.ScannedProduct
import com.jose.listacompra.domain.repository.IOpenFoodFactsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenFoodFactsRepositoryImpl @Inject constructor() : IOpenFoodFactsRepository {
    
    companion object {
        private const val TAG = "OpenFoodFacts"
        private const val BASE_URL = "https://world.openfoodfacts.org/"
    }
    
    private val api: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
    
    // Mapeo de categorías de OpenFoodFacts a categorías de la app
    private val categoryMapping = mapOf(
        "en:fruits" to 1L,
        "en:vegetables" to 1L,
        "en:meats" to 2L,
        "en:beef" to 2L,
        "en:pork" to 2L,
        "en:chicken" to 2L,
        "en:fishes" to 3L,
        "en:seafood" to 3L,
        "en:dairies" to 4L,
        "en:milk" to 4L,
        "en:cheeses" to 4L,
        "en:yogurts" to 4L,
        "en:breads" to 5L,
        "en:bakery" to 5L,
        "en:beverages" to 6L,
        "en:drinks" to 6L,
        "en:waters" to 6L,
        "en:sodas" to 6L,
        "en:canned-foods" to 7L,
        "en:pasta" to 7L,
        "en:frozen-foods" to 8L,
        "en:hygiene" to 9L,
        "en:beauty-products" to 9L,
        "en:cleaning-products" to 10L,
        "en:pet-food" to 11L,
        "en:baby-foods" to 12L,
        "en:baby-products" to 12L
    )
    
    override suspend fun getProductByBarcode(barcode: String): ScannedProduct? {
        return try {
            Log.d(TAG, "Buscando producto con código: $barcode")
            val response = api.getProductByBarcode(barcode)
            
            if (response.status == 1 && response.product != null) {
                val product = response.product
                val scannedProduct = ScannedProduct(
                    barcode = barcode,
                    name = product.getBestName(),
                    brand = product.brands,
                    imageUrl = product.getBestImage(),
                    quantity = product.quantity,
                    categoryTag = mapCategory(product)
                )
                Log.d(TAG, "Producto encontrado: $scannedProduct")
                scannedProduct
            } else {
                Log.d(TAG, "Producto no encontrado para código: $barcode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar producto", e)
            null
        }
    }
    
    private fun mapCategory(product: ProductInfo): String? {
        val tags = product.categories_tags ?: return null
        
        for (tag in tags) {
            val categoryId = categoryMapping[tag]
            if (categoryId != null) {
                return categoryId.toString()
            }
        }
        
        return null
    }
}
