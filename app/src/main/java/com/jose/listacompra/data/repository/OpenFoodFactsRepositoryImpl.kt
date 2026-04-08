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
    
    /**
     * Mapeo de tags de OpenFoodFacts a IDs de categoría de la app
     * ID 1 = Sin categoría (fallback)
     */
    private val categoryMapping = mapOf(
        // Bebidas (3)
        "en:beverages" to 3L,
        "en:drinks" to 3L,
        "en:waters" to 3L,
        "en:sodas" to 3L,
        "en:fruit-based-beverages" to 3L,
        "en:coffees" to 3L,
        "en:fruit-juices" to 3L,
        
        // Galletas y pasteles (4)
        "en:biscuits-and-cakes" to 4L,
        "en:biscuits" to 4L,
        "en:cakes" to 4L,
        "en:cookies" to 4L,
        "en:pastry" to 4L,
        
        // Panes (5)
        "en:breads" to 5L,
        "en:bakery-products" to 5L,
        "en:bakery" to 5L,
        "en:brioche" to 5L,
        
        // Cereales desayuno (6)
        "en:breakfast-cereals" to 6L,
        "en:cereals" to 6L,
        "en:cereals-and-potato-dishes" to 6L,
        
        // Conservas (7)
        "en:canned-foods" to 7L,
        "en:canned-vegetables" to 7L,
        "en:canned-meats" to 7L,
        "en:canned-fishes" to 7L,
        
        // Quesos (8)
        "en:cheeses" to 8L,
        
        // Chocolates (9)
        "en:chocolates" to 9L,
        "en:confectioneries" to 9L,
        "en:candies" to 9L,
        "en:sweets" to 9L,
        
        // Condimentos (10)
        "en:condiments" to 10L,
        "en:spices" to 10L,
        "en:herbs" to 10L,
        "en:salts" to 10L,
        "en:pepper" to 10L,
        
        // Lácteos (11)
        "en:dairies" to 11L,
        "en:milk" to 11L,
        "en:yogurts" to 11L,
        "en:dairy-products" to 11L,
        
        // Postres (12)
        "en:desserts" to 12L,
        "en:frozen-desserts" to 12L,
        "en:ice-creams" to 12L,
        
        // Productos secos (13)
        "en:dried-products" to 13L,
        "en:legumes" to 13L,
        "en:dried-vegetables" to 13L,
        "en:dried-fruits" to 13L,
        
        // Huevos (14)
        "en:eggs" to 14L,
        
        // Grasas (15)
        "en:fats" to 15L,
        "en:butter" to 15L,
        "en:margarines" to 15L,
        
        // Pescado y marisco (16)
        "en:fishes" to 16L,
        "en:seafood" to 16L,
        "en:fish-and-seafood" to 16L,
        
        // Congelados (17)
        "en:frozen-foods" to 17L,
        "en:frozen-meals" to 17L,
        "en:frozen-vegetables" to 17L,
        
        // Frutas (18)
        "en:fruits" to 18L,
        "en:fresh-fruits" to 18L,
        
        // Verduras (19)
        "en:vegetables" to 19L,
        "en:fresh-vegetables" to 19L,
        "en:leaf-vegetables" to 19L,
        
        // Carnes (20)
        "en:meats" to 20L,
        "en:beef" to 20L,
        "en:pork" to 20L,
        "en:chicken" to 20L,
        "en:poultry" to 20L,
        "en:meat-and-poultry" to 20L,
        "en:delicatessen" to 20L,
        
        // Comidas preparadas (21)
        "en:meals" to 21L,
        "en:prepared-meals" to 21L,
        "en:ready-meals" to 21L,
        
        // Frutos secos (22)
        "en:nuts" to 22L,
        "en:seeds" to 22L,
        
        // Aceites (23)
        "en:oils" to 23L,
        "en:olive-oil" to 23L,
        "en:vegetable-oils" to 23L,
        
        // Pasta (24)
        "en:pasta" to 24L,
        "en:dried-pasta" to 24L,
        
        // Encurtidos (25)
        "en:pickles" to 25L,
        "en:pickled-vegetables" to 25L,
        
        // Pizza (26)
        "en:pizza" to 26L,
        "en:pizzas" to 26L,
        
        // Alimentos vegetales (27)
        "en:plant-based-foods" to 27L,
        "en:plant-based-foods-and-beverages" to 27L,
        "en:vegan-foods" to 27L,
        
        // Salsas (28)
        "en:sauces" to 28L,
        "en:tomato-sauces" to 28L,
        "en:mayonnaises" to 28L,
        "en:ketchup" to 28L,
        
        // Aperitivos (29)
        "en:snacks" to 29L,
        "en:chips" to 29L,
        "en:crisps" to 29L,
        "en:salty-snacks" to 29L,
        "en:appetizers" to 29L,
        
        // Sopas (30)
        "en:soups" to 30L,
        "en:dried-soups" to 30L,
        
        // Untables (31)
        "en:spreads" to 31L,
        "en:honey" to 31L,
        "en:jams" to 31L,
        "en:chocolate-spreads" to 31L,
        "en:nut-butters" to 31L,
        
        // Azúcares (32)
        "en:sugars" to 32L,
        "en:sweeteners" to 32L,
        "en:sugar" to 32L,
        
        // Tés e infusiones (33)
        "en:teas" to 33L,
        "en:tea" to 33L,
        "en:herbal-teas" to 33L,
        "en:infusions" to 33L,
        
        // Bebidas alcohólicas (34)
        "en:alcoholic-beverages" to 34L,
        "en:beers" to 34L,
        "en:wines" to 34L,
        "en:spirits" to 34L,
        "en:ciders" to 34L,
        
        // Higiene y belleza (35)
        "en:hygiene" to 35L,
        "en:beauty-products" to 35L,
        "en:cosmetics" to 35L,
        
        // Limpieza (36)
        "en:cleaning-products" to 36L,
        "en:detergents" to 36L,
        
        // Alimentos infantiles (2)
        "en:baby-foods" to 2L,
        "en:baby-products" to 2L,
        "en:baby-milk" to 2L,
        "en:baby-meals" to 2L
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
    
    /**
     * Mapea las categorías de OpenFoodFacts a ID de categoría de la app
     * Devuelve 1 (Sin categoría) si no hay mapeo
     */
    private fun mapCategory(product: ProductInfo): String? {
        val tags = product.categories_tags ?: return null
        
        // Buscar la primera coincidencia en el mapeo
        for (tag in tags) {
            val categoryId = categoryMapping[tag]
            if (categoryId != null) {
                Log.d(TAG, "Mapeado '$tag' → categoría $categoryId")
                return categoryId.toString()
            }
        }
        
        // No se encontró mapeo → categoría 1 (Sin categoría)
        Log.d(TAG, "Sin mapeo para tags: $tags → categoría 1")
        return "1"
    }
}
