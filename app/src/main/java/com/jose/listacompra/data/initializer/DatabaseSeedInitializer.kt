package com.jose.listacompra.data.initializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
//import androidx.startup.AppInitializer
//import androidx.startup.Initializer
import com.jose.listacompra.data.local.dataseeder.InitialDataSeeder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Initializer que ejecuta el seed de datos al inicio de la app
 * Se ejecuta automáticamente antes de que se muestre la primera pantalla
 */
class DatabaseSeedInitializer : Initializer<Unit> {
    
    companion object {
        private const val TAG = "DatabaseSeedInitializer"
    }

    override fun create(context: Context) {
        Log.d(TAG, "DatabaseSeedInitializer.create() called")
        
        val appContext = context.applicationContext
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            DatabaseSeedInitializerEntryPoint::class.java
        )
        
        val initialDataSeeder = hiltEntryPoint.initialDataSeeder()
        
        // Ejecutar en background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting database seed...")
                initialDataSeeder.seedAll()
                Log.d(TAG, "Database seed completed successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding database", e)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
    
    /**
     * EntryPoint para obtener el InitialDataSeeder desde Hilt
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DatabaseSeedInitializerEntryPoint {
        fun initialDataSeeder(): InitialDataSeeder
    }
}
