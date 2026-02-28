package com.jose.listacompra.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "list_preferences")

class ListPreferences(private val context: Context) {
    
    companion object {
        val SELECTED_LIST_ID = longPreferencesKey("selected_list_id")
    }
    
    val selectedListId: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_LIST_ID] ?: -1L // -1 indica que no hay lista seleccionada
        }
    
    suspend fun setSelectedListId(listId: Long) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LIST_ID] = listId
        }
    }
    
    suspend fun clearSelectedList() {
        context.dataStore.edit { preferences ->
            preferences.remove(SELECTED_LIST_ID)
        }
    }
}
