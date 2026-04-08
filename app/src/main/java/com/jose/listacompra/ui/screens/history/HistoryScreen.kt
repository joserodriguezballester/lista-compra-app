package com.jose.listacompra.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ProductChartData(
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                onNavigateToHome = {
                    scope.launch { drawerState.close() }
                    onNavigateToHome()
                },
                onNavigateToList = {
                    scope.launch { drawerState.close() }
                    onNavigateToList()
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
                    // Ya estamos en historial
                },
                onNavigateToSupermarkets = {
                    scope.launch { drawerState.close() }
                    onNavigateToSupermarkets()
                },
                onNavigateToCatalogo = {
                    scope.launch { drawerState.close() }
                    onNavigateToCatalogo()
                }
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
                    isDarkMode = isDarkMode,
                    onMicrophoneClick = { showVoiceDialog = true }
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
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text("📊 Frecuencia", maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = { Text("📈 Precios", maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        text = { Text("📉 Gráfica", maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        text = { Text("📊 Comparar", maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                    )
                }

                when (uiState.selectedTab) {
                    0 -> FrequencyTab(
                        products = uiState.frequencyData,
                        onProductClick = { viewModel.selectProduct(it) }
                    )
                    1 -> PriceListTab(
                        products = uiState.frequencyData,
                        selectedProduct = uiState.selectedProduct,
                        priceHistory = uiState.priceHistory,
                        onProductSelect = { viewModel.selectProduct(it) }
                    )
                    2 -> SingleChartTab(
                        products = uiState.frequencyData,
                        selectedProduct = uiState.selectedProduct,
                        priceHistory = uiState.priceHistory,
                        onProductSelect = { viewModel.selectProduct(it) }
                    )
                    3 -> MultiProductCompareTab(
                        products = uiState.frequencyData,
                        viewModel = viewModel
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

// ============ PESTAÑA 0: FRECUENCIA ============
@Composable
private fun FrequencyTab(
    products: List<ProductFrequencyEntity>,
    onProductClick: (ProductFrequencyEntity) -> Unit
) {
    if (products.isEmpty()) {
        EmptyState(emoji = "📊", title = "Sin datos", subtitle = "Añade productos para ver frecuencia")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products.sortedByDescending { it.timesPurchased }) { product ->
                FrequencyCard(product = product, onClick = { onProductClick(product) })
            }
        }
    }
}

@Composable
private fun FrequencyCard(product: ProductFrequencyEntity, onClick: () -> Unit) {
    val daysSince = if (product.lastPurchaseDate > 0) {
        val diff = (System.currentTimeMillis() - product.lastPurchaseDate) / (1000 * 60 * 60 * 24)
        when {
            diff == 0L -> "Hoy"
            diff == 1L -> "Ayer"
            diff < 7 -> "Hace $diff días"
            diff < 30 -> "Hace ${diff / 7} sem"
            else -> "Hace ${diff / 30} mes(es)"
        }
    } else "Nunca"

    val isOverdue = product.estimatedNextDate != null && 
                   product.estimatedNextDate > 0 && 
                   product.estimatedNextDate < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.originalName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
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
                            "Cada ${product.averageDaysBetween.toInt()} días" else "Sin datos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text("Última", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(daysSince, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ============ PESTAÑA 1: LISTA DE PRECIOS ============
@Composable
private fun PriceListTab(
    products: List<ProductFrequencyEntity>,
    selectedProduct: ProductFrequencyEntity?,
    priceHistory: List<ProductPriceHistoryEntity>,
    onProductSelect: (ProductFrequencyEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (products.isNotEmpty()) {
            ProductSelectorCards(products, selectedProduct, onProductSelect)
        }
        Spacer(modifier = Modifier.height(16.dp))

        when {
            selectedProduct == null -> EmptyState(emoji = "📈", title = "Selecciona un producto", subtitle = "")
            priceHistory.isEmpty() -> EmptyState(emoji = "📈", title = "Sin historial", subtitle = "para ${selectedProduct.originalName}")
            else -> PriceHistoryList(priceHistory)
        }
    }
}

@Composable
private fun PriceHistoryList(history: List<ProductPriceHistoryEntity>) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (history.isNotEmpty()) {
            val prices = history.map { it.price }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCol("Mín", "€${String.format("%.2f", prices.minOrNull()!!)}")
                        StatCol("Máx", "€${String.format("%.2f", prices.maxOrNull()!!)}")
                        StatCol("Media", "€${String.format("%.2f", prices.average())}")
                        StatCol("Compras", "${history.size}x")
                    }
                }
            }
        }

        items(history.sortedByDescending { it.fecha }) { record ->
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(dateFormat.format(Date(record.fecha)), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
                        Text("${record.quantity} uds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("€${String.format("%.2f", record.price)}", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ============ PESTAÑA 2: GRÁFICA 1 PRODUCTO ============
@Composable
private fun SingleChartTab(
    products: List<ProductFrequencyEntity>,
    selectedProduct: ProductFrequencyEntity?,
    priceHistory: List<ProductPriceHistoryEntity>,
    onProductSelect: (ProductFrequencyEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (products.isNotEmpty()) {
            ProductSelectorCards(products, selectedProduct, onProductSelect)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            selectedProduct == null -> EmptyState(emoji = "📉", title = "Selecciona un producto", subtitle = "Para ver su gráfica")
            priceHistory.isEmpty() -> EmptyState(emoji = "📉", title = "Sin datos", subtitle = "para ${selectedProduct.originalName}")
            else -> {
                Text(
                    text = "📈 ${selectedProduct.originalName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    SingleLineChart(
                        history = priceHistory.sortedBy { it.fecha },
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val prices = priceHistory.map { it.price }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCol("Mín", "€${String.format("%.2f", prices.minOrNull()!!)}")
                        StatCol("Máx", "€${String.format("%.2f", prices.maxOrNull()!!)}")
                        StatCol("Media", "€${String.format("%.2f", prices.average())}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleLineChart(
    history: List<ProductPriceHistoryEntity>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    val prices = history.map { it.price }
    val maxPrice = prices.maxOrNull() ?: 0f
    val minTime = history.minOf { it.fecha }
    val maxTime = history.maxOf { it.fecha }
    val timeRange = maxTime - minTime

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        val chartWidth = canvasWidth - 2 * padding
        val chartHeight = canvasHeight - 2 * padding

        // Ejes
        drawLine(surfaceVariant, Offset(padding, padding), Offset(padding, canvasHeight - padding), 2f)
        drawLine(surfaceVariant, Offset(padding, canvasHeight - padding), Offset(canvasWidth - padding, canvasHeight - padding), 2f)

        // Etiquetas eje Y (precio) - H1: textSize = 24f
        val ySteps = 5
        for (i in 0..ySteps) {
            val price = maxPrice * i / ySteps
            val y = canvasHeight - padding - (price / maxPrice) * chartHeight

            if (i > 0) {
                drawLine(surfaceVariant.copy(alpha = 0.3f), Offset(padding, y), Offset(canvasWidth - padding, y), 1f)
            }

            drawContext.canvas.nativeCanvas.drawText(
                "€${String.format("%.1f", price)}",
                5f,
                y + 5f,
                android.graphics.Paint().apply {
                    color = onSurfaceVariant.hashCode()
                    textSize = 24f // H1
                }
            )
        }

        val points = history.map { record ->
            val x = if (timeRange > 0) {
                padding + ((record.fecha - minTime).toFloat() / timeRange) * chartWidth
            } else {
                canvasWidth / 2
            }
            val y = canvasHeight - padding - (record.price / maxPrice) * chartHeight
            Offset(x, y)
        }

        if (points.size >= 2) {
            val areaPath = Path().apply {
                moveTo(points.first().x, canvasHeight - padding)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, canvasHeight - padding)
                close()
            }
            drawPath(areaPath, primaryColor.copy(alpha = 0.1f))

            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(linePath, primaryColor, style = Stroke(width = 3f, pathEffect = PathEffect.cornerPathEffect(10f)))
        }

        points.forEach { point ->
            drawCircle(primaryColor, 6f, point)
            drawCircle(Color.White, 3f, point)
        }
    }
}

// ============ PESTAÑA 3: COMPARATIVA MULTIPRODUCTO ============
@Composable
private fun MultiProductCompareTab(
    products: List<ProductFrequencyEntity>,
    viewModel: HistoryViewModel
) {
    val colors = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00BCD4)
    )

    var selectedProducts by remember { mutableStateOf<List<ProductChartData>>(emptyList()) }
    val histories = remember { mutableStateMapOf<String, List<ProductPriceHistoryEntity>>() }

    LaunchedEffect(selectedProducts) {
        selectedProducts.forEach { pd ->
            if (!histories.containsKey(pd.product.productName)) {
                val history = viewModel.getPriceHistoryForProduct(pd.product.productName)
                histories[pd.product.productName] = history
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "📊 Comparativa de productos (máx 6)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // H3: Grid de 2 columnas para productos disponibles
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(products.sortedBy { it.originalName }.take(12)) { product ->
                val isSelected = selectedProducts.any { it.product.productName == product.productName }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedProducts = selectedProducts.filter { it.product.productName != product.productName }
                        } else if (selectedProducts.size < 6) {
                            selectedProducts = selectedProducts + ProductChartData(
                                product = product,
                                history = histories[product.productName] ?: emptyList(),
                                color = colors[selectedProducts.size % colors.size]
                            )
                        }
                    },
                    label = { 
                        Text(
                            product.originalName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall // H5
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Productos seleccionados
        if (selectedProducts.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedProducts) { pd ->
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = { Text(pd.product.originalName, color = pd.color, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = {
                            IconButton(
                                onClick = { selectedProducts = selectedProducts.filter { it.product.productName != pd.product.productName } },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, "Quitar", tint = pd.color, modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedProducts.isEmpty()) {
            EmptyState(emoji = "📊", title = "Selecciona productos", subtitle = "Para comparar sus precios")
        } else {
            val updatedProducts = selectedProducts.map { pd ->
                pd.copy(history = histories[pd.product.productName] ?: emptyList())
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                MultiLineChart(
                    productsData = updatedProducts,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // H4: Leyenda con scroll
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxSize()
                ) {
                    Text("Leyenda:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(updatedProducts.filter { it.history.isNotEmpty() }) { pd ->
                            val min = pd.history.minOf { it.price }
                            val max = pd.history.maxOf { it.price }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(pd.color, MaterialTheme.shapes.small))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(pd.product.originalName, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) // H5
                                }
                                Text("€${String.format("%.2f", min)} - €${String.format("%.2f", max)}",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiLineChart(
    productsData: List<ProductChartData>,
    modifier: Modifier = Modifier
) {
    if (productsData.isEmpty()) return

    val allHistory = productsData.flatMap { it.history }
    if (allHistory.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Sin datos para mostrar", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxPrice = allHistory.maxOf { it.price }
    val minTime = allHistory.minOf { it.fecha }
    val maxTime = allHistory.maxOf { it.fecha }
    val timeRange = maxTime - minTime

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        val chartWidth = canvasWidth - 2 * padding
        val chartHeight = canvasHeight - 2 * padding

        drawLine(surfaceVariant, Offset(padding, padding), Offset(padding, canvasHeight - padding), 2f)
        drawLine(surfaceVariant, Offset(padding, canvasHeight - padding), Offset(canvasWidth - padding, canvasHeight - padding), 2f)

        // H1: textSize = 24f para eje Y
        for (i in 0..5) {
            val price = maxPrice * i / 5
            val y = canvasHeight - padding - (price / maxPrice) * chartHeight
            if (i > 0) {
                drawLine(surfaceVariant.copy(alpha = 0.2f), Offset(padding, y), Offset(canvasWidth - padding, y), 1f)
            }
            drawContext.canvas.nativeCanvas.drawText(
                "€${String.format("%.1f", price)}",
                5f, y + 5f,
                android.graphics.Paint().apply { color = onSurfaceVariant.hashCode(); textSize = 24f } // H1
            )
        }

        productsData.forEach { pd ->
            if (pd.history.isNotEmpty()) {
                val points = pd.history.sortedBy { it.fecha }.map { record ->
                    val x = if (timeRange > 0) {
                        padding + ((record.fecha - minTime).toFloat() / timeRange) * chartWidth
                    } else canvasWidth / 2
                    val y = canvasHeight - padding - (record.price / maxPrice) * chartHeight
                    Offset(x, y)
                }

                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(path, pd.color, style = Stroke(width = 3f, pathEffect = PathEffect.cornerPathEffect(10f)))
                }

                points.forEach { point ->
                    drawCircle(pd.color, 5f, point)
                    drawCircle(Color.White, 2f, point)
                }
            }
        }
    }
}

// ============ SELECTOR TIPO CARDS (H6) ============
@Composable
private fun ProductSelectorCards(
    products: List<ProductFrequencyEntity>,
    selected: ProductFrequencyEntity?,
    onSelect: (ProductFrequencyEntity) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(products.sortedBy { it.originalName }) { product ->
            val isSelected = selected?.productName == product.productName
            
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(80.dp)
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        ) else Modifier
                    ),
                onClick = { onSelect(product) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📦",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.originalName,
                        style = MaterialTheme.typography.labelSmall, // H5
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ============ COMPONENTES AUXILIARES ============
@Composable
private fun EmptyState(emoji: String, title: String, subtitle: String = "") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun StatCol(label: String, value: String) {
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
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("Producto", style = MaterialTheme.typography.labelSmall) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            products.sortedBy { it.originalName }.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.originalName, style = MaterialTheme.typography.labelSmall) },
                    onClick = { onSelect(product); expanded = false }
                )
            }
        }
    }
}
ct.originalName, style = MaterialTheme.typography.labelSmall) },
                    onClick = { onSelect(product); expanded = false }
                )
            }
        }
    }
}
