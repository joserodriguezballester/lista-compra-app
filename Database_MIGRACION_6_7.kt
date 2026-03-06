package com.jose.listacompra.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migracion de base de datos version 6 a 7
 * Añade campos de foto a la tabla products
 * 
 * USO: En tu Database.kt, en companion object de ShoppingListDatabase:
 * 
 * val MIGRATION_6_7 = Migration_6_7()
 * .addMigrations(MIGRATION_6_7)
 */
object Database_Migracion_6_7 {
    
    fun getMigration(): Migration {
        return object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Añadir columna photoUri
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN photoUri TEXT DEFAULT NULL"
                )
                
                // 2. Añadir columna photoTimestamp
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN photoTimestamp INTEGER DEFAULT NULL"
                )
                
                // 3. Añadir columna isPhotoUserSelected (0 = false, 1 = true)
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN isPhotoUserSelected INTEGER DEFAULT 0"
                )
            }
        }
    }
}

/**
 * ANOTACION para Database.kt:
 * 
 * Cambiar:
 *   version = 6
 * Por:
 *   version = 7
 * 
 * Y en @Database entities, asegurarte que ProductEntity esta incluido
 */
