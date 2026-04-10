package com.jose.listacompra.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * TopBar común con:
 * - Drawer (hamburguesa) o volver
 * - Título
 * - Micrófono (para añadir productos)
 * - Menú overflow (opciones específicas + ajustes)
 */
data class ReleaseInfo(
    val tagName: String,
    val name: String,
    val publishedAt: String,
    val htmlUrl: String
)

suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
    try {
        val url = java.net.URL("https://api.github.com/repos/joserodriguezballester/lista-compra-app/releases/latest")
        val connection = url.openConnection()
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        val response = connection.getInputStream().bufferedReader().readText()
        val json = JSONObject(response)
        
        ReleaseInfo(
            tagName = json.getString("tag_name"),
            name = json.optString("name", json.getString("tag_name")),
            publishedAt = json.optString("published_at", ""),
            htmlUrl = json.getString("html_url")
        )
    } catch (e: Exception) {
        null
    }
}

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
    showVersionInOverflow: Boolean = true,
    overflowActions: @Composable (Expanded: Boolean, onDismiss: () -> Unit) -> Unit = { _, _ -> }
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Versión instalada
    val installedVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "?"
        }
    }
    
    // Último release de GitHub
    var latestRelease by remember { mutableStateOf<ReleaseInfo?>(null) }
    LaunchedEffect(Unit) {
        latestRelease = fetchLatestRelease()
    }
    
    // Comparar versiones (formato vX.X.X)
    val hasUpdate = remember(installedVersion, latestRelease) {
        if (latestRelease == null || installedVersion == "?") false
        else {
            val installed = installedVersion.removePrefix("v")
            val latest = latestRelease!!.tagName.removePrefix("v")
            installed != latest
        }
    }

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
                
                // 📁 Ajustes
                DropdownMenuItem(
                    text = { Text("📁 Ajustes") },
                    onClick = { /* Header, no action */ },
                    enabled = false,
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                if (onToggleDarkMode != null) {
                    DropdownMenuItem(
                        text = { Text("    " + if (isDarkMode) "Modo claro" else "Modo oscuro") },
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
                    text = { Text("    Cambiar color") },
                    onClick = {
                        onChangeColor()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    }
                )
                
                // Versión y actualizar
                if (showVersionInOverflow) {
                    HorizontalDivider()
                    
                    // Info del último release
                    latestRelease?.let { release ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(
                                        text = "Último: ${release.tagName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (hasUpdate) {
                                        Text(
                                            text = "Actualización disponible",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = { showMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        )
                    }
                    
                    DropdownMenuItem(
                        text = { Text("Instalada: v$installedVersion") },
                        onClick = { showMenu = false },
                        leadingIcon = {
                            Icon(Icons.Default.Download, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Ver releases") },
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joserodriguezballester/lista-compra-app/releases"))
                            context.startActivity(intent)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
