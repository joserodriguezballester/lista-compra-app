package com.jose.listacompra.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Producto detectado en un ticket PDF
 */
data class TicketProduct(
    val name: String,
    val price: Float,
    val quantity: Int = 1,
    val isSelected: Boolean = true,
    val rawText: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTicketScreen(
    onProductsImported: (List<TicketProduct>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var detectedProducts by remember { mutableStateOf<List<TicketProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Launcher para seleccionar PDF
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            fileName = getFileName(context, it) ?: "ticket.pdf"
            isLoading = true
            errorMessage = null
            
            // Parsear PDF
            try {
                val products = parsePdfTicket(context, it)
                detectedProducts = products
                if (products.isEmpty()) {
                    errorMessage = "No se detectaron productos en el PDF. Asegúrate de que es un ticket de Carrefour."
                }
            } catch (e: Exception) {
                errorMessage = "Error al leer el PDF: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📄 Importar Ticket") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (detectedProducts.isNotEmpty()) {
                        val selectedCount = detectedProducts.count { it.isSelected }
                        if (selectedCount > 0) {
                            TextButton(
                                onClick = {
                                    onProductsImported(detectedProducts.filter { it.isSelected })
                                }
                            ) {
                                Text("Añadir ($selectedCount)")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón para seleccionar PDF
            if (selectedFileUri == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { pdfLauncher.launch("application/pdf") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Toca para seleccionar PDF",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Busca el ticket en Descargas, Gmail o WhatsApp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                // Archivo seleccionado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1
                            )
                            Text(
                                text = "${detectedProducts.size} productos detectados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(
                            onClick = {
                                selectedFileUri = null
                                detectedProducts = emptyList()
                                fileName = ""
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cambiar archivo")
                        }
                    }
                }
            }
            
            // Mensaje de error
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Loading
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Lista de productos detectados
            if (detectedProducts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Productos detectados:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    TextButton(
                        onClick = {
                            val allSelected = detectedProducts.all { it.isSelected }
                            detectedProducts = detectedProducts.map { 
                                it.copy(isSelected = !allSelected) 
                            }
                        }
                    ) {
                        Text(
                            if (detectedProducts.all { it.isSelected }) "Desmarcar todos"
                            else "Seleccionar todos"
                        )
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detectedProducts) { product ->
                        TicketProductCard(
                            product = product,
                            onToggleSelection = {
                                detectedProducts = detectedProducts.map {
                                    if (it.name == product.name) it.copy(isSelected = !it.isSelected)
                                    else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketProductCard(
    product: TicketProduct,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = product.isSelected,
                    onCheckedChange = { onToggleSelection() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (product.quantity > 1) {
                        Text(
                            text = "Cantidad: ${product.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Text(
                text = "${String.format("%.2f", product.price)}€",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Obtiene el nombre del archivo desde una URI
 */
private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

/**
 * Parsea un PDF de ticket y extrae productos
 * NOTA: Esta es una implementación básica. Los tickets de Carrefour
 * tienen formato específico que requeriría parsing más avanzado.
 */
private fun parsePdfTicket(context: Context, uri: Uri): List<TicketProduct> {
    val products = mutableListOf<TicketProduct>()
    
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            // Leer el PDF como texto plano (funciona con PDFs de texto, no escaneados)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            
            // Patrones comunes en tickets de Carrefour
            // Buscamos líneas con formato: NOMBRE PRODUCTO + PRECIO
            val lines = content.split("\n", "\r")
            
            for (line in lines) {
                val trimmed = line.trim()
                
                // Ignorar líneas vacías o muy cortas
                if (trimmed.length < 3) continue
                
                // Ignorar líneas de cabecera/pie
                if (trimmed.contains("CARREFOUR", ignoreCase = true)) continue
                if (trimmed.contains("SUBTOTAL", ignoreCase = true)) continue
                if (trimmed.contains("TOTAL", ignoreCase = true)) continue
                if (trimmed.contains("DTO", ignoreCase = true)) continue
                if (trimmed.contains("DESCUENTO", ignoreCase = true)) continue
                if (trimmed.contains("==", ignoreCase = true)) continue
                if (trimmed.contains("NRF:", ignoreCase = true)) continue
                if (trimmed.contains("VENTA", ignoreCase = true)) continue
                if (trimmed.contains("TARJETA", ignoreCase = true)) continue
                
                // Intentar detectar precio al final de la línea
                // Formatos comunes: "PRODUCTO 1,99" o "PRODUCTO 1.99"
                val priceRegex = Regex("([\\d,]+)\\s*€?\$")
                val priceMatch = priceRegex.find(trimmed)
                
                if (priceMatch != null) {
                    val priceStr = priceMatch.groupValues[1].replace(',', '.')
                    val price = priceStr.toFloatOrNull()
                    
                    if (price != null && price > 0 && price < 1000) {
                        // Extraer nombre quitando el precio
                        val name = trimmed.substring(0, priceMatch.range.first).trim()
                            .replace(Regex("\\d+\\s*x\\s*\\(?"), "") // Quitar cantidades como "2 x"
                            .replace(Regex("\\(?\\d+\\s*,\\s*\\d+\\)?"), "") // Quitar (1,99)
                            .trim()
                        
                        if (name.length > 2 && !name.matches(Regex("\\d+"))) {
                            products.add(TicketProduct(
                                name = name,
                                price = price,
                                rawText = trimmed
                            ))
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    return products
}
