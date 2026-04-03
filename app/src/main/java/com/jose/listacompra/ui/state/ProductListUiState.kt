package com.jose.listacompra.ui.state

import com.jose.listacompra.data.local.dao.PriceStats
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.domain.model.Aisle
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Supermarket

data class ProductListUiState(
    val productsByAisle: Map<Aisle, List<Product>> = emptyMap(),
    val totalPrice: Float = 0f,
    val totalItems: Int = 0,
    val purchasedItems: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val supermarkets: List<Supermarket> = emptyList(),
    val selectedSupermarketId: Long? = null,
    val aisles: List<Aisle> = emptyList(),
    val categories: List<Category> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val articleSuggestions: List<Articulo> = emptyList(),
    val collapsedAisles: Set<Long> = emptySet(),
    val historySuggestions: List<ProductFrequencyEntity> = emptyList(),
    val selectedPriceHistory: List<ProductPriceHistoryEntity> = emptyList(),
    val selectedPriceStats: PriceStats? = null
)