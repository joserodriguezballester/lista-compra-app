package com.jose.listacompra.data.local.dataseeder

/**
 * Configuración de datos a cargar
 * 
 * true = Modo desarrollo (carga todo: artículos ejemplo, productos en lista)
 * false = Modo producción (carga mínimo: supermercados, pasillos Carrefour, ofertas, categorías)
 * 
 * Cambiar a false antes de generar APK de producción
 */
object DataConfig {
    const val LOAD_FULL_DATA = true
}
