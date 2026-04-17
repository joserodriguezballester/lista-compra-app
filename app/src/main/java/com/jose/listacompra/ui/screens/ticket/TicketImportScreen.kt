@file:OptIn(ExperimentalMaterial3Api::class)
package com.jose.listacompra.ui.screens.ticket

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.listacompra.R
import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.model.Category
import com.jose.listacompra.domain.model.TicketLine
import com.jose.listacompra.ui.screens.catalogo.AddEditArticuloDialog
import com.jose.listacompra.ui.viewmodel.ImportStep
import com.jose.listacompra.ui.viewmodel.TicketImportUiState
import com.jose.listacompra.ui.viewmodel.TicketImportViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TicketImportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
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
                onFileSelected = { viewModel.importTicket(it) },
                onDebugTicket = { viewModel.importDebugTicket() }
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
                onNavigateToHome = onNavigateToHome,
                onNavigateToHistory = onNavigateToHistory,
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
    onFileSelected: (Uri) -> Unit,
    onDebugTicket: () -> Unit
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
            text = "Importar ticket",
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onDebugTicket) {
            Icon(Icons.Default.BugReport, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Debug ticket ejemplo (AAA.pdf)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Importa un PDF y revisa antes de guardar los datos detectados.",
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
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MatchedTicketLineRow(line: TicketLine) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                ) {}
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getTicketLineEmoji(line.articuloNombre ?: line.nombreOriginal),
                        fontSize = 24.sp
                    )
                }

                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Macheado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo_carrefour),
                        contentDescription = "Carrefour",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = line.articuloNombre ?: line.nombreOriginal,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${line.cantidad} uds · ${String.format("%.2f", line.precioUnitario)} €/ud",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${String.format("%.2f", line.precioTotal)} €",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

private fun getTicketLineEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("leche") -> "🥛"
        lower.contains("pan") -> "🍞"
        lower.contains("huevo") -> "🥚"
        lower.contains("yogur") -> "🥛"
        lower.contains("queso") -> "🧀"
        lower.contains("tomate") -> "🍅"
        lower.contains("platano") || lower.contains("plátano") -> "🍌"
        lower.contains("manzana") -> "🍎"
        lower.contains("naranja") -> "🍊"
        lower.contains("pollo") -> "🍗"
        lower.contains("carne") -> "🥩"
        lower.contains("pescado") -> "🐟"
        lower.contains("galleta") -> "🍪"
        lower.contains("cafe") || lower.contains("café") -> "☕"
        lower.contains("aceite") -> "🫒"
        lower.contains("agua") -> "💧"
        lower.contains("cerveza") -> "🍺"
        lower.contains("vino") -> "🍷"
        lower.contains("detergente") -> "🧼"
        lower.contains("papel") -> "🧻"
        lower.contains("jabon") || lower.contains("jabón") -> "🧴"
        else -> "📦"
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
    val indexedLines = ticket.lines.mapIndexed { index, line -> index to line }
    val unresolved = indexedLines.filter { (_, line) -> line.articuloId == null && line.articuloNombre == null }
    val resolved = indexedLines.filter { (_, line) -> line.articuloId != null || line.articuloNombre != null }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (unresolved.isEmpty()) "✅ Todo matcheado" else "⚠️ ${unresolved.size} productos sin matchear",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unresolved.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (uiState.debugLog.isNotEmpty()) {
            item { DebugLogCard(uiState.debugLog) }
        }

        item {
            SectionHeader(
                title = "Sin machear",
                subtitle = if (unresolved.isEmpty()) "Nada pendiente" else "Resuelve estos productos antes de guardar si quieres dejarlos finos"
            )
        }

        if (unresolved.isEmpty()) {
            item { InfoPill("No hay productos pendientes de macheo") }
        } else {
            items(unresolved.size) { pos ->
                val (index, line) = unresolved[pos]
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

        item {
            SectionHeader(
                title = "Ya macheados",
                subtitle = if (resolved.isEmpty()) "Todavía no hay productos resueltos" else "Productos resueltos listos para guardarse en el ticket"
            )
        }

        if (resolved.isEmpty()) {
            item { InfoPill("Todavía no hay productos macheados") }
        } else {
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((((resolved.size + 1) / 2) * 168).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(resolved.size) { pos ->
                        val (_, line) = resolved[pos]
                        MatchedTicketLineRow(line)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(onClick = onSave, modifier = Modifier.weight(1f), enabled = !uiState.isSaving) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar ticket")
                    }
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
        AddEditArticuloDialog(
            articulo = null,
            ean = null,
            selectedImageUri = null,
            categories = categories,
            prefillName = line.nombreOriginal,
            prefillQuantity = line.cantidad.toString(),
            prefillCategoryId = line.categoriaId?.toString(),
            prefillPrice = line.precioUnitario.toString(),
            onDismiss = { showCreateDialog = false },
            onSave = { articulo ->
                onCreateArticulo(articulo.name, articulo.categoryId.takeIf { it != 0L })
                showCreateDialog = false
            },
            onScanBarcode = {},
            onSelectImage = {}
        )
    }

}

@Composable
private fun CompleteStep(
    modifier: Modifier,
    ticketId: Long?,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
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
        Button(
            onClick = onNavigateToHistory,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text("Ir al historial")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onNavigateToHome,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text("Volver al inicio")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onImportAnother) { Text("Importar otro ticket") }
    }
}

