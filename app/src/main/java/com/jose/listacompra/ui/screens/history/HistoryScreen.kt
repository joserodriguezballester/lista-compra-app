package com.jose.listacompra.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.components.startDirectVoiceRecognition
import com.jose.listacompra.ui.viewmodel.HistoryViewModel
import com.jose.listacompra.ui.viewmodel.ProductListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
                },
                onNavigateToTicketImport = {}
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
                    onMicrophoneClick = { startDirectVoiceRecognition(context, productListViewModel, scope) }
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

@Composable
fun rememberDrawerState(initialValue: DrawerValue): androidx.compose.material3.DrawerState {
    return androidx.compose.material3.rememberDrawerState(initialValue = initialValue)
}
