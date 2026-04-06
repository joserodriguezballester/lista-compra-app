package com.jose.listacompra.ui.screens.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class ProductHistory(
    val product: ProductFrequencyEntity,
    val history: List<ProductPriceHistoryEntity>,
    val color: Color
)

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

    // Colores para las líneas de cada producto
    val lineColors = listOf(
        Color(0xFF2196F3), // Azul
        Color(0xFF4CAF50), // Verde
        Color(0xFFFF9800), // Naranja
        Color(0xFF9C27B0), // Púrpura
        Color(0xFFF44336), // Rojo
        Color(0xFF00BCD4)  // Cyan
    )

    // Estado para productos seleccionados en la gráfica
    var selectedProducts by remember { mutableStateOf<List<ProductHistory>>(emptyList()) }
    var showProductPicker by remember { mutableStateOf(false) }

    // Actualizar historial cuando cambian los productos seleccionados
    LaunchedEffect(selectedProducts) {
        if (selectedProducts.isNotEmpty()) {
            selectedProducts.forEach { ph ->
                viewModel.selectProduct(ph.product)
            }
        }
    }

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
                    2 -> MultiProductChartTab(
                        products = uiState.frequencyData,
                        selectedProducts = selectedProducts,
                        onAddProduct = { product ->
                            if (selectedProducts.none { it.product.productName == product.productName }) {
                                val newHistory = uiState.frequencyData
                                    .filter { it.productName == product.productName }
                                    .map { 
                                        // Buscar historial de precios para este producto
                                        uiState.priceHistory.filter { it.productName == product.productName }
                                    }
                                    .firstOrNull() ?: emptyList()
                                
                                selectedProducts = selectedProducts + ProductHistory(
                                    product = product,
                                    history = newHistory,
                                    color = lineColors[selectedProducts.size % lineColors.size]
                                )
                            }
                        },
                        onRemoveProduct = { product ->
                            selectedProducts = selectedProducts.filter { it.product.productName != product.productName }
                        },
                        priceHistoryMap = emptyMap() // Se actualizará dinámicamente
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
        EmptyStateTab(
            emoji = "📊",
            title = "Sin datos de frecuencia",
            subtitle = "Añade productos a tu lista para ver estadísticas"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products.sortedByDescending { it.timesPurchased }) { product ->
                FrequencyCard(product = product, onClick = { onProductClick(product) })
            }
        }
    }
}

@Composable
private fun FrequencyCard(
    product: ProductFrequencyEntity,
    onClick: () -> Unit
) {
    val daysSinceLast = if (product.lastPurchaseDate > 0) {
        val diffDays = (System.currentTimeMillis() - product.lastPurchaseDate) / (1000 * 60 * 60 * 24)
        when {
            diffDays == 0L -> "Hoy"
            diffDays == 1L -> "Ayer"
            diffDays < 7 -> "Hace $diffDays días"
            diffDays < 30 -> "Hace ${diffDays / 7} sem"
            else -> "Hace ${diffDays / 30} mes(es)"
        }
    } else "Nunca"
    
    val nextPurchase = if (product.estimatedNextDate != null && product.estimatedNextDate > 0) {
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
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
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
                    Text("Frecuencia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (product.averageDaysBetween != null && product.averageDaysBetween > 0)
                            "Cada ${product.averageDaysBetween.toInt()} días"
                        else "Sin datos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text("Última", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(daysSinceLast, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                
                if (nextPurchase != null) {
                    Column {
                        Text("Próxima", style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(nextPurchase, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
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
    Column(modifier = Modifier.fillMaxSize()) {
        if (products.isNotEmpty()) {
            ProductSelector(products, selectedProduct, onProductSelect)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            selectedProduct == null -> EmptyStateTab(
                emoji = "📈",
                title = "Selecciona un producto",
                subtitle = "Para ver su evolución de precios"
            )
            priceHistory.isEmpty() -> EmptyStateTab(
                emoji = "📈",
                title = "Sin historial",
                subtitle = "para ${selectedProduct.originalName}"
            )
            else -> PriceChartContent(selectedProduct.originalName, priceHistory)
        }
    }
}

@Composable
private fun MultiProductChartTab(
    products: List<ProductFrequencyEntity>,
    selectedProducts: List<ProductHistory>,
    onAddProduct: (ProductFrequencyEntity) -> Unit,
    onRemoveProduct: (ProductFrequencyEntity) -> Unit,
    priceHistoryMap: Map<String, List<ProductPriceHistoryEntity>>
) {
    val colors = listOf(
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFF00BCD4)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "📉 Comparativa de precios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Productos seleccionados (chips)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedProducts) { ph ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveProduct(ph.product) },
                    label = { Text(ph.product.originalName) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Quitar",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = ph.color.copy(alpha = 0.2f),
                        labelColor = ph.color
                    )
                )
            }
            
            // Botón añadir
            if (selectedProducts.size < 6) {
                item {
                    AssistChip(
                        onClick = { /* Abrir selector */ },
                        label = { Text("+ Añadir") },
                        leadingIcon = {
                            Icon(Icons.Default.Close, "+", modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selector de productos
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Selecciona productos para comparar:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products.take(10)) { product ->
                        val isSelected = selectedProducts.any { it.product.productName == product.productName }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    onRemoveProduct(product)
                                } else if (selectedProducts.size < 6) {
                                    onAddProduct(product)
                                }
                            },
                            label = { Text(product.originalName) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gráfica
        if (selectedProducts.isEmpty()) {
            EmptyStateTab(
                emoji = "📊",
                title = "Selecciona productos",
                subtitle = "Para comparar sus precios en la gráfica"
            )
        } else {
            MultiLinePriceChart(
                productsData = selectedProducts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Leyenda con estadísticas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Resumen",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    selectedProducts.forEach { ph ->
                        val prices = ph.history.map { it.price }
                        if (prices.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(ph.color, MaterialTheme.shapes.small)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(ph.product.originalName, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    "€${String.format("%.2f", prices.minOrNull()!!)} - €${String.format("%.2f", prices.maxOrNull()!!)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiLinePriceChart(
    productsData: List<ProductHistory>,
    modifier: Modifier = Modifier
) {
    if (productsData.isEmpty()) return
    
    // Combinar todos los datos para obtener rango de fechas y precios
    val allHistory = productsData.flatMap { it.history }
    if (allHistory.isEmpty()) return
    
    val minDate = allHistory.minOf { it.fecha }
    val maxDate = allHistory.maxOf { it.fecha }
    val maxPrice = allHistory.maxOf { it.price }
    
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 50f
        val chartWidth = canvasWidth - 2 * padding
        val chartHeight = canvasHeight - 2 * padding
        
        // Dibujar ejes
        drawLine(
            color = surfaceVariant,
            start = Offset(padding, padding),
            end = Offset(padding, canvasHeight - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = surfaceVariant,
            start = Offset(padding, canvasHeight - padding),
            end = Offset(canvasWidth - padding, canvasHeight - padding),
            strokeWidth = 2f
        )
        
        // Dibujar líneas de grid y etiquetas de precio (eje Y)
        val priceStep = maxPrice / 5
        for (i in 0..5) {
            val price = i * priceStep
            val y = canvasHeight - padding - (price / maxPrice) * chartHeight
            
            // Línea de grid
            drawLine(
                color = surfaceVariant.copy(alpha = 0.3f),
                start = Offset(padding, y),
                end = Offset(canvasWidth - padding, y),
                strokeWidth = 1f
            )
            
            // Etiqueta de precio
            drawContext.canvas.nativeCanvas.drawText(
                "€${String.format("%.1f", price)}",
                5f,
                y + 5f,
                android.graphics.Paint().apply {
                    color = onSurfaceVariant.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }
        
        // Dibujar etiquetas de fecha (eje X)
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val dateRange = maxDate - minDate
        for (i in 0..4) {
            val date = minDate + (dateRange * i / 4)
            val x = padding + (i.toFloat() / 4) * chartWidth
            
            drawContext.canvas.nativeCanvas.drawText(
                dateFormat.format(Date(date)),
                x,
                canvasHeight - 20f,
                android.graphics.Paint().apply {
                    color = onSurfaceVariant.hashCode()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
        
        // Dibujar líneas para cada producto
        productsData.forEach { ph ->
            if (ph.history.isNotEmpty()) {
                val points = ph.history.sortedBy { it.fecha }.map { record ->
                    val x = if (dateRange > 0) {
                        padding + ((record.fecha - minDate).toFloat() / dateRange) * chartWidth
                    } else {
                        canvasWidth / 2
                    }
                    val y = canvasHeight - padding - (record.price / maxPrice) * chartHeight
                    Offset(x, y)
                }
                
                // Línea
                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                }
                
                drawPath(
                    path = path,
                    color = ph.color,
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.cornerPathEffect(10f)
                    )
                )
                
                // Puntos
                points.forEach { point ->
                    drawCircle(
                        color = ph.color,
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
    }
}

@Composable
private fun PriceChartContent(productName: String, history: List<ProductPriceHistoryEntity>) {
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
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Estadísticas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                            StatItem(label = "Compras", value = "${history.size}x")
                        }
                    }
                }
            }
        }
        
        item {
            Text("Historial de compras", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        
        items(history.sortedByDescending { it.fecha }) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(dateFormat.format(Date(record.fecha)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("${record.quantity} unidades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("€${String.format("%.2f", record.price)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateTab(emoji: String, title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).menuAnchor(),
            label = { Text("Producto") }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.sortedBy { it.originalName }.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.originalName) },
                    onClick = { onSelect(product); expanded = false }
                )
            }
        }
    }
}