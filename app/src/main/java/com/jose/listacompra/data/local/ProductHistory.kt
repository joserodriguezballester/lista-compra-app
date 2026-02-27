package com.jose.listacompra.data.local

import androidx.room.*

/**
 * Productos frecuentes/historial para autocompletado
 */
@Entity(tableName = "product_history")
data class ProductHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // Nombre normalizado (lowercase para búsquedas)
    val originalName: String,   // Nombre como lo escribió el usuario
    val aisleId: Long,
    val lastQuantity: Float,
    val lastPrice: Float?,
    val usageCount: Int = 1,    // Cuántas veces se ha usado (para ordenar por frecuencia)
    val lastUsed: Long = System.currentTimeMillis()
)

@Dao
interface ProductHistoryDao {
    
    @Query("SELECT * FROM product_history WHERE name LIKE :query || '%' ORDER BY usageCount DESC, lastUsed DESC LIMIT 5")
    suspend fun findSuggestions(query: String): List<ProductHistoryEntity>
    
    @Query("SELECT * FROM product_history WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): ProductHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: ProductHistoryEntity): Long
    
    @Query("UPDATE product_history SET usageCount = usageCount + 1, lastUsed = :timestamp, lastQuantity = :quantity, lastPrice = :price WHERE name = :name")
    suspend fun updateUsage(name: String, quantity: Float, price: Float?, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM product_history ORDER BY usageCount DESC LIMIT 20")
    suspend fun getMostFrequent(): List<ProductHistoryEntity>
}
