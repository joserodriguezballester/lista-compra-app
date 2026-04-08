package com.jose.listacompra.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.ui.utils.BeepHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Datos del reconocimiento de voz
 */
data class VoiceResult(
    val text: String,
    val quantity: Float,
    val unit: String
)

/**
 * Diálogo completo de entrada por voz con feedback sonoro
 * 
 * Flujo:
 * 1. Usuario pulsa → empieza a escuchar
 * 2. Recibe texto → busca en artículos
 * 3. 1 coincidencia → beep éxito + añade directo
 * 4. >1 coincidencias → beep duda + muestra selección + reabre mic
 * 5. 0 coincidencias → beep error + añade genérico
 */
@Composable
fun VoiceInputDialog(
    viewModel: com.jose.listacompra.ui.viewmodel.ProductListViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isListening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Habla ahora...") }
    var voiceMatches by remember { mutableStateOf<List<Articulo>>(emptyList()) }
    var lastQuantity by remember { mutableStateOf(1f) }
    var lastParsed by remember { mutableStateOf<VoiceResult?>(null) } // T4
    var showSelection by remember { mutableStateOf(false) }
    
    // Animación de escucha
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing)
        ),
        label = "scale"
    )
    
    // Verificar permiso
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            status = "Se necesita permiso de micrófono"
            isListening = false
        }
    }
    
    // Comprobar permiso al abrir
    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == 
            PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            // Iniciar escucha automáticamente
            isListening = true
        }
    }
    
    // Diálogo principal
    AlertDialog(
        onDismissRequest = {
            if (!showSelection) {
                BeepHelper.release()
                onDismiss()
            }
        },
        title = { 
            Text(
                "Entrada por voz",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Indicador visual de escucha
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = if (isListening)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Micrófono",
                        modifier = Modifier
                            .size(48.dp)
                            .scale(if (isListening) scale else 1f),
                        tint = if (isListening)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Estado
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                
                // Instrucciones
                Text(
                    text = "Di algo como:\n\"3 de leche\"\n\"2 kilos de patatas\"\n\"medio kilo de jamón\"",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!showSelection) {
                    // Iniciar escucha de nuevo
                    isListening = true
                    status = "Habla ahora..."
                    
                    startSpeechRecognition(
                        context = context,
                        onResult = { text ->
                            scope.launch {
                                isListening = false
                                status = "Procesando: \"$text\""
                                
                                // Parsear el comando
                                val parsed = parseVoiceCommand(text)
                                lastQuantity = parsed.quantity
                                lastParsed = parsed // T4
                                
                                // Buscar coincidencias
                                val matches = viewModel.searchVoiceProducts(text)
                                voiceMatches = matches
                                
                                when (matches.size) {
                                    0 -> {
                                        // Sin coincidencias
                                        BeepHelper.error(context)
                                        status = "Sin coincidencias. Añadiendo..."
                                        
                                        // T4: Pasar supermercado parseado
                                        viewModel.addGenericProductFromVoice(parsed.productName, parsed.quantity, parsed.supermarketName)
                                        
                                        // Esperar un poco y cerrar
                                        kotlinx.coroutines.delay(1000)
                                        BeepHelper.release()
                                        onDismiss()
                                    }
                                    1 -> {
                                        // Una coincidencia - añadir directo
                                        BeepHelper.success(context)
                                        status = "Añadido: ${matches[0].name}"
                                        
                                        viewModel.addProductFromVoice(matches[0], parsed.quantity, parsed.supermarketName)
                                        
                                        // Esperar un poco y cerrar
                                        kotlinx.coroutines.delay(500)
                                        BeepHelper.release()
                                        onDismiss()
                                    }
                                    else -> {
                                        // Múltiples coincidencias
                                        BeepHelper.question(context)
                                        status = "Selecciona el producto correcto"
                                        showSelection = true
                                    }
                                }
                            }
                        },
                        onError = { error ->
                            isListening = false
                            status = "Error: $error"
                        }
                    )
                }
            }) {
                Text("Hablar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                BeepHelper.release()
                onDismiss()
            }) {
                Text("Cancelar")
            }
        }
    )
    
    // Diálogo de selección (múltiples coincidencias)
    AnimatedVisibility(visible = showSelection) {
        VoiceSelectionDialog(
            matches = voiceMatches,
            quantity = lastQuantity,
            onConfirm = { articulo ->
                BeepHelper.success(context)
                viewModel.addProductFromVoice(articulo, lastQuantity, lastParsed?.supermarketName)
                BeepHelper.release()
                onDismiss()
            },
            onDismiss = {
                showSelection = false
                // Reabrir micrófono para intentar otra vez
                isListening = true
                status = "Habla de nuevo..."
            }
        )
    }
}

/**
 * Inicia el reconocimiento de voz
 */
private fun startSpeechRecognition(
    context: android.content.Context,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    val speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
    
    val listener = object : android.speech.RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {
            // Listo para escuchar
        }
        
        override fun onBeginningOfSpeech() {
            // Comenzó a hablar
        }
        
        override fun onRmsChanged(rmsdB: Float) {}
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {}
        
        override fun onError(error: Int) {
            val message = when (error) {
                android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció nada"
                android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                else -> "Error de reconocimiento ($error)"
            }
            onError(message)
            speechRecognizer.destroy()
        }
        
        override fun onResults(results: android.os.Bundle?) {
            val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            } else {
                onError("Sin resultados")
            }
            speechRecognizer.destroy()
        }
        
        override fun onPartialResults(partialResults: android.os.Bundle?) {}
        
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }
    
    speechRecognizer.setRecognitionListener(listener)
    
    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
            android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    
    speechRecognizer.startListening(intent)
}
