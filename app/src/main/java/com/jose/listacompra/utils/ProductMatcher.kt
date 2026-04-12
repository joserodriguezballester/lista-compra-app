package com.jose.listacompra.utils


import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category

/**
 * Utilidades para matching de productos con fuzzy search.
 */
object ProductMatcher {
    /**
     * Calcula la distancia de Levenshtein entre dos strings.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[s1.length][s2.length]
    }



    // Palabras clave para categorías automáticas
    private val categoryKeywords = mapOf(
        "Aceites" to listOf("aceite", "oliva", "girasol", "soja"),
        "Lácteos" to listOf("leche", "yogur", "yogurt", "queso", "nata", "mantequilla", "flan", "natillas"),
        "Carnes" to listOf("pollo", "carne", "cerdo", "ternera", "jamón", "jamon", "chuleta", "picada"),
        "Pescados" to listOf("pescado", "merluza", "salmón", "atún", "atun", "bacalao"),
        "Frutas" to listOf("manzana", "plátano", "platano", "naranja", "pera", "fresa", "fresón", "uva", "melón", "melon"),
        "Verduras" to listOf("tomate", "lechuga", "zanahoria", "calabacín", "calabacin", "berenjena", "pepino", "cebolla"),
        "Panadería" to listOf("pan", "barra", "panecillo", "tostada"),
        "Bebidas" to listOf("agua", "zumo", "refresco", "coca", "cerveza", "vino"),
        "Limpieza" to listOf("detergente", "lejía", "fregasuelos", "lavavajillas", "suavizante"),
        "Higiene" to listOf("champú", "champu", "gel", "jabón", "jabon", "pasta dental", "colonia"),
        "Congelados" to listOf("congelado", "helado", "pizza", "croquetas"),
        "Despensa" to listOf("arroz", "pasta", "espagueti", "macarrones", "harina", "azúcar", "azucar", "sal"),
        "Aperitivos" to listOf("patatas", "chips", "galletas", "palomitas", "frutos secos"),
        "Bebé" to listOf("pañales", "pañal", "bebé", "bebe", "papilla")
    )

    // Palabras a ignorar en el matching
    private val stopWords = setOf(
        "de", "del", "la", "el", "las", "los", "y", "con", "sin", "para", "por", "en", "un", "una",
        "gr", "g", "ml", "l", "kg", "uds", "ud", "unidad", "unidades"
    )

    /**
     * Busca el mejor match para un nombre de producto en el catálogo.
     * @param normalizedName Nombre normalizado del producto del ticket
     * @param articulos Lista de artículos del catálogo
     * @param threshold Umbral mínimo de similitud (0-1)
     * @return El artículo que mejor matchea, o null si no hay match suficiente
     */
    fun findBestMatch(
        normalizedName: String,
        articulos: List<Articulo>,
        threshold: Float = 0.6f
    ): Articulo? {
        if (articulos.isEmpty()) return null

        val scores = articulos.map { articulo ->
            val score = calculateSimilarity(normalizedName, articulo.name)
            articulo to score
        }.sortedByDescending { it.second }

        val bestMatch = scores.firstOrNull()
        return if (bestMatch != null && bestMatch.second >= threshold) {
            bestMatch.first
        } else {
            null
        }
    }

    /**
     * Busca todos los matches posibles ordenados por similitud.
     */
    fun findAllMatches(
        normalizedName: String,
        articulos: List<Articulo>,
        threshold: Float = 0.4f,
        maxResults: Int = 5
    ): List<Pair<Articulo, Float>> {
        return articulos
            .map { it to calculateSimilarity(normalizedName, it.name) }
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
            .take(maxResults)
    }

    /**
     * Calcula la similitud entre dos nombres de producto.
     * Combina múltiples técnicas para mejor matching.
     */
    private fun calculateSimilarity(name1: String, name2: String): Float {
        val n1 = preprocessForMatching(name1)
        val n2 = preprocessForMatching(name2)

        if (n1.isEmpty() || n2.isEmpty()) return 0f

        // 1. Match exacto
        if (n1 == n2) return 1f

        // 2. Contiene el otro
        if (n1.contains(n2) || n2.contains(n1)) return 0.9f

        // 3. Similitud de palabras clave
        val words1 = n1.split(" ").filter { it.length > 2 && it !in stopWords }.toSet()
        val words2 = n2.split(" ").filter { it.length > 2 && it !in stopWords }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) {
            // Fallback a Levenshtein
            return levenshteinSimilarity(n1, n2)
        }

        val commonWords = words1.intersect(words2)
        val wordSimilarity = commonWords.size.toFloat() / maxOf(words1.size, words2.size)

        // 4. Similitud de caracteres (Levenshtein)
        val charSimilarity = levenshteinSimilarity(n1, n2)

        // Combinar scores con pesos
        return (wordSimilarity * 0.6f + charSimilarity * 0.4f)
    }

    /**
     * Preprocesa un nombre para matching.
     */
    private fun preprocessForMatching(name: String): String {
        return name.lowercase()
            .replace(Regex("""[^\w\s]"""), " ") // Quitar puntuación
            .replace(Regex("""\d+"""), " ") // Quitar números
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    /**
     * Calcula similitud usando distancia de Levenshtein.
     */
    private fun levenshteinSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1f
        val distance = levenshteinDistance(s1, s2)
        val maxLength = maxOf(s1.length, s2.length)
        return if (maxLength == 0) 1f else 1f - (distance.toFloat() / maxLength)
    }

    /**
     * Asigna categoría automáticamente basada en palabras clave.
     */
    fun assignCategory(productName: String, categories: List<Category>): Long? {
        val normalized = productName.lowercase()

        for ((categoryName, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (normalized.contains(keyword)) {
                    val category = categories.find {
                        it.name.equals(categoryName, ignoreCase = true)
                    }
                    return category?.id
                }
            }
        }

        return null
    }

    /**
     * Obtiene el nombre de categoría sugerido para un producto.
     */
    fun getSuggestedCategoryName(productName: String): String? {
        val normalized = productName.lowercase()

        for ((categoryName, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (normalized.contains(keyword)) {
                    return categoryName
                }
            }
        }

        return null
    }
}
