package com.jose.listacompra.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight

/**
 * TopBar común con:
 * - Drawer (hamburguesa) o volver
 * - Título
 * - Micrófono (para añadir productos)
 * - Menú overflow (opciones específicas + ajustes)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: () -> Unit = {},
    onMicrophoneClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onChangeColor: () -> Unit = {},
    onToggleDarkMode: (() -> Unit)? = null,
    isDarkMode: Boolean = false,
    overflowActions: @Composable (Expanded: Boolean, onDismiss: () -> Unit) -> Unit = { _, _ -> }
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            } else {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }
            }
        },
        actions = {
            // Botón + (si está disponible)
            if (onAddClick != null) {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
            
            // Micrófono (si está disponible)
            if (onMicrophoneClick != null) {
                IconButton(onClick = onMicrophoneClick) {
                    Icon(Icons.Default.Mic, contentDescription = "Añadir por voz")
                }
            }
            
            // Menú overflow
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // Acciones específicas de cada pantalla
                overflowActions(showMenu) { showMenu = false }
                
                // Divider si hay acciones específicas
                HorizontalDivider()
                
                // Ajustes comunes
                if (onToggleDarkMode != null) {
                    DropdownMenuItem(
                        text = { Text(if (isDarkMode) "Modo claro" else "Modo oscuro") },
                        onClick = {
                            onToggleDarkMode()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null
                            )
                        }
                    )
                }
                
                DropdownMenuItem(
                    text = { Text("Cambiar color") },
                    onClick = {
                        onChangeColor()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
