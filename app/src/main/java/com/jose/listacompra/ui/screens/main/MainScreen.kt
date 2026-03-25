package com.jose.listacompra.ui.screens.main


//import androidx.compose.material.icons.filled.Mic
//import androidx.compose.material.icons.filled.Palette
//import androidx.compose.material.icons.filled.QrCodeScanner
//import androidx.compose.material.icons.filled.RemoveDone
//import androidx.compose.material.icons.filled.Sort
//import androidx.compose.material.icons.filled.UploadFile
//import androidx.compose.material.icons.filled.DarkMode
//import androidx.compose.material.icons.filled.DeleteForever
//import androidx.compose.material.icons.filled.AutoAwesome

//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainScreen(
//    viewModel: ShoppingListViewModel = hiltViewModel(),
//    currentPrimaryColor: Int = 0xFF4CAF50.toInt(),
//    onColorChanged: (Int) -> Unit = {},
//    onNavigateToLists: () -> Unit = {},
//    onToggleTheme: () -> Unit = {},
//    onClearList: (Boolean) -> Unit = {}
//) {
//    // 1. Estado de la pestaña activa
//    //  var currentScreen by remember { mutableStateOf(Screen.SHOPPING_LIST) }
//
//    val uiState by viewModel.uiState.collectAsState()
//    val context = LocalContext.current
//
//    // Navigation controller
//    val navController = rememberNavController()
//    val currentRoute by navController.currentBackStackEntryAsState()
//    val currentRouteString = currentRoute?.destination?.route
//
//    var activeDialog by remember { mutableStateOf<DialogType?>(DialogType.None) }
//    var showSnackbar by remember { mutableStateOf<String?>(null) }
//    var showThemeMenu by remember { mutableStateOf(false) }
//
//    // Estado previo para detectar cuando se completa toda la lista
//    var wasListComplete by remember { mutableStateOf(false) }
//    val snackbarHostState = remember { SnackbarHostState() }
//    // Booleanos para condicionales
//    val isOnList = currentRouteString == Route.ShoppingList.route
//
//    val canGoBack = navController.previousBackStackEntry != null
//    // Detectar cuando se completa toda la lista para vibración especial
//    LaunchedEffect(uiState.purchasedCount, uiState.totalCount) {
//        val isNowComplete = uiState.totalCount > 0 && uiState.purchasedCount == uiState.totalCount
//        if (isNowComplete && !wasListComplete) {
//            // Lista completada - vibración de éxito
//            context.vibrateFeedback(context, isCompletion = true)
//        }
//        wasListComplete = isNowComplete
//    }
//
//    // Mostrar snackbar cuando hay mensaje
//    LaunchedEffect(showSnackbar) {
//        showSnackbar?.let { message ->
//            val result = snackbarHostState.showSnackbar(
//                message = message,
//                actionLabel = "DESHACER"
//            )
//            if (result == SnackbarResult.ActionPerformed) {
//                // TODO: Implementar deshacer
//            }
//            showSnackbar = null
//        }
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        // ===== TOP APP BAR (común para todas las pantallas) =====
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        // Título según la ruta actual
//                        Text(
//                            text = when (currentRouteString) {
//                                Route.ShoppingList.route -> "🛒 ${uiState.currentList?.name ?: "Lista"}"
//                                Route.Catalogo.route -> "📦 Catálogo"
//                                Route.ImportTicket.route -> "📄 Importar Ticket"
//                                Route.ColorSettings.route -> "🎨 Color"
//                                Route.ProductHistory.route -> "📋 Historial"
//                                Route.BarcodeScanner.route -> "📷 Escanear"
//                                else -> "Lista Compra"
//                            },
//                            maxLines = 1
//                        )
//
//                        // Subtítulo solo en la lista principal
//                        if (isOnList && uiState.totalCount > 0) {
//                            Text(
//                                text = "${uiState.purchasedCount}/${uiState.totalCount} productos",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                },
//
//                // Botón de retroceso (aparece cuando hay backstack)
//                navigationIcon = {
//                    if (canGoBack) {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack, contentDescription = "Volver"
//                            )
//                        }
//                    }
//                },
//
//                // ===== ACTIONS (Menú siempre disponible) =====
//                actions = {
//                    // AudioButton solo en lista principal
//                    if (isOnList) {
//                        AudioButton(
//                            viewModel = viewModel,
//                            uiState = uiState,
//                            onCommand = { command ->
//                                when (command) {
//                                    "mostrar_productos" ->
//                                        uiState.currentList?.let { list ->
//                                            navController.navigate(Route.ShoppingList.route)
//                                        }
//
//                                    "limpiar_comprados" -> onClearList(false)
//                                    "vaciar_lista" -> onClearList(true)
//                                    //Todo                               else -> viewModel.processVoiceCommand(command)
//                                }
//                            }
//                        )
//                    }
//
//                    // Menú hamburguesa común
//                    CommonMenu(
//                        navController = navController,
//                        isOnList = isOnList,
//                        onNavigateToLists = onNavigateToLists,
//                        onToggleTheme = onToggleTheme,
//                        onNavigateToColor = { navController.navigate(Route.ColorSettings.route) },
//                        onShowManageAisles = { activeDialog = DialogType.ManageAisles },
//                        onNavigateToImport = { navController.navigate(Route.ImportTicket.route) },
//                        onClearList = onClearList
//                    )
//                }
//            )
//        },
//
//// 2. LA BARRA INFERIOR
//        bottomBar = {
//            Column {
//                // Si estamos en la lista, mostramos la barra de totales encima de la nav
//                if (isOnList) {
//                    TotalsBar(
//                        totalWithOffers = uiState.totalEstimate,
//                        totalWithoutOffers = uiState.totalWithoutOffers,
//                        savings = uiState.savings,
//                        purchasedCount = uiState.purchasedCount,
//                        totalCount = uiState.totalCount
//                    )
//                }
//
//                NavigationBar {
//                    NavigationBarItem(
//                        selected = isOnList,
//                        onClick = { if (!isOnList) navController.navigate(Route.ShoppingList.route) },
//                        icon = {
//                            Icon(
//                                imageVector = Icons.Default.ShoppingCart,
//                                contentDescription = null
//                            )
//                        },
//                        label = {
//                            Text("Mi Lista")
//                        }
//                    )
//                    NavigationBarItem(
//                        selected = currentRouteString == Route.Catalogo.route,
//                        onClick = {
//                            if (currentRouteString != Route.Catalogo.route) {
//                                navController.navigate(Route.Catalogo.route)
//                            }
//                        },
//                        label = { Text("Artículos") },
//                        icon = {
//                            Icon(
//                                Icons.Default.Inventory, contentDescription = null
//                            )
//                        }
//                    )
//                }
//            }
//        },
//        floatingActionButton = {
//            // Solo mostramos el FAB de añadir si estamos en la lista
//            if (isOnList) {
//                // Menú desplegable de opciones para añadir producto
//                var showAddMenu by remember { mutableStateOf(false) }
//                Box {
//                    // Botón principal FAB
//                    FloatingActionButton(
//                        onClick = { showAddMenu = !showAddMenu }
//                    ) {
//                        Icon(
//                            imageVector = if (showAddMenu) Icons.Default.Close else Icons.Default.Add,
//                            contentDescription = if (showAddMenu) "Cerrar menú" else "Añadir producto"
//                        )
//                    }
//
//                    // Menú desplegable con 3 opciones
//                    DropdownMenu(
//                        expanded = showAddMenu,
//                        onDismissRequest = { showAddMenu = false },
//                        modifier = Modifier.Companion.padding(bottom = 8.dp)
//                    ) {
//                        // Opción 1: Por Voz
//                        DropdownMenuItem(
//                            text = { Text("🎙️ Por Voz") },
//                            onClick = {
//                                showAddMenu = false
//                                // TODO: Abrir diálogo de voz
//                                activeDialog =
//                                    DialogType.AddProduct  // Por ahora, usamos el diálogo actual
//                            },
//                            leadingIcon = {
//                                Icon(
//                                    Icons.Default.Mic,
//                                    contentDescription = null
//                                )
//                            }
//                        )
//
//                        // Opción 2: Escribir Nombre
//                        DropdownMenuItem(
//                            text = { Text("⌨️ Escribir Nombre") },
//                            onClick = {
//                                activeDialog = DialogType.AddProduct
//                            },
//                            leadingIcon = {
//                                Icon(Icons.Default.Edit, contentDescription = null)
//                            }
//                        )
//
//                        // Opción 3: Desde Historial
//                        DropdownMenuItem(
//                            text = { Text("📋 Desde Historial") },
//                            onClick = {
//                                //   showAddMenu = false
//                                activeDialog =
//                                    DialogType.ShowProductHistory // Abrir pantalla de historial
//                            },
//                            leadingIcon = {
//                                Icon(Icons.Default.List, contentDescription = null)
//                            }
//                        )
//
//                        // Opción 4: Escanear Código de Barras
//                        DropdownMenuItem(
//                            text = { Text("📷 Escanear Código") },
//                            onClick = {
//                                // showAddMenu = false
//                                activeDialog =
//                                    DialogType.ShowBarcodeScanner // Abrir pantalla de escáner
//                            },
//                            leadingIcon = {
//                                Icon(
//                                    Icons.Default.Add, // QrCodeScanner,
//                                    contentDescription = null
//                                )
//                            }
//                        )
//                    }
//                }
//            }
//        },
////
//    ) { padding ->
//
////        NavHost(
////            navController = navController,
////            startDestination = Route.ShoppingList.route,
////            modifier = Modifier.padding(padding)  // Respeta las barras
////        ) {
////            // ---- PANTALLA PRINCIPAL: Lista de la compra ----
////            composable(Route.ShoppingList.route) {
////                ShoppingListContent(
////                    uiState = uiState,
////                    viewModel = viewModel,
////                    onEditProduct = { product ->
////                        activeDialog = DialogType.EditProduct(product)
////                    },
////                    snackbarHostState = snackbarHostState
////                )
////            }
////
////            // ---- PANTALLA: Catálogo ----
////            composable(Route.Catalogo.route) {
////                // Tu contenido de catálogo (simplificado por ahora)
////                Box(
////                    modifier = Modifier.fillMaxSize(),
////                    contentAlignment = Alignment.Center
////                ) {
////                    Text("Catálogo (en desarrollo)")
////                }
////            }
////
////            // ---- PANTALLA: Importar Ticket (antes era dialog) ----
////            composable(Route.ImportTicket.route) {
////                ImportTicketScreen(
////                    onNavigateBack = { navController.popBackStack() }
////                )
////            }
////
////            // ---- PANTALLA: Configuración de Color (antes era dialog) ----
////            composable(Route.ColorSettings.route) {
////                ColorSettingsScreen(
////                    currentColor = currentPrimaryColor,
////                    onColorSelected = onColorChanged,
////                    onNavigateBack = { navController.popBackStack() }
////                )
////            }
////
////            // ---- PANTALLA: Historial de Productos (antes FAB->dialog) ----
////            composable(Route.ProductHistory.route) {
////                ProductHistoryScreen(
////                    onProductSelected = { product ->
////                        // Añadir a la lista y volver
////                        viewModel.addProduct(product.name)
////                        navController.popBackStack()
////                    },
////                    onNavigateBack = { navController.popBackStack() }
////                )
////            }
////
////            // ---- PANTALLA: Escanear Código (antes FAB->dialog) ----
////            composable(Route.BarcodeScanner.route) {
////                BarcodeScannerScreen(
////                    onBarcodeScanned = { barcode ->
////                        // Procesar el código y volver
////                        viewModel.processScannedBarcode(barcode)
////                        navController.popBackStack()
////                    },
////                    onNavigateBack = { navController.popBackStack() }
////                )
////            }
////        }
//
//        // ============================================
//        // DIALOGOS MODALES (fuera del NavHost)
//        // Flotan ENCIMA de todo, no son navegación
//        // ============================================
//
//        when (val dialog = activeDialog) {
//
//            // ---- Dialog: Añadir Producto ----
//            is DialogType.AddProduct -> {
//                AddProductDialog(
//                    aisles = uiState.aisles,
//                    offers = TODO(),
//                    suggestions = TODO(),
//                    onSearchSuggestions = TODO(),
//                    onCalculateOffer = TODO(),
//                    onDismiss = {
//                        activeDialog = DialogType.None },
//                    onAdd = {
//                        name, aisle, price, quantity, unit ->
//                        viewModel.addProduct(name, aisle, price, quantity, unit)
//                        activeDialog = DialogType.None
//                    },
//                )
//            }
//
//            // ---- Dialog: Editar Producto ----
//            is DialogType.EditProduct -> {
//                EditProductDialog(
//                    product = dialog.product,
//                    aisles = uiState.aisles,
//                    onDismiss = { activeDialog = DialogType.None },
//                    onSave = { updated ->
//                        viewModel.updateProduct(updated)
//                        activeDialog = DialogType.None
//                    },
//                    onDelete = {
//                        viewModel.deleteProduct(dialog.product.id)
//                        activeDialog = DialogType.None
//                    }
//                )
//            }
//
//            // ---- Dialog: Gestionar Pasillos ----
//            is DialogType.ManageAisles -> {
//                ManageAislesDialog(
//                    aisles = uiState.aisles,
//                    onDismiss = { activeDialog = DialogType.None },
//                    onAdd = { name, icon ->
//                        viewModel.addAisle(name, icon)
//                    },
//                    onDelete = { aisle ->
//                        viewModel.deleteAisle(aisle)
//                    },
//                    onUpdate = { oldName, newName ->
//                        viewModel.updateAisle(oldName, newName)
//                    },
//                    onReorder = { reorderedAisles ->
//                        viewModel.reorderAisles(reorderedAisles)
//                    }
//                )
//            }
//
//            // Nada activo
//            DialogType.None -> {}
//            else -> {
//                //Todo
//            }
//        }
//    }
//}
//
//@Composable
//fun AudioButton(
//    viewModel: ShoppingListViewModel,
//    uiState: ShoppingListUiState, // O el tipo de estado que uses
//    onCommand: (String) -> Unit
//) {
//    IconButton(onClick = {
//        // Aquí iría la lógica de activación del micrófono
//        // Por ahora simulamos un comando para probar
//        onCommand("mostrar_productos")
//    }) {
//        Icon(
//            imageVector = Icons.Default.AccountBox,
//            contentDescription = "Comando de voz",
//            tint = MaterialTheme.colorScheme.primary
//        )
//    }
//}
//
//// ============================================
//// COMPOSED COMPONENTS (extraer para limpieza)
//// ============================================
//
//@Composable
//private fun CommonMenu(
//    navController: NavHostController,
//    isOnList: Boolean,
//    onNavigateToLists: () -> Unit,
//    onToggleTheme: () -> Unit,
//    onNavigateToColor: () -> Unit,
//    onShowManageAisles: () -> Unit,
//    onNavigateToImport: () -> Unit,
//    onClearList: (Boolean) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    IconButton(onClick = { expanded = true }) {
//        Icon(Icons.Default.Settings, contentDescription = "Menú")
//    }
//
//    DropdownMenu(
//        expanded = expanded,
//        onDismissRequest = { expanded = false }
//    ) {
//        // Navegar a Mis Listas
//        DropdownMenuItem(
//            text = { Text("📋 Mis Listas") },
//            onClick = {
//                expanded = false
//                onNavigateToLists()
//            },
//            leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, null) }
//        )
//
//        HorizontalDivider()
//
//        // Preferencias visuales
//        DropdownMenuItem(
//            text = { Text("🌙☀️ Modo Oscuro/Claro") },
//            onClick = {
//                expanded = false
//                onToggleTheme()
//            },
//            leadingIcon = {
//                Icon(
//                    Icons.Default.DarkMode,
//                    null
//                )
//            }
//        )
//
//        DropdownMenuItem(
//            text = { Text("🎨 Cambiar Color") },
//            onClick = {
//                expanded = false
//                onNavigateToColor()
//            },
//            leadingIcon = {
//                Icon(
//                    Icons.Default.Palette,
//                    null
//                )
//            }
//        )
//
//        DropdownMenuItem(
//            text = { Text("🗂️ Gestionar Pasillos") },
//            onClick = {
//                expanded = false
//                onShowManageAisles()
//            },
//            leadingIcon = {
//                Icon(
//                    Icons.Default.Dashboard,
//                    null
//                )
//            }
//        )
//
//        HorizontalDivider()
//
//        // Importaciones
//        DropdownMenuItem(
//            text = { Text("📄 Importar Ticket PDF") },
//            onClick = {
//                expanded = false
//                onNavigateToImport()
//            },
//            leadingIcon = {
//                Icon(
//                    Icons.Default. UploadFile,
//                    null
//                )
//            }
//        )
//
//        // Acciones de lista (solo si estamos en la lista)
//        if (isOnList) {
//            HorizontalDivider()
//
//            DropdownMenuItem(
//                text = { Text("🧹 Quitar Comprados") },
//                onClick = {
//                    expanded = false
//                    onClearList(false)
//                },
//                leadingIcon = {
//                    Icon(
//                        Icons.Default. RemoveDone,
//                        null
//                    )
//                }
//            )
//
//            DropdownMenuItem(
//                text = {
//                    Text("🗑️ Vaciar Lista Completa")
//                },
//                onClick = {
//                    expanded = false
//                    onClearList(true)
//                },
//                leadingIcon = {
//                    Icon(
//                        Icons.Default. DeleteForever,
//                        null
//                    )
//                },
//                modifier = Modifier
//            )
//        }
//    }
//}
//
//@Composable
//private fun ShoppingBottomBar(
//    uiState: ShoppingListUiState,
//    viewModel: ShoppingListViewModel,
//    onClearList: (Boolean) -> Unit
//) {
//    BottomAppBar {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Categorizar
//            IconButton(onClick = { viewModel.categorizeProducts() }) {
//                Icon(
//                    Icons.Default.AutoAwesome,
//                    "Categorizar"
//                )
//            }
//
//            // Ordenar por pasillo
//            IconButton(onClick = { viewModel.toggleSortOrder() }) {
//                Icon(
//                    Icons.AutoMirrored.Filled.Sort,
//                    "Ordenar"
//                )
//            }
//        }
//    }
//}
//
//
//
