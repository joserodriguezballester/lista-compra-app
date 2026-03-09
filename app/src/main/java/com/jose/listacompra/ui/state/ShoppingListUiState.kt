package com.jose.listacompra.ui.state

import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.ProductSuggestion
import com.jose.listacompra.domain.model.ShoppingList

data class ShoppingListUiState(
    val currentList: ShoppingList? = null,
    val aisles: List<Aisle> = emptyList(),
    val products: List<Product> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val totalEstimate: Float = 0f,           // Total con ofertas aplicadas
    val totalWithoutOffers: Float = 0f,      // Total sin ofertas
    val savings: Float = 0f,                 // Ahorro total
    val purchasedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val productSuggestions: List<ProductSuggestion> = emptyList(),
    val showEmptyListConfirmDialog: Boolean=false
)