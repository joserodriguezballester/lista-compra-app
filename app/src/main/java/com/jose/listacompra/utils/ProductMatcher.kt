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
    private fun levenshteinDistance(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,
                    curr[j - 1] + 1,
                    prev[j - 1] + cost
                )
            }
            for (j in prev.indices) prev[j] = curr[j]
        }

        return prev[b.length]
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
        return if (bestMatch != null && bestMatch.second >= threshold) bestMatch.first else null
    }

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

    private fun calculateSimilarity(name1: String, name2: String): Float {
        val n1 = preprocessForMatching(name1)
        val n2 = preprocessForMatching(name2)

        if (n1.isEmpty() || n2.isEmpty()) return 0f
        if (n1 == n2) return 1f
        if (n1.contains(n2) || n2.contains(n1)) return 0.9f

        val words1 = n1.split(" ").filter { it.length > 2 && it !in stopWords }.toSet()
        val words2 = n2.split(" ").filter { it.length > 2 && it !in stopWords }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return levenshteinSimilarity(n1, n2)

        val commonWords = words1.intersect(words2)
        val wordSimilarity = commonWords.size.toFloat() / maxOf(words1.size, words2.size)
        val charSimilarity = levenshteinSimilarity(n1, n2)

        return (wordSimilarity * 0.6f + charSimilarity * 0.4f)
    }

    private fun preprocessForMatching(name: String): String {
        return name.lowercase()
            .replace(Regex("""[^\w\s]"""), " ")
            .replace(Regex("""\d+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun levenshteinSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1f
        val distance = levenshteinDistance(s1, s2)
        val maxLength = maxOf(s1.length, s2.length)
        return if (maxLength == 0) 1f else 1f - (distance.toFloat() / maxLength)
    }

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
