package com.jose.listacompra.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Historial") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
            // Tabs
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("📊 Frecuencia") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("📈 Precios") }
                )
            }
            
            // Contenido
            when (uiState.selectedTab) {
                0 -> FrequencyTab(
                    products = uiState.frequencyData,
                    onProductClick = { viewModel.selectProduct(it) }
                )
                1 -> PriceEvolutionTab(
                    products = uiState.frequencyData,
                    selectedProduct = uiState.selectedProduct,
                    priceHistory = uiState.priceHistory,
                    onProductSelect = { viewModel.selectProduct(it) }
                )
            }
        }
    }
}

@Composable
private fun FrequencyTab(
    products: List<ProductFrequencyEntity>,
    onProductClick: (ProductFrequencyEntity) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Sin datos de frecuencia",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Añade productos a tu lista para ver estadísticas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products.sortedByDescending { it.timesPurchased }) { product ->
                FrequencyCard(
                    product = product,
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}

@Composable
private fun FrequencyCard(
    product: ProductFrequencyEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val daysSinceLastPurchase = if (product.lastPurchaseDate > 0) {
        val diffDays = (System.currentTimeMillis() - product.lastPurchaseDate) / (1000 * 60 * 60 * 24)
        when {
            diffDays == 0L -> "Hoy"
            diffDays == 1L -> "Ayer"
            diffDays < 7 -> "Hace $diffDays días"
            diffDays < 30 -> "Hace ${diffDays / 7} sem"
            else -> "Hace ${diffDays / 30} mes(es)"
        }
    } else "Nunca"
    
    val nextPurchaseText = if (product.estimatedNextDate != null && product.estimatedNextDate > 0) {
        val diffDays = (product.estimatedNextDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
        when {
            diffDays < 0 -> "⚠️ Ya debería estar en la lista"
            diffDays == 0L -> "Hoy"
            diffDays == 1L -> "Mañana"
            diffDays < 7 -> "En $diffDays días"
            else -> "En ${diffDays / 7} semanas"
        }
    } else null
    
    val isOverdue = product.estimatedNextDate != null && 
                   product.estimatedNextDate > 0 && 
                   product.estimatedNextDate < System.currentTimeMillis()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.originalName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isOverdue)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${product.timesPurchased}x",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Frecuencia
                Column {
                    Text(
                        text = "Frecuencia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (product.averageDaysBetween != null && product.averageDaysBetween > 0)
                            "Cada ${product.averageDaysBetween.toInt()} días"
                        else
                            "Sin datos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Última compra
                Column {
                    Text(
                        text = "Última compra",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = daysSinceLastPurchase,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Próxima estimada
                if (nextPurchaseText != null) {
                    Column {
                        Text(
                            text = "Próxima",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = nextPurchaseText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceEvolutionTab(
    products: List<ProductFrequencyEntity>,
    selectedProduct: ProductFrequencyEntity?,
    priceHistory: List<ProductPriceHistoryEntity>,
    onProductSelect: (ProductFrequencyEntity) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Selector de producto
        if (products.isNotEmpty()) {
            ProductSelector(
                products = products,
                selected = selectedProduct,
                onSelect = onProductSelect
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gráfico o mensaje
        if (selectedProduct == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Selecciona un producto para ver su evolución",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (priceHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📈", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin historial de precios para ${selectedProduct.originalName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Añade este producto varias veces con precio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            PriceChart(
                productName = selectedProduct.originalName,
                history = priceHistory
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSelector(
    products: List<ProductFrequencyEntity>,
    selected: ProductFrequencyEntity?,
    onSelect: (ProductFrequencyEntity) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.originalName ?: "Seleccionar producto",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .menuAnchor(),
            label = { Text("Producto") }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.sortedBy { it.originalName }.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.originalName) },
                    onClick = {
                        onSelect(product)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PriceChart(
    productName: String,
    history: List<ProductPriceHistoryEntity>
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        item {
            Text(
                text = "📈 $productName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Estadísticas
        if (history.isNotEmpty()) {
            val prices = history.map { it.price }
            val min = prices.minOrNull() ?: 0f
            val max = prices.maxOrNull() ?: 0f
            val avg = prices.average().toFloat()
            val current = prices.last()
            val trend = if (prices.size >= 2) {
                val diff = current - prices.first()
                val percent = if (prices.first() > 0) (diff / prices.first()) * 100 else 0f
                when {
                    percent > 5 -> "📈 Subiendo +${String.format("%.0f", percent)}%"
                    percent < -5 -> "📉 Bajando ${String.format("%.0f", percent)}%"
                    else -> "➡️ Estable"
                }
            } else "Sin tendencia"
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Estadísticas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Mínimo", value = "€${String.format("%.2f", min)}")
                            StatItem(label = "Máximo", value = "€${String.format("%.2f", max)}")
                            StatItem(label = "Media", value = "€${String.format("%.2f", avg)}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Actual", value = "€${String.format("%.2f", current)}")
                            StatItem(label = "Tendencia", value = trend)
                            StatItem(label = "Compras", value = "${history.size}x")
                        }
                    }
                }
            }
        }
        
        // Lista de precios históricos
        item {
            Text(
                text = "Historial de compras",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(history.sortedByDescending { it.fecha }) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dateFormat.format(Date(record.fecha)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${record.quantity} unidades",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "€${String.format("%.2f", record.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}