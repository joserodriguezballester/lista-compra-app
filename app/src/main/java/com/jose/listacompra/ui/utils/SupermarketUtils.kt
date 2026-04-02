package com.jose.listacompra.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.jose.listacompra.R

/**
 * Mapea el nombre del supermercado al recurso del logo
 * @param name Nombre del supermercado (ej: "Mercadona", "Carrefour")
 * @return ID del recurso drawable o null si no hay logo
 */
fun getSupermarketLogoRes(name: String): Int? {
    val lower = name.lowercase()
    return when {
        lower.contains("mercadona") -> R.drawable.logo_mercadona
        lower.contains("carrefour") -> R.drawable.logo_carrefour
        lower.contains("lidl") -> R.drawable.logo_lidl
        lower.contains("aldi") -> R.drawable.logo_aldi
        lower.contains("dia") -> R.drawable.logo_dia
        lower.contains("consum") -> R.drawable.logo_consum
        else -> null
    }
}

/**
 * Obtiene el emoji del supermercado
 */
fun getSupermarketEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("mercadona") -> "🟢"
        lower.contains("carrefour") -> "🔵"
        lower.contains("lidl") -> "🟡"
        lower.contains("aldi") -> "🟠"
        lower.contains("dia") -> "🔴"
        lower.contains("consum") -> "🟣"
        else -> "🏪"
    }
}

/**
 * Obtiene el color principal del supermercado (para temas personalizados)
 */
fun getSupermarketColor(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("mercadona") -> "#4CAF50"  // Verde
        lower.contains("carrefour") -> "#1976D2" // Azul
        lower.contains("lidl") -> "#FFEB3B"      // Amarillo
        lower.contains("aldi") -> "#FF9800"     // Naranja
        lower.contains("dia") -> "#F44336"      // Rojo
        lower.contains("consum") -> "#9C27B0"   // Púrpura
        else -> "#757575"                        // Gris
    }
}