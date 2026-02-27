package com.jose.listacompra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAislesDialog(
    aisles: List<Aisle>,
    onDismiss: () -> Unit,
    onAddAisle: (name: String, emoji: String) -> Unit,
    onDeleteAisle: (Aisle) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newAisleName by remember { mutableStateOf("") }
    var newAisleEmoji by remember { mutableStateOf("") }
    
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
                
                // Lista de pasillos
                Text(
                    text = "Pasillos actuales (${aisles.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(aisles) { aisle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${aisle.emoji} ${aisle.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
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
