package com.jose.listacompra.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// ============================================
// COLOR SCHEMES POR DEFECTO (Fallback)
// ============================================

private val DarkColorScheme = darkColorScheme(
    // Primarios
    primary = DarkPrimaryGreen,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    // Secundarios
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    // Terciarios
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    // Fondos
    background = DarkBackground,
    onBackground = DarkOnBackground,
    // Superficies
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    // Error
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    // Otros
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
    inverseSurface = DarkOnBackground,
    inverseOnSurface = DarkBackground,
    inversePrimary = DarkPrimaryContainer,
    scrim = Color.Black.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// ============================================
// THEME COMPOSABLE
// ============================================

@Composable
fun ListaCompraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    primaryColorInt: Int? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // Determinar si usamos tema oscuro basado en la preferencia
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    
    // Construir el color scheme
    val colorScheme = when {
        // Si hay un color personalizado, generar esquema basado en él
        primaryColorInt != null -> {
            val customColors = generateColorScheme(primaryColorInt)
            if (darkTheme) {
                // Para modo oscuro, adaptar los colores personalizados con fondos oscuros definidos
                val adjustedPrimary = if (customColors.primary == GreenPrimary) DarkPrimaryGreen else customColors.primary
                darkColorScheme(
                    primary = adjustedPrimary,
                    onPrimary = DarkOnPrimary,
                    primaryContainer = customColors.primary.copy(alpha = 0.25f),
                    onPrimaryContainer = adjustedPrimary,
                    secondary = customColors.secondary,
                    onSecondary = DarkOnSecondary,
                    secondaryContainer = customColors.secondary.copy(alpha = 0.25f),
                    onSecondaryContainer = customColors.secondary,
                    tertiary = customColors.tertiary,
                    onTertiary = DarkOnTertiary,
                    tertiaryContainer = customColors.tertiary.copy(alpha = 0.25f),
                    onTertiaryContainer = customColors.tertiary,
                    // Fondos y superficies oscuros según especificación
                    background = DarkBackground,
                    onBackground = DarkOnBackground,
                    surface = DarkSurface,
                    onSurface = DarkOnSurface,
                    surfaceVariant = DarkSurfaceVariant,
                    onSurfaceVariant = DarkOnSurfaceVariant,
                    // Errores
                    error = DarkError,
                    onError = DarkOnError,
                    errorContainer = DarkErrorContainer,
                    onErrorContainer = DarkOnErrorContainer,
                    // Otros
                    outline = DarkOutline,
                    outlineVariant = DarkSurfaceVariant
                )
            } else {
                lightColorScheme(
                    primary = customColors.primary,
                    onPrimary = customColors.onPrimary,
                    primaryContainer = customColors.primaryContainer,
                    onPrimaryContainer = customColors.onPrimaryContainer,
                    secondary = customColors.secondary,
                    onSecondary = customColors.onSecondary,
                    secondaryContainer = customColors.secondaryContainer,
                    onSecondaryContainer = customColors.onSecondaryContainer,
                    tertiary = customColors.tertiary,
                    onTertiary = customColors.onTertiary,
                    tertiaryContainer = customColors.tertiaryContainer,
                    onTertiaryContainer = customColors.onTertiaryContainer
                )
            }
        }
        // Colores dinámicos del sistema (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Esquemas por defecto
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Actualizar barras del sistema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}