package com.jose.listacompra.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Datos extraídos del comando de voz
 */
data class VoiceCommand(
    val productName: String,
    val quantity: Float,
    val unit: String
)

/**
 * Componente de entrada por voz para añadir productos
 */
@Composable
fun VoiceInputButton(
    onVoiceCommand: (VoiceCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isListening by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Launcher para solicitar permiso de micrófono
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            errorMessage = "Se necesita permiso de micrófono para usar la voz"
        }
    }
    
    // Solicitar permiso al inicio
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    // Mostrar error si hay
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
    
    Box(modifier = modifier) {
        IconButton(
            onClick = {
                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return@IconButton
                }
                
                if (isListening) {
                    isListening = false
                } else {
                    startVoiceRecognition(
                        context = context,
                        onResult = { text ->
                            isListening = false
                            parseVoiceCommand(text)?.let { command ->
                                onVoiceCommand(command)
                            } ?: run {
                                errorMessage = "No pude entender: '$text'. Intenta decir algo como '3 litros de leche'"
                            }
                        },
                        onError = { error ->
                            isListening = false
                            errorMessage = error
                        }
                    )
                    isListening = true
                }
            }
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isListening) "Escuchando..." else "Añadir por voz",
                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        
        // Indicador de escucha
        AnimatedVisibility(
            visible = isListening,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .scale(1.2f)
            )
        }
    }
}

/**
 * Inicia el reconocimiento de voz usando el intent del sistema
 */
private fun startVoiceRecognition(
    context: Context,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Di algo como: 3 litros de leche")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    
    try {
        // Usar el launcher de actividad para el resultado
        // Nota: En Compose puro necesitaríamos un ActivityResultLauncher
        // Por simplicidad, usaremos SpeechRecognizer directamente
        startSpeechRecognizer(context, onResult, onError)
    } catch (e: Exception) {
        onError("Error al iniciar reconocimiento: ${e.message}")
    }
}

/**
 * Usa SpeechRecognizer directamente
 */
private fun startSpeechRecognizer(
    context: Context,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        onError("El reconocimiento de voz no está disponible en este dispositivo")
        return
    }
    
    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    
    val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció nada. Intenta de nuevo."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz. Intenta de nuevo."
                else -> "Error desconocido ($error)"
            }
            onError(message)
            speechRecognizer.destroy()
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            } else {
                onError("No se reconoció nada")
            }
            speechRecognizer.destroy()
        }
        
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    
    speechRecognizer.setRecognitionListener(listener)
    
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    
    speechRecognizer.startListening(intent)
}

/**
 * Parsea el texto reconocido para extraer cantidad, unidad y nombre del producto
 * Soporta formatos como:
 * - "3 litros de leche"
 * - "tres kilos de patatas"
 * - "500 gramos de jamón"
 * - "un paquete de galletas"
 * - "dos botellas de agua"
 */
fun parseVoiceCommand(text: String): VoiceCommand? {
    val lowerText = text.lowercase().trim()
    
    // Palabras numéricas a números
    val numberWords = mapOf(
        "un" to 1f, "una" to 1f, "uno" to 1f,
        "dos" to 2f,
        "tres" to 3f,
        "cuatro" to 4f,
        "cinco" to 5f,
        "seis" to 6f,
        "siete" to 7f,
        "ocho" to 8f,
        "nueve" to 9f,
        "diez" to 10f,
        "media" to 0.5f,
        "medio" to 0.5f,
        "cuarto" to 0.25f
    )
    
    // Unidades soportadas
    val units = listOf(
        "litros", "litro", "l",
        "kilos", "kilo", "kg",
        "gramos", "gramo", "gr", "g",
        "paquetes", "paquete",
        "botellas", "botella",
        "latas", "lata",
        "botes", "bote",
        "cajas", "caja",
        "unidades", "unidad", "uds", "ud",
        "piezas", "pieza"
    )
    
    // Palabras conectoras a eliminar
    val connectors = listOf("de", "del", "un", "una", "unos", "unas")
    
    // Intentar encontrar patrón: [NÚMERO] [UNIDAD] [CONECTOR] [PRODUCTO]
    
    // Primero, buscar número (ya sea dígito o palabra)
    var quantity = 1f
    var remainingText = lowerText
    
    // Buscar número al inicio (dígitos)
    val numberRegex = Regex("^(\\d+(?:[.,]\\d+)?)\\s*")
    val numberMatch = numberRegex.find(lowerText)
    
    if (numberMatch != null) {
        val numStr = numberMatch.groupValues[1].replace(',', '.')
        quantity = numStr.toFloatOrNull() ?: 1f
        remainingText = lowerText.substring(numberMatch.range.last + 1).trim()
    } else {
        // Buscar palabra numérica
        for ((word, num) in numberWords) {
            if (remainingText.startsWith("$word ")) {
                quantity = num
                remainingText = remainingText.substring(word.length).trim()
                break
            }
        }
    }
    
    // Buscar unidad
    var unit = "unidad"
    for (u in units) {
        val unitPattern = Regex("^$u\\b")
        if (unitPattern.containsMatchIn(remainingText)) {
            unit = when (u) {
                "litros", "litro", "l" -> "litros"
                "kilos", "kilo", "kg" -> "kilos"
                "gramos", "gramo", "gr", "g" -> "gramos"
                "paquetes", "paquete" -> "paquetes"
                "botellas", "botella" -> "botellas"
                "latas", "lata" -> "latas"
                "botes", "bote" -> "botes"
                "cajas", "caja" -> "cajas"
                "unidades", "unidad", "uds", "ud" -> "unidades"
                "piezas", "pieza" -> "piezas"
                else -> u
            }
            remainingText = remainingText.replaceFirst(unitPattern, "").trim()
            break
        }
    }
    
    // Eliminar conectores al inicio
    for (connector in connectors) {
        if (remainingText.startsWith("$connector ")) {
            remainingText = remainingText.substring(connector.length).trim()
            break
        }
    }
    
    // Limpiar texto final
    val productName = remainingText
        .replace(Regex("^[a-z]+\\s+"), "") // Eliminar artículos residuales
        .trim()
        .capitalizeWords()
    
    return if (productName.isNotBlank()) {
        VoiceCommand(productName, quantity, unit)
    } else {
        null
    }
}

/**
 * Capitaliza cada palabra del string
 */
private fun String.capitalizeWords(): String {
    return this.split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}
