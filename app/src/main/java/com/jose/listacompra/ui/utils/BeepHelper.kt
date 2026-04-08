package com.jose.listacompra.ui.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Utilidad para generar beeps de feedback
 */
object BeepHelper {
    private const val TAG = "BeepHelper"
    
    private var toneGenerator: ToneGenerator? = null
    
    /**
     * Beep de éxito - tono corto y agudo
     */
    fun success(context: Context) {
        playTone(context, ToneGenerator.TONE_PROP_BEEP, 150)
    }
    
    /**
     * Beep de duda/pregunta - tono medio, dos veces
     */
    fun question(context: Context) {
        playTone(context, ToneGenerator.TONE_PROP_BEEP2, 200)
    }
    
    /**
     * Beep de error - tono grave, dos pitidos
     */
    fun error(context: Context) {
        playTone(context, ToneGenerator.TONE_CDMA_CALLDROP_LITE, 300)
    }
    
    private fun playTone(context: Context, toneType: Int, durationMs: Int) {
        try {
            // Liberar el anterior si existe
            toneGenerator?.release()
            
            // Crear nuevo generador
            toneGenerator = ToneGenerator(
                AudioManager.STREAM_NOTIFICATION,
                100 // Volumen máximo
            )
            
            toneGenerator?.startTone(toneType, durationMs)
            
            Log.d(TAG, "Beep reproducido: tipo=$toneType, duración=$durationMs ms")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir beep", e)
        }
    }
    
    /**
     * Liberar recursos
     */
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error al liberar ToneGenerator", e)
        }
    }
}
