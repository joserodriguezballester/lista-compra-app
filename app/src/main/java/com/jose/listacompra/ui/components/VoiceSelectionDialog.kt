package com.jose.listacompra.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jose.listacompra.domain.model.Articulo

/**
 * Diálogo para seleccionar entre múltiples coincidencias de voz
 */
@Composable
fun VoiceSelectionDialog(
    matches: List<Articulo>,
    quantity: Float,
    onConfirm: (Articulo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf<Articulo?>(matches.firstOrNull()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona el producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cantidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cantidad:", style = MaterialTheme.typography.bodyMedium)
                    Text("$quantity uds", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                HorizontalDivider()
                
                // Lista de opciones
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(matches) { articulo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = articulo },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected == articulo)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Imagen si tiene
                                articulo.photoUri?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = articulo.name,
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(articulo.name, style = MaterialTheme.typography.bodyLarge)
                                    articulo.finalPrice?.let { price ->
                                        Text("€${String.format("%.2f", price)}", 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                if (selected == articulo) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selected?.let(onConfirm) },
                enabled = selected != null
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
