package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity

@Dao
interface PurchaseHistoryDao {
    @Query("SELECT * FROM purchase_history ORDER BY fecha DESC")
    suspend fun getAllPurchases(): List<PurchaseHistoryEntity>

    @Query("SELECT * FROM purchase_history WHERE fecha >= :since ORDER BY fecha DESC")
    suspend fun getPurchasesSince(since: Long): List<PurchaseHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseHistoryEntity): Long

    @Query("SELECT * FROM purchase_history ORDER BY fecha DESC LIMIT 1")
    suspend fun getLastPurchase(): PurchaseHistoryEntity?

    @Query("SELECT AVG(total) FROM purchase_history")
    suspend fun getAveragePurchaseAmount(): Float?

    @Query("SELECT SUM(total) FROM purchase_history WHERE fecha >= :since")
    suspend fun getTotalSpentSince(since: Long): Float?
}