package com.jose.listacompra.ui.screens.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    onChangeColor: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiState by viewModel.uiState.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                isDarkMode = isDarkMode,
                onToggleDarkMode = {
                    onToggleDarkMode()
                    scope.launch { drawerState.close() }
                },
                onNavigateToOffers = {
                    scope.launch { drawerState.close() }
                    onNavigateToOffers()
                },
                onNavigateToCategories = {
                    scope.launch { drawerState.close() }
                    onNavigateToCategories()
                },
                onNavigateToHistory = {
                    scope.launch { drawerState.close() }
                },
                onNavigateToSupermarkets = {
                    scope.launch { drawerState.close() }
                    onNavigateToSupermarkets()
                },
                onNavigateToCatalogo = {
                    scope.launch { drawerState.close() }
                    onNavigateToCatalogo()
                },
                onChangeColor = {
                    scope.launch { drawerState.close() }
                    onChangeColor()
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = "📊 Historial",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onChangeColor = onChangeColor,
                    onToggleDarkMode = onToggleDarkMode,
                    isDarkMode = isDarkMode
                )
            },
            bottomBar = {
                CommonBottomBar(
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToList = onNavigateToList,
                    currentRoute = "historial"
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
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
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        text = { Text("📉 Gráfica") }
                    )
                }

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
                    2 -> PriceChartTab(
                        products = uiState.frequencyData,
                        selectedProduct = uiState.selectedProduct,
                        priceHistory = uiState.priceHistory,
                        onProductSelect = { viewModel.selectProduct(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDrawerState(initialValue: DrawerValue): androidx.compose.material3.DrawerState {
    return androidx.compose.material3.rememberDrawerState(initialValue = initialValue)
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
        if (products.isNotEmpty()) {
            ProductSelector(
                products = products,
                selected = selectedProduct,
                onSelect = onProductSelect
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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

@Composable
private fun PriceChartTab(
    products: List<ProductFrequencyEntity>,
    selectedProduct: ProductFrequencyEntity?,
    priceHistory: List<ProductPriceHistoryEntity>,
    onProductSelect: (ProductFrequencyEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (products.isNotEmpty()) {
            ProductSelector(
                products = products,
                selected = selectedProduct,
                onSelect = onProductSelect
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedProduct == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Selecciona un producto para ver su gráfica",
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
                    Text("📉", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin datos para graficar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Gráfica de líneas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📈 ${selectedProduct.originalName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    PriceLineChart(
                        history = priceHistory.sortedBy { it.fecha },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas rápidas
            if (priceHistory.isNotEmpty()) {
                val prices = priceHistory.map { it.price }
                val min = prices.minOrNull() ?: 0f
                val max = prices.maxOrNull() ?: 0f
                val avg = prices.average().toFloat()
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Mínimo", value = "€${String.format("%.2f", min)}")
                        StatItem(label = "Máximo", value = "€${String.format("%.2f", max)}")
                        StatItem(label = "Media", value = "€${String.format("%.2f", avg)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLineChart(
    history: List<ProductPriceHistoryEntity>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return
    
    val prices = history.map { it.price }
    val minPrice = prices.minOrNull() ?: 0f
    val maxPrice = prices.maxOrNull() ?: 0f
    val priceRange = maxPrice - minPrice
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        
        // Dibujar ejes
        drawLine(
            color = surfaceVariant,
            start = Offset(padding, canvasHeight - padding),
            end = Offset(canvasWidth - padding, canvasHeight - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = surfaceVariant,
            start = Offset(padding, padding),
            end = Offset(padding, canvasHeight - padding),
            strokeWidth = 2f
        )
        
        if (history.size < 2) {
            // Solo un punto, dibujar un círculo
            val x = canvasWidth / 2
            val y = canvasHeight / 2
            drawCircle(
                color = primaryColor,
                radius = 8f,
                center = Offset(x, y)
            )
            return@Canvas
        }
        
        // Calcular puntos
        val points = history.mapIndexed { index, record ->
            val x = padding + (index.toFloat() / (history.size - 1)) * (canvasWidth - 2 * padding)
            val normalizedPrice = if (priceRange > 0) (record.price - minPrice) / priceRange else 0.5f
            val y = canvasHeight - padding - normalizedPrice * (canvasHeight - 2 * padding)
            Offset(x, y)
        }
        
        // Dibujar área bajo la línea
        val path = Path().apply {
            moveTo(points.first().x, canvasHeight - padding)
            points.forEach { point ->
                lineTo(point.x, point.y)
            }
            lineTo(points.last().x, canvasHeight - padding)
            close()
        }
        
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.1f)
        )
        
        // Dibujar línea
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        
        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.cornerPathEffect(10f)
            )
        )
        
        // Dibujar puntos
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = point
            )
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSelector(
    products: List<ProductFrequencyEntity>,
    selected: ProductFrequencyEntity?,
    onSelect: (ProductFrequencyEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
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
        item {
            Text(
                text = "📈 $productName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
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