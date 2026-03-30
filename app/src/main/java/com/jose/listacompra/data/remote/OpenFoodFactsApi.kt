package com.jose.listacompra.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): OpenFoodFactsResponse
}

data class OpenFoodFactsResponse(
    val status: Int,
    val product: ProductInfo?
)

data class ProductInfo(
    val product_name: String?,
    val product_name_es: String?,
    val brands: String?,
    val image_url: String?,
    val image_small_url: String?,
    val categories: String?,
    val categories_tags: List<String>?,
    val quantity: String?,
    val serving_size: String?
) {
    fun getBestName(): String? = product_name_es ?: product_name
    
    fun getBestImage(): String? = image_url ?: image_small_url
}
