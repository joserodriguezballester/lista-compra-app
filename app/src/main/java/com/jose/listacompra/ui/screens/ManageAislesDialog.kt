package com.jose.listacompra.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jose.listacompra.domain.model.Aisle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAislesDialog(
    aisles: List<Aisle>,
    onDismiss: () -> Unit,
    onAddAisle: (name: String, emoji: String) -> Unit,
    onDeleteAisle: (Aisle) -> Unit,
    onReorderAisles: (List<Aisle>) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newAisleName by remember { mutableStateOf("") }
    var newAisleEmoji by remember { mutableStateOf("") }
    
    // Estado local para la lista reordenable
    var localAisles by remember(aisles) { mutableStateOf(aisles.sortedBy { it.orderIndex }) }
    
    // Estado para el item que se está arrastrando
    var draggedItemId by remember { mutableStateOf<Long?>(null) }
    var draggedItemIndex by remember { mutableStateOf(-1) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar pasillos") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (showAddForm) {
                    // Formulario para añadir pasillo
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAisleName,
                            onValueChange = { newAisleName = it },
                            label = { Text("Nombre del pasillo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = newAisleEmoji,
                            onValueChange = { newAisleEmoji = it },
                            label = { Text("Emoji (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { showAddForm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }
                            Button(
                                onClick = {
                                    if (newAisleName.isNotBlank()) {
                                        onAddAisle(newAisleName, newAisleEmoji)
                                        newAisleName = ""
                                        newAisleEmoji = ""
                                        showAddForm = false
                                    }
                                },
                                enabled = newAisleName.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Añadir")
                            }
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
                
                // Lista de pasillos con drag & drop
                Text(
                    text = "Pasillos actuales (${localAisles.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Arrastra los items para reordenar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(localAisles, key = { it.id }) { aisle ->
                        val isDragging = draggedItemId == aisle.id
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "elevation"
                        )
                        val animatedPadding by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "padding"
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .shadow(
                                    elevation = elevation,
                                    shape = MaterialTheme.shapes.small
                                )
                                .zIndex(if (isDragging) 100f else 1f)
                                .pointerInput(aisle.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggedItemId = aisle.id
                                            draggedItemIndex = localAisles.indexOf(aisle)
                                        },
                                        onDragEnd = {
                                            // Persistir el nuevo orden
                                            if (draggedItemId != null) {
                                                onReorderAisles(localAisles)
                                            }
                                            draggedItemId = null
                                            draggedItemIndex = -1
                                        },
                                        onDragCancel = {
                                            draggedItemId = null
                                            draggedItemIndex = -1
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            
                                            // Calcular la nueva posición basada en el arrastre vertical
                                            val threshold = 60f // píxeles para cambiar de posición
                                            val currentIndex = localAisles.indexOfFirst { it.id == aisle.id }
                                            
                                            if (currentIndex != -1) {
                                                when {
                                                    dragAmount.y < -threshold && currentIndex > 0 -> {
                                                        // Mover arriba
                                                        val newList = localAisles.toMutableList()
                                                        val item = newList.removeAt(currentIndex)
                                                        newList.add(currentIndex - 1, item)
                                                        localAisles = newList
                                                        draggedItemIndex = currentIndex - 1
                                                    }
                                                    dragAmount.y > threshold && currentIndex < localAisles.size - 1 -> {
                                                        // Mover abajo
                                                        val newList = localAisles.toMutableList()
                                                        val item = newList.removeAt(currentIndex)
                                                        newList.add(currentIndex + 1, item)
                                                        localAisles = newList
                                                        draggedItemIndex = currentIndex + 1
                                                    }
                                                }
                                            }
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDragging) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Icono de drag handle
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Arrastrar para reordenar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    
                                    Text(
                                        text = "${aisle.emoji} ${aisle.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Indicador de posición
                                    Text(
                                        text = "#${localAisles.indexOf(aisle) + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Solo mostrar eliminar si no es pasillo por defecto
                                if (!aisle.isDefault) {
                                    IconButton(
                                        onClick = { onDeleteAisle(aisle) }
                                    ) {
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
                }
            }
        },
        confirmButton = {
            if (!showAddForm) {
                Button(onClick = { showAddForm = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo pasillo")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
