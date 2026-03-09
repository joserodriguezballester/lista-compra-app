package com.jose.listacompra.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Función helper para realizar vibración de feedback táctil
 * @param context Contexto de Android
 * @param milliseconds Duración de la vibración (por defecto 60ms para feedback sutil)
 * @param isCompletion true para vibración de éxito (más larga y con patrón)
 */
fun Context.vibrateFeedback(context: Context, milliseconds: Long = 60L, isCompletion: Boolean = false) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Vibración suave para feedback táctil
        val effect = if (isCompletion) {
            // Patrón de éxito: dos vibraciones cortas
            VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 80), -1)
        } else {
            // Vibración simple y sutil
            VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        if (isCompletion) {
            vibrator.vibrate(longArrayOf(0, 50, 100, 80), -1)
        } else {
            vibrator.vibrate(milliseconds)
        }
    }
}