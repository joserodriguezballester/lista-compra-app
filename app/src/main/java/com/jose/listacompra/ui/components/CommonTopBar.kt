package com.jose.listacompra.ui.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class ReleaseInfo(
    val tagName: String,
    val name: String,
    val publishedAt: String,
    val htmlUrl: String,
    val apkUrl: String?,
    val apkSize: Long
)

suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
    try {
        val url = java.net.URL("https://api.github.com/repos/joserodriguezballester/lista-compra-app/releases?per_page=10")
        val connection = url.openConnection()
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val response = connection.getInputStream().bufferedReader().readText()
        val releases = JSONArray(response)

        var selectedRelease: JSONObject? = null
        var selectedApkUrl: String? = null
        var selectedApkSize = 0L

        for (i in 0 until releases.length()) {
            val release = releases.getJSONObject(i)
            if (release.optBoolean("draft", false)) continue

            val assets = release.optJSONArray("assets") ?: continue
            for (j in 0 until assets.length()) {
                val asset = assets.getJSONObject(j)
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                if (name.endsWith(".apk", ignoreCase = true) && url.isNotBlank()) {
                    selectedRelease = release
                    selectedApkUrl = url
                    selectedApkSize = asset.optLong("size", 0)
                    break
                }
            }
            if (selectedRelease != null) break
        }

        selectedRelease?.let { json ->
            ReleaseInfo(
                tagName = json.getString("tag_name"),
                name = json.optString("name", json.getString("tag_name")),
                publishedAt = json.optString("published_at", ""),
                htmlUrl = json.getString("html_url"),
                apkUrl = selectedApkUrl,
                apkSize = selectedApkSize
            )
        }
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
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0L) }
    
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
    
    // Formatear tamaño de archivo
    val apkSizeFormatted = remember(latestRelease?.apkSize) {
        val size = latestRelease?.apkSize ?: 0
        if (size > 1024 * 1024) {
            "%.1f MB".format(size / (1024.0 * 1024.0))
        } else {
            "%.0f KB".format(size / 1024.0)
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
                                Text("Último: ${release.tagName} ($apkSizeFormatted)")
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
                    
                    // Botón de actualización
                    if (hasUpdate && latestRelease?.apkUrl != null) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    if (isDownloading) "Descargando..." else "⬇️ Actualizar a ${latestRelease?.tagName}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                if (!isDownloading) {
                                    isDownloading = true
                                    downloadAndInstallApk(
                                        context = context,
                                        apkUrl = latestRelease!!.apkUrl!!,
                                        versionName = latestRelease!!.tagName,
                                        onProgress = { progress -> downloadProgress = progress },
                                        onComplete = { isDownloading = false }
                                    )
                                }
                                showMenu = false
                            },
                            enabled = !isDownloading,
                            leadingIcon = {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null)
                                }
                            }
                        )
                    } else {
                        // Sin actualización o sin APK
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
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

fun downloadAndInstallApk(
    context: Context,
    apkUrl: String,
    versionName: String,
    onProgress: (Long) -> Unit,
    onComplete: () -> Unit
) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    
    // Nombre del archivo
    val fileName = "lista-compra-$versionName.apk"
    
    // Eliminar descarga anterior si existe
    val query = DownloadManager.Query()
    query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
    val cursor = downloadManager.query(query)
    while (cursor.moveToNext()) {
        val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
        if (localUri?.contains("lista-compra-") == true) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
            downloadManager.remove(id)
        }
    }
    cursor.close()
    
    // Crear solicitud de descarga
    val request = DownloadManager.Request(Uri.parse(apkUrl))
        .setTitle("Actualizando Lista Compra")
        .setDescription("Descargando $versionName...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
    
    // Iniciar descarga
    val downloadId = downloadManager.enqueue(request)
    
    // Registrar receiver para cuando termine
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                onComplete()
                
                // Obtener URI del archivo descargado
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    cursor.close()
                    
                    // Abrir el APK para instalación
                    val apkUri = Uri.parse(localUri)
                    val apkFile = File(apkUri.path ?: return)
                    
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            // Android 7+ necesita FileProvider
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                apkFile
                            )
                            setDataAndType(contentUri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } else {
                            setDataAndType(apkUri, "application/vnd.android.package-archive")
                        }
                    }
                    
                    context.startActivity(installIntent)
                }
                
                // Desregistrar receiver
                context.unregisterReceiver(this)
            }
        }
    }
    
    // Registrar receiver
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
    } else {
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}
