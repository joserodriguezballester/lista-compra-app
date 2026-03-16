package com.jose.listacompra.data.repository

import com.jose.listacompra.data.local.converters.toDomain
import com.jose.listacompra.data.local.converters.toEntity
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.domain.model.ShoppingList
import com.jose.listacompra.domain.repository.IShoppingListRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) : IShoppingListRepository {

    override suspend fun getActiveLists(): List<ShoppingList> {
        return shoppingListDao.getActiveLists().map { it.toDomain() }
    }

    override suspend fun getAllLists(): List<ShoppingList> {
        return shoppingListDao.getAllLists().map { it.toDomain() }
    }

    override suspend fun getArchivedLists(): List<ShoppingList> {
        return shoppingListDao.getArchivedLists().map { it.toDomain() }
    }

    override suspend fun getListById(id: Long): ShoppingList? {
        return shoppingListDao.getListById(id)?.toDomain()
    }

    override suspend fun createList(name: String, useDefaultAisles: Boolean): Long {
        val entity = ShoppingListEntity(
            name = name,
            fechaCreacion = System.currentTimeMillis(),
            estado = "ACTIVA",
            // Aquí podrías usar useDefaultAisles si tu entidad lo requiere

        )
        return shoppingListDao.insertList(entity)
    }

    override suspend fun updateList(list: ShoppingList) {
        shoppingListDao.updateList(list.toEntity())
    }

    override suspend fun deleteList(list: ShoppingList) {
        if (list.isArchived()) {
            shoppingListDao.deleteList(list.toEntity())
        }
    }

    override suspend fun archiveList(listId: Long) {
        shoppingListDao.archiveList(listId)
    }

    override suspend fun unarchiveList(listId: Long) {
        shoppingListDao.unarchiveList(listId)
    }

  }