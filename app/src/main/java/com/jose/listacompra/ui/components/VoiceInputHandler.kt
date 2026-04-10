package com.jose.listacompra.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jose.listacompra.ui.utils.BeepHelper
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
import kotlinx.coroutines.launch

/**
 * Inicia el reconocimiento de voz directamente (sin diálogo)
 * y añade el producto al finalizar.
 * 
 * Muestra feedback vía Toast/Snackbar.
 */
fun startDirectVoiceRecognition(
    context: Context,
    viewModel: ProductListViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onStatusUpdate: ((String) -> Unit)? = null,
    onRequestPermission: (() -> Unit)? = null
) {
    // Verificar permiso
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    
    if (!hasPermission) {
        // Solicitar permiso
        if (context is ComponentActivity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_MICROPHONE
            )
            onStatusUpdate?.invoke("🎤 Se necesita permiso de micrófono")
            Toast.makeText(context, "Concede permiso de micrófono", Toast.LENGTH_SHORT).show()
        }
        onRequestPermission?.invoke()
        return
    }
    
    // Mostrar que está escuchando
    onStatusUpdate?.invoke("🎤 Escuchando...")
    Toast.makeText(context, "🎤 Escuchando...", Toast.LENGTH_SHORT).show()
    
    // Crear reconocedor
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    
    val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {
            Toast.makeText(context, "Habla ahora...", Toast.LENGTH_SHORT).show()
        }
        
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No te he entendido. Intenta de nuevo."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No he oído nada. Intenta de nuevo."
                else -> "Error de reconocimiento"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onStatusUpdate?.invoke("❌ $message")
            recognizer.destroy()
        }
        
        override fun onResults(results: android.os.Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                processVoiceResult(matches[0], viewModel, context, scope, onStatusUpdate)
            }
            recognizer.destroy()
        }
        
        override fun onPartialResults(partialResults: android.os.Bundle?) {}
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }
    
    recognizer.setRecognitionListener(listener)
    
    val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    
    recognizer.startListening(intent)
}

/**
 * Procesa el resultado de voz
 */
private fun processVoiceResult(
    text: String,
    viewModel: ProductListViewModel,
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onStatusChange: ((String) -> Unit)?
) {
    scope.launch {
        onStatusChange?.invoke("📝 Procesando: \"$text\"")
        Toast.makeText(context, "Procesando: $text", Toast.LENGTH_SHORT).show()
        
        // Parsear el comando
        val parsed = parseVoiceCommand(text)
        
        // Buscar coincidencias
        val matches = viewModel.searchVoiceProducts(text)
        
        when (matches.size) {
            0 -> {
                // Sin coincidencias - añadir genérico
                BeepHelper.error(context)
                Toast.makeText(context, "➕ Añadido: ${parsed.productName}", Toast.LENGTH_SHORT).show()
                onStatusChange?.invoke("➕ Añadido: ${parsed.productName}")
                
                viewModel.addGenericProductFromVoice(
                    parsed.productName, 
                    parsed.quantity, 
                    parsed.supermarketName
                )
                
                kotlinx.coroutines.delay(1000)
                BeepHelper.release()
            }
            1 -> {
                // Una coincidencia - añadir directo
                BeepHelper.success(context)
                Toast.makeText(context, "✅ Añadido: ${matches[0].name}", Toast.LENGTH_SHORT).show()
                onStatusChange?.invoke("✅ Añadido: ${matches[0].name}")
                
                viewModel.addProductFromVoice(matches[0], parsed.quantity, parsed.supermarketName)
                
                kotlinx.coroutines.delay(800)
                BeepHelper.release()
            }
            else -> {
                // Múltiples coincidencias - añadir el primero con aviso
                BeepHelper.question(context)
                Toast.makeText(context, "¿Querías: ${matches[0].name}? (añadido)", Toast.LENGTH_SHORT).show()
                onStatusChange?.invoke("¿Querías: ${matches[0].name}?")
                
                viewModel.addProductFromVoice(matches[0], parsed.quantity, parsed.supermarketName)
                
                kotlinx.coroutines.delay(1200)
                BeepHelper.release()
            }
        }
    }
}

private const val REQUEST_CODE_MICROPHONE = 1001

