package com.jose.listacompra.ui.screens.offers

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
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.ui.components.AppDrawer
import com.jose.listacompra.ui.components.CommonBottomBar
import com.jose.listacompra.ui.components.CommonTopBar
import com.jose.listacompra.ui.viewmodel.OffersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    onChangeColor: () -> Unit = {},
    viewModel: OffersViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var offerToEdit by remember { mutableStateOf<Offer?>(null) }
    var offerToDelete by remember { mutableStateOf<Offer?>(null) }

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
                    // Ya estamos en ofertas
                },
                onNavigateToCategories = {
                    scope.launch { drawerState.close() }
                    onNavigateToCategories()
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
                    title = "🏷️ Ofertas",
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onChangeColor = onChangeColor,
                    onToggleDarkMode = onToggleDarkMode,
                    isDarkMode = isDarkMode,
                    overflowActions = { expanded, onDismiss ->
                        DropdownMenuItem(
                            text = { Text("Añadir oferta") },
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
                    currentRoute = "ofertas"
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
            } else if (uiState.offers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay ofertas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir oferta")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.offers, key = { it.id }) { offer ->
                        OfferCard(
                            offer = offer,
                            onEdit = { offerToEdit = offer },
                            onDelete = { offerToDelete = offer }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo añadir
    if (showAddDialog) {
        OfferDialog(
            onDismiss = { showAddDialog = false },
            onSave = { offer ->
                viewModel.addOffer(offer)
                showAddDialog = false
            }
        )
    }
    
    // Diálogo editar
    if (offerToEdit != null) {
        OfferDialog(
            offer = offerToEdit,
            onDismiss = { offerToEdit = null },
            onSave = { offer ->
                viewModel.updateOffer(offer)
                offerToEdit = null
            }
        )
    }
    
    // Diálogo confirmar borrado
    if (offerToDelete != null) {
        AlertDialog(
            onDismissRequest = { offerToDelete = null },
            title = { Text("Eliminar oferta") },
            text = { Text("¿Seguro que quieres eliminar \"${offerToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        offerToDelete?.let { viewModel.deleteOffer(it) }
                        offerToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { offerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OfferCard(
    offer: Offer,
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
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = offer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = offer.code,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
fun OfferDialog(
    offer: Offer? = null,
    onDismiss: () -> Unit,
    onSave: (Offer) -> Unit
) {
    var name by remember { mutableStateOf(offer?.name ?: "") }
    var code by remember { mutableStateOf(offer?.code ?: "") }
    var description by remember { mutableStateOf(offer?.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (offer == null) "Nueva oferta" else "Editar oferta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Código") },
                    placeholder = { Text("3x2, 2nd_50...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("3x2, 2ª unidad -50%...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Lleva 3, paga 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Offer(
                            id = offer?.id ?: 0,
                            code = code,
                            name = name,
                            description = description,
                            isDefault = offer?.isDefault ?: false,
                            formula = offer?.formula ?: ""
                        )
                    )
                },
                enabled = name.isNotBlank() && code.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDrawerState(initialValue: DrawerValue): androidx.compose.material3.DrawerState {
    return androidx.compose.material3.rememberDrawerState(initialValue = initialValue)
}
