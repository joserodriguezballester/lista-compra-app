package com.jose.listacompra.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.rememberLazyListState

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
    
    // Estado local para la lista reordenable - ordenado por orderIndex
    var localAisles by remember(aisles) { 
        mutableStateOf(aisles.sortedBy { it.orderIndex }.mapIndexed { index, aisle ->
            // Actualizar orderIndex para que sea consecutivo
            aisle.copy(orderIndex = index)
        })
    }
    
    // Estado para el item que se está arrastrando
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragIndex by remember { mutableStateOf<Int?>(null) }
    
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
                    text = "Mantén pulsado y arrastra para reordenar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(localAisles, key = { it.id }) { aisle ->
                        val index = localAisles.indexOf(aisle)
                        val isDragging = draggedItemIndex == index
                        val isTarget = currentDragIndex == index && draggedItemIndex != index
                        
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else if (isTarget) 4.dp else 0.dp,
                            label = "elevation"
                        )
                        
                        val backgroundColor = when {
                            isDragging -> MaterialTheme.colorScheme.primaryContainer
                            isTarget -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .shadow(
                                    elevation = elevation,
                                    shape = MaterialTheme.shapes.small
                                )
                                .zIndex(if (isDragging) 100f else 1f)
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedItemIndex = index
                                            currentDragIndex = index
                                        },
                                        onDragEnd = {
                                            // Persistir el nuevo orden
                                            draggedItemIndex?.let { from ->
                                                currentDragIndex?.let { to ->
                                                    if (from != to) {
                                                        val newList = localAisles.toMutableList()
                                                        val item = newList.removeAt(from)
                                                        newList.add(to, item)
                                                        localAisles = newList.mapIndexed { i, a -> 
                                                            a.copy(orderIndex = i) 
                                                        }
                                                    }
                                                    // Siempre guardar el orden actual
                                                    onReorderAisles(localAisles)
                                                }
                                            }
                                            draggedItemIndex = null
                                            currentDragIndex = null
                                        },
                                        onDragCancel = {
                                            draggedItemIndex = null
                                            currentDragIndex = null
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            
                                            // Calcular nueva posición basada en el arrastre
                                            val itemHeight = 60f // aproximado
                                            val dragOffset = dragAmount.y
                                            val currentIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                                            
                                            val newIndex = when {
                                                dragOffset < -itemHeight && currentIndex > 0 -> currentIndex - 1
                                                dragOffset > itemHeight && currentIndex < localAisles.size - 1 -> currentIndex + 1
                                                else -> currentIndex
                                            }
                                            
                                            if (newIndex != currentDragIndex) {
                                                // Reordenar visualmente
                                                val newList = localAisles.toMutableList()
                                                val item = newList.removeAt(currentIndex)
                                                newList.add(newIndex, item)
                                                localAisles = newList
                                                draggedItemIndex = newIndex
                                                currentDragIndex = newIndex
                                            }
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = backgroundColor
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
                                        text = "#${index + 1}",
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
