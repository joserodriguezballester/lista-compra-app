package com.jose.listacompra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jose.listacompra.data.local.ShoppingListEntity

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists WHERE estado = 'ACTIVA' ORDER BY fechaCreacion DESC")
    suspend fun getActiveLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists WHERE estado = 'ARCHIVADA' ORDER BY fechaCreacion DESC")
    suspend fun getArchivedLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists ORDER BY fechaCreacion DESC")
    suspend fun getAllLists(): List<ShoppingListEntity>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: Long): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertList(list: ShoppingListEntity): Long

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("UPDATE shopping_lists SET estado = 'ARCHIVADA' WHERE id = :listId")
    suspend fun archiveList(listId: Long)

    @Query("UPDATE shopping_lists SET estado = 'ACTIVA' WHERE id = :listId")
    suspend fun unarchiveList(listId: Long)
}