package com.jose.listacompra.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.viewmodel.CategoriesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    onChangeColor: () -> Unit = {},
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                onNavigateToOffers = {
                    scope.launch { drawerState.close() }
                    onNavigateToOffers()
                },
                onNavigateToCategories = {
                    scope.launch { drawerState.close() }
                },
                onNavigateToHistory = {
                    scope.launch { drawerState.close() }
                    onNavigateToHistory()
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
                    title = "📂 Categorías",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onChangeColor = onChangeColor,
                    onToggleDarkMode = onToggleDarkMode,
                    isDarkMode = isDarkMode,
                    overflowActions = { expanded, onDismiss ->
                        DropdownMenuItem(
                            text = { Text("Añadir categoría") },
                            onClick = {
                                showAddDialog = true
                                onDismiss()
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                )
            },
            bottomBar = {
                CommonBottomBar(
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToList = onNavigateToList,
                    currentRoute = "categorias"
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay categorías",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir categoría")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            onEdit = { categoryToEdit = category },
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo añadir
    if (showAddDialog) {
        CategoryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { category ->
                viewModel.addCategory(category)
                showAddDialog = false
            }
        )
    }
    
    // Diálogo editar
    if (categoryToEdit != null) {
        CategoryDialog(
            category = categoryToEdit,
            onDismiss = { categoryToEdit = null },
            onSave = { category ->
                viewModel.updateCategory(category)
                categoryToEdit = null
            }
        )
    }
    
    // Diálogo confirmar borrado
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Eliminar categoría") },
            text = { Text("¿Seguro que quieres eliminar \"${categoryToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.id?.let { viewModel.deleteCategory(it) }
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    category: Category? = null,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "📦") }
    
    val commonIcons = listOf("🍎", "🥩", "🐟", "🥛", "🍞", "🥤", "🥫", "🧊", "🧴", "🧼", "🐕", "👶", "🏠", "📦", "🧀", "🥚", "🍚", "☕", "🍪", "🧻")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Nueva categoría" else "Editar categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "Icono:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = icon,
                    style = MaterialTheme.typography.displayMedium
                )
                
                // Selector de iconos comunes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonIcons.forEach { emoji ->
                        FilterChip(
                            selected = icon == emoji,
                            onClick = { icon = emoji },
                            label = { Text(emoji, fontSize = androidx.compose.ui.unit.TextUnit.Unspecified) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Category(
                            id = category?.id ?: 0,
                            name = name,
                            icon = icon
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
