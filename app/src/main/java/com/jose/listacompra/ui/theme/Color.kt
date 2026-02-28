package com.jose.listacompra.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

// Colores por defecto del sistema
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Colores predefinidos de la app
val GreenPrimary = Color(0xFF4CAF50)
val BluePrimary = Color(0xFF2196F3)
val RedPrimary = Color(0xFFF44336)
val OrangePrimary = Color(0xFFFF9800)
val PurplePrimary = Color(0xFF9C27B0)

/**
 * Genera un esquema de colores Material3 basado en un color primario personalizado.
 * Usa HSL para calcular variantes ligeras/oscuras del color base.
 */
fun generateColorScheme(primaryColorInt: Int): CustomColorScheme {
    val primary = Color(primaryColorInt)
    val primaryArgb = primaryColorInt
    
    // Generar color más claro para superficies/contenedores
    val primaryContainer = Color(
        ColorUtils.blendARGB(primaryArgb, Color.White.toArgb(), 0.7f)
    )
    
    // Generar color más oscuro para onPrimary
    val onPrimary = if (isLightColor(primary)) Color.Black else Color.White
    
    // Color onPrimaryContainer basado en el color primario
    val onPrimaryContainer = primary.copy(alpha = 0.8f)
    
    // Generar colores secundarios y terciarios basados en el primario
    // Variaciones de tono (hue shift) para crear armonía
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(primaryArgb, hsl)
    
    // Secundario: ligero shift en el tono
    val secondaryHsl = hsl.copyOf().apply { 
        this[0] = (this[0] + 30) % 360 
    }
    val secondary = Color(ColorUtils.HSLToColor(secondaryHsl))
    val secondaryContainer = Color(
        ColorUtils.blendARGB(ColorUtils.HSLToColor(secondaryHsl), Color.White.toArgb(), 0.7f)
    )
    
    // Terciario: otro shift en el tono
    val tertiaryHsl = hsl.copyOf().apply { 
        this[0] = (this[0] - 30 + 360) % 360 
    }
    val tertiary = Color(ColorUtils.HSLToColor(tertiaryHsl))
    val tertiaryContainer = Color(
        ColorUtils.blendARGB(ColorUtils.HSLToColor(tertiaryHsl), Color.White.toArgb(), 0.7f)
    )
    
    return CustomColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onPrimary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onPrimaryContainer,
        tertiary = tertiary,
        onTertiary = onPrimary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onPrimaryContainer
    )
}

/**
 * Determina si un color es claro u oscuro para elegir el color de texto adecuado
 */
fun isLightColor(color: Color): Boolean {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    return hsl[2] > 0.5f
}

/**
 * Esquema de colores personalizado basado en un color primario
 */
data class CustomColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color
)
