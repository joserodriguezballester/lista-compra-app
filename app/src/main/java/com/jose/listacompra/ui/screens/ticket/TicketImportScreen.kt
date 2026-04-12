@file:OptIn(ExperimentalMaterial3Api::class)
package com.jose.listacompra.ui.screens.ticket

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.TicketLine
import com.jose.listacompra.ui.viewmodel.ImportStep
import com.jose.listacompra.ui.viewmodel.TicketImportUiState
import com.jose.listacompra.ui.viewmodel.TicketImportViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TicketImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: TicketImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Ticket") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState.step) {
            ImportStep.SELECT_FILE -> SelectFileStep(
                modifier = Modifier.padding(padding),
                error = uiState.error,
                debugLog = uiState.debugLog,
                onDismissError = { viewModel.clearError() },
                onFileSelected = { viewModel.importTicket(it) }
            )
            ImportStep.LOADING -> LoadingStep(
                modifier = Modifier.padding(padding),
                debugLog = uiState.debugLog
            )
            ImportStep.REVIEW -> ReviewStep(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onConfirmMatch = { lineId, articuloId -> viewModel.confirmMatch(lineId, articuloId) },
                onCreateArticulo = { lineId, name, catId -> viewModel.createArticuloForLine(lineId, name, catId) },
                onSave = { viewModel.saveTicket() },
                onCancel = { viewModel.cancel() }
            )
            ImportStep.COMPLETE -> CompleteStep(
                modifier = Modifier.padding(padding),
                ticketId = uiState.savedTicketId,
                onDone = onNavigateBack,
                onImportAnother = { viewModel.reset() }
            )
        }
    }
}

@Composable
private fun SelectFileStep(
    modifier: Modifier,
    error: String?,
    debugLog: List<String>,
    onDismissError: () -> Unit,
    onFileSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) onFileSelected(uri)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Importar Ticket de Carrefour",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona un archivo PDF del ticket",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { launcher.launch(arrayOf("application/pdf")) }) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar PDF")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Esta versión muestra una traza del proceso para depurar el fallo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onDismissError) { Text("Cerrar") }
                }
            }
        }

        if (debugLog.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            DebugLogCard(debugLog)
        }
    }
}

@Composable
private fun LoadingStep(modifier: Modifier, debugLog: List<String>) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Procesando ticket...", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Extracción y parsing en curso",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (debugLog.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            DebugLogCard(debugLog)
        }
    }
}

@Composable
private fun DebugLogCard(debugLog: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Traza", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    debugLog.forEach { line ->
                        Text("• $line", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(
    modifier: Modifier,
    uiState: TicketImportUiState,
    onConfirmMatch: (Int, Long) -> Unit,
    onCreateArticulo: (Int, String, Long?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val ticket = uiState.ticket ?: return

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ticket.supermarketName ?: "Carrefour",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ticket.fecha),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${ticket.lines.size} productos", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Total: %.2f €".format(ticket.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.unmatchedCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ ${uiState.unmatchedCount} productos sin matchear",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (uiState.debugLog.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                DebugLogCard(uiState.debugLog)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(ticket.lines) { index, line ->
                TicketLineCard(
                    line = line,
                    lineIndex = index,
                    articulos = uiState.articulos,
                    categories = uiState.categories,
                    onConfirmMatch = { articuloId -> onConfirmMatch(index, articuloId) },
                    onCreateArticulo = { name, catId -> onCreateArticulo(index, name, catId) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(onClick = onSave, modifier = Modifier.weight(1f), enabled = !uiState.isSaving) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun TicketLineCard(
    line: TicketLine,
    lineIndex: Int,
    articulos: List<Articulo>,
    categories: List<Category>,
    onConfirmMatch: (Long) -> Unit,
    onCreateArticulo: (String, Long?) -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val isMatched = line.articuloId != null || line.articuloNombre != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (line.confirmado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = line.nombreOriginal,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${line.cantidad} uds × %.2f €".format(line.precioUnitario),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.2f €".format(line.precioTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isMatched) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(line.articuloNombre ?: "Matcheado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    if (line.confirmado) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✓", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { showSearch = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buscar", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(onClick = { showCreateDialog = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crear", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    if (showSearch) {
        AlertDialog(
            onDismissRequest = { showSearch = false },
            title = { Text("Buscar producto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Buscar") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val filtered = articulos.filter { it.name.contains(searchText, ignoreCase = true) }
                        items(filtered.size) { index ->
                            val articulo = filtered[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onConfirmMatch(articulo.id)
                                    showSearch = false
                                }
                            ) {
                                Text(text = articulo.name, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearch = false }) { Text("Cancelar") }
            }
        )
    }

    if (showCreateDialog) {
        var newName by remember { mutableStateOf(line.nombreOriginal) }
        var selectedCategory by remember { mutableStateOf<Long?>(line.categoriaId) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear nuevo artículo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Categoría (opcional)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = categories.find { it.id == selectedCategory }?.name ?: "Seleccionar",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Sin categoría") },
                                onClick = {
                                    selectedCategory = null
                                    expanded = false
                                }
                            )
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.icon} ${cat.name}") },
                                    onClick = {
                                        selectedCategory = cat.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onCreateArticulo(newName, selectedCategory)
                    showCreateDialog = false
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CompleteStep(
    modifier: Modifier,
    ticketId: Long?,
    onDone: () -> Unit,
    onImportAnother: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("¡Ticket importado!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Los productos se han añadido al historial",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDone) { Text("Volver al historial") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onImportAnother) { Text("Importar otro ticket") }
    }
}
