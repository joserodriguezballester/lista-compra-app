# Pasos a Seguir 7 - Vaciar Lista con Confirmación

## Requisito
Botón visible en pantalla principal para vaciar la lista, con diálogo de confirmación.

---

## 1. Añadir Botón en TopAppBar

```kotlin
// MainScreen.kt - TopAppBar actions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ShoppingListViewModel,
    // ... otros parámetros
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }
    var showEmptySuccessSnackbar by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.currentList?.name ?: "Lista de Compra") },
                actions = {
                    // Botón vaciar lista (solo si hay productos)
                    if (uiState.products.isNotEmpty()) {
                        IconButton(
                            onClick = { showEmptyConfirmDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Vaciar lista"
                            )
                        }
                    }
                    
                    // Menú de tres puntos (otras opciones)
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, "Más opciones")
                    }
                    
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        // Mover "Vaciar" también aquí como alternativa
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Vaciar lista",
                                    color = MaterialTheme.colorScheme.error
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DeleteSweep, 
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showEmptyConfirmDialog = true
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Gestionar pasillos") },
                            onClick = { /* ... */ }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Gestionar categorías") },
                            onClick = { /* ... */ }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Archivar lista") },
                            onClick = { /* ... */ }
                        )
                    }
                }
            )
        },
        // ... resto del Scaffold
    ) { paddingValues ->
        // Contenido principal...
        
        // Diálogo de confirmación
        if (showEmptyConfirmDialog) {
            EmptyListConfirmDialog(
                productCount = uiState.products.size,
                onConfirm = {
                    viewModel.emptyCurrentList()
                    showEmptyConfirmDialog = false
                    showEmptySuccessSnackbar = true
                },
                onDismiss = { showEmptyConfirmDialog = false }
            )
        }
        
        // Snackbar de éxito
        if (showEmptySuccessSnackbar) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showEmptySuccessSnackbar = false
            }
        }
    }
}
```

---

## 2. Diálogo de Confirmación

```kotlin
@Composable
fun EmptyListConfirmDialog(
    productCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("¿Vaciar lista?")
        },
        text = {
            Column {
                Text(
                    "Se eliminarán $productCount productos de la lista actual."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                // Opciones adicionales
                var deleteFromHistory by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Checkbox(
                        checked = deleteFromHistory,
                        onCheckedChange = { deleteFromHistory = it }
                    )
                    Text(
                        "También borrar del historial de precios",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Vaciar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
```

---

## 3. ViewModel - Método emptyCurrentList()

```kotlin
// ShoppingListViewModel.kt

fun emptyCurrentList(deleteFromHistory: Boolean = false) {
    viewModelScope.launch {
        val listId = _currentListId.value ?: return@launch
        
        // Opción 1: Eliminar todos los productos
        repository.deleteAllProductsInList(listId)
        
        // Opción 2 (alternativa): Marcar como comprados y mover a historial
        // repository.markAllAsPurchased(listId)
        
        if (deleteFromHistory) {
            // Opcional: borrar también del historial de precios
            repository.deletePriceHistoryForList(listId)
        }
        
        // Haptic feedback
        // _hapticFeedback.trySend(HapticFeedbackType.Heavy)
        
        // Recargar datos
        loadData()
    }
}

// Versión alternativa: Mover a historial en lugar de borrar
fun archiveCompletedList() {
    viewModelScope.launch {
        val listId = _currentListId.value ?: return@launch
        
        // Crear registro de compra completada
        val products = repository.getAllProducts(listId)
        val total = products.sumOf { it.finalPriceToPay().toDouble() }.toFloat()
        
        repository.savePurchaseHistory(
            listId = listId