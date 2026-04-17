package com.jose.listacompra.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.ui.components.AppDrawerScaffold
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.startDirectVoiceRecognition
import com.jose.listacompra.ui.viewmodel.HistoryViewModel
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
import com.jose.listacompra.ui.navigation.AppNavigator
import com.jose.listacompra.ui.navigation.DrawerDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navigator: AppNavigator,
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    onChangeColor: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel(),
    productListViewModel: ProductListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    AppDrawerScaffold(
        title = "📊 Historial",
        navigator = navigator,
        currentDestination = DrawerDestination.History,
        onChangeColor = onChangeColor,
        onToggleDarkMode = onToggleDarkMode,
        isDarkMode = isDarkMode,
        onMicrophoneClick = { context, scope -> startDirectVoiceRecognition(context, productListViewModel, scope) },
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

