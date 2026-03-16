package com.jose.listacompra.domain.repository

import com.jose.listacompra.domain.model.ShoppingList

interface IShoppingListRepository {
    suspend fun getActiveLists(): List<ShoppingList>
    suspend fun getArchivedLists(): List<ShoppingList>
    suspend fun getListById(id: Long): ShoppingList?
    suspend fun createList(name: String, useDefaultAisles: Boolean): Long
    suspend fun updateList(list: ShoppingList)
    suspend fun deleteList(list: ShoppingList)
    suspend fun archiveList(listId: Long)
    suspend fun unarchiveList(listId: Long)
    suspend fun getAllLists(): List<ShoppingList>



}