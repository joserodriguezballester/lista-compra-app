package com.jose.listacompra.ui.components

import android.util.Log

/**
 * Parsea comandos de voz del tipo:
 * - "3 de leche"
 * - "2 kilos de patatas"
 * - "medio kilo de jamón"
 * - "1 litro de aceite"
 */
fun parseVoiceCommand(text: String): VoiceResult {
    val normalized = text.lowercase().trim()
    
    Log.d("VoiceParser", "Parseando: '$normalized'")
    
    // Patrones comunes
    val patterns = listOf(
        // "3 de leche" → qty=3, product=leche
        Regex("^(\\d+)\\s+(?:de\\s+)?(.+)"),
        // "2 kilos de patatas" → qty=2, unit=kilos, product=patatas
        Regex("^(\\d+)\\s+(kilo|kilos|kg|gramo|gramos|g|litro|litros|l|ml|unidad|unidades|uds?|paquete|paquetes)\\s+(?:de\\s+)?(.+)"),
        // "medio kilo de jamón" → qty=0.5, unit=kilo, product=jamón
        Regex("^(medio|media)\\s+(kilo|kilogramo|litro)\\s+(?:de\\s+)?(.+)"),
        // "un kilo de..." → qty=1
        Regex("^(un|una|1)\\s+(kilo|kilogramo|litro|paquete)\\s+(?:de\\s+)?(.+)"),
        // Solo el nombre → qty=1
        Regex("^(.+)$")
    )
    
    for (pattern in patterns) {
        val match = pattern.find(normalized)
        if (match != null) {
            return when (match.groupValues.size) {
                3 -> {
                    // "3 de leche"
                    val qty = match.groupValues[1].toFloatOrNull() ?: 1f
                    val product = match.groupValues[2].trim()
                    VoiceResult(
                        text = text,
                        quantity = qty,
                        unit = ""
                    )
                }
                4 -> {
                    val group1 = match.groupValues[1]
                    val group2 = match.groupValues[2]
                    val group3 = match.groupValues[3]
                    
                    if (group1 == "medio" || group1 == "media") {
                        // "medio kilo de jamón"
                        VoiceResult(
                            text = text,
                            quantity = 0.5f,
                            unit = group2
                        )
                    } else {
                        // "2 kilos de patatas"
                        val qty = group1.toFloatOrNull() ?: 1f
                        VoiceResult(
                            text = text,
                            quantity = qty,
                            unit = group2
                        )
                    }
                }
                else -> {
                    // Solo nombre
                    VoiceResult(
                        text = text,
                        quantity = 1f,
                        unit = ""
                    )
                }
            }
        }
    }
    
    // Fallback
    return VoiceResult(
        text = text,
        quantity = 1f,
        unit = ""
    )
}
