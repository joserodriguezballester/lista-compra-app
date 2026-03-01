package com.jose.listacompra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle

/**
 * Datos de producto del historial
 */
data class HistoricalProduct(
    val name: String,
    val price: Float,
    val aisle: String,
    val emoji: String,
    val lastBought: String, // "Hoy", "Hace 2 días", "Semana pasada"
    val frequency: String, // "Cada semana", "Cada mes"
    val hasOffer: Boolean = false,
    val offerDescription: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductHistoryScreen(
    onProductSelected: (HistoricalProduct) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    
    // Mock de productos del historial (luego vendrán de BD)
    val historicalProducts = remember {
        listOf(
            HistoricalProduct(
                "Leche semidesnatada",
                0.88f,
                "Lácteos",
                "🥛",
                "Hoy",
                "Cada semana",
                false
            ),
            HistoricalProduct(
                "Galletas Gullon",
                1.37f,
                "Galletas",
                "🍪",
                "Hace 3 días",
                "Cada semana",
                true,
                "Oferta habitual: 3x2"
            ),
            HistoricalProduct(
                "Pizza 4 quesos",
                2.39f,
                "Congelados",
                "🍕",
                "Hace 5 días",
                "Cada semana",
                true,
                "Oferta: 13x2"
            ),
            HistoricalProduct(
                "Gaseosa Carrefour",
                0.33f,
                "Bebidas",
                "🥤",
                "Hace 2 días",
                "Cada semana",
                true,
                "Oferta: 2x1"
            ),
            HistoricalProduct(
                "Queso curado",
                3.00f,
                "Quesos",
                "🧀",
                "Hace 1 semana",
                "Cada mes",
                false
            ),
            HistoricalProduct(
                "Tomate frito",
                1.99f,
                "Despensa",
                "🥫",
                "Hace 2 semanas",
                "Cada 2 semanas",
                false
            ),
            HistoricalProduct(
                "Brócoli",
                1.70f,
                "Fruta/Verdura",
                "🥦",
                "Hace 3 días",
                "Cada semana",
                false
            ),
            HistoricalProduct(
                "Chocobomb Blanco",
                1.14f,
                "Galletas",
                "🍫",
                "Hace 2 días",
                "Cada mes",
                false
            ),
            HistoricalProduct(
                "Cookies Bites",
                2.30f,
                "Galletas",
                "🍪",
                "Hace 2 días",
                "Cada mes",
                false
            ),
            HistoricalProduct(
                "Cubes Wafer",
                2.85f,
                "Galletas",
                "🧇",
                "Hace 2 días",
                "Cada mes",
                false
            ),
            HistoricalProduct(
                "Capuccino CRF",
                0.79f,
                "Bebidas",
                "☕",
                "Hace 5 días",
                "Cada semana",
                false
            ),
            HistoricalProduct(
                "Zumo naranja",
                1.79f,
                "Bebidas",
                "🍊",
                "Hace 3 días",
                "Cada semana",
                false
            ),
            HistoricalProduct(
                "Rallado 4 quesos",
                1.79f,
                "Quesos",
                "🧀",
                "Hace 1 semana",
                "Cada 2 semanas",
                false
            ),
            HistoricalProduct(
                "Croqueta La Abuela",
                2.55f,
                "Congelados",
                "🥟",
                "Hace 2 días",
                "Cada mes",
                false
            )
        )
    }
    
    // Categorías disponibles
    val categories = listOf(
        "Todos", "⭐ Favoritos", "🥛 Lácteos", "🍪 Galletas", 
        "🥤 Bebidas", "🧊 Congelados", "🥫 Despensa", 
        "🧀 Quesos", "🥦 Frescos", "🥩 Carnicería"
    )
    
    // Filtrar productos
    val filteredProducts = historicalProducts.filter { product ->
        val matchesSearch = searchQuery.isBlank() || 
            product.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "Todos" -> true
            "⭐ Favoritos" -> product.frequency.contains("semana")
            "🥛 Lácteos" -> product.aisle == "Lácteos"
            "🍪 Galletas" -> product.aisle == "Galletas"
            "🥤 Bebidas" -> product.aisle == "Bebidas"
            "🧊 Congelados" -> product.aisle == "Congelados"
            "🥫 Despensa" -> product.aisle == "Despensa"
            "🧀 Quesos" -> product.aisle == "Quesos"
            "🥦 Frescos" -> product.aisle == "Fruta/Verdura"
            "🥩 Carnicería" -> product.aisle == "Carnicería"
            else -> true
        }
        matchesSearch && matchesCategory
    }.sortedWith(
        compareByDescending<HistoricalProduct> { 
            it.lastBought.contains("Hoy") || it.lastBought.contains("1") 
        }.thenBy { it.name }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Buscar en Historial") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            // Filtros de categoría (scroll horizontal)
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category, maxLines = 1) }
                    )
                }
            }
            
            // Lista de productos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sección: Más usados (si está en "Todos" o "Favoritos")
                if ((selectedCategory == "Todos" || selectedCategory == "⭐ Favoritos") && searchQuery.isBlank()) {
                    item {
                        Text(
                            text = "⭐ MÁS USADOS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(
                        filteredProducts
                            .filter { it.frequency.contains("semana") }
                            .take(5)
                    ) { product ->
                        HistoricalProductCard(
                            product = product,
                            onClick = { onProductSelected(product) }
                        )
                    }
                    
                    item {
                        Text(
                            text = "📅 TODOS LOS PRODUCTOS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                }
                
                // Resto de productos filtrados
                items(filteredProducts) { product ->
                    HistoricalProductCard(
                        product = product,
                        onClick = { onProductSelected(product) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoricalProductCard(
    product: HistoricalProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (product.hasOffer) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.emoji,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${product.aisle} • ${product.lastBought}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                if (product.hasOffer) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🏷️ ${product.offerDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.2f", product.price)}€",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = product.frequency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
