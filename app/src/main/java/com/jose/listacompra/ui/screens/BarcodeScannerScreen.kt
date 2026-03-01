package com.jose.listacompra.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

/**
 * Datos de producto obtenidos del escaneo
 */
data class ScannedProduct(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val imageUrl: String?,
    val category: String?,
    val found: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onProductScanned: (ScannedProduct) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var scannedProduct by remember { mutableStateOf<ScannedProduct?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Solicitar permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            errorMessage = "Se necesita permiso de cámara para escanear códigos"
        }
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    // Procesar código escaneado
    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let { barcode ->
            isSearching = true
            scannedProduct = null
            
            // Buscar en Open Food Facts
            val product = searchProductInOpenFoodFacts(barcode)
            scannedProduct = product
            isSearching = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📷 Escanear Código") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Vista de cámara o mensaje de error
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Preview de cámara
                    CameraPreview(
                        onBarcodeDetected = { barcode ->
                            if (scannedBarcode != barcode && !isSearching) {
                                scannedBarcode = barcode
                            }
                        }
                    )
                    
                    // Overlay con instrucciones
                    if (scannedBarcode == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Enfoca el código de barras",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Se detectará automáticamente",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Sin permiso de cámara
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Se necesita permiso de cámara",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Resultado del escaneo
            if (isSearching) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Buscando producto...")
                    }
                }
            }
            
            scannedProduct?.let { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (product.found) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (product.found) "✅ Producto encontrado" else "⚠️ Producto no encontrado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Código: ${product.barcode}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        if (product.found) {
                            product.name?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            product.brand?.let {
                                Text(
                                    text = "Marca: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            product.category?.let {
                                Text(
                                    text = "Categoría: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { onProductScanned(product) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Añadir a la lista")
                            }
                        } else {
                            Text(
                                text = "No se encontró información. ¿Quieres añadirlo manualmente?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { 
                                    onProductScanned(
                                        ScannedProduct(
                                            barcode = product.barcode,
                                            name = "Producto ${product.barcode.takeLast(4)}",
                                            brand = null,
                                            imageUrl = null,
                                            category = null,
                                            found = false
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Añadir con código")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(
                            onClick = { 
                                scannedBarcode = null
                                scannedProduct = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Escanear otro")
                        }
                    }
                }
            }
            
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview de cámara con análisis de códigos de barras
 */
@Composable
private fun CameraPreview(
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Image analysis para detección de códigos
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(
                                barcodeScanner,
                                imageProxy,
                                onBarcodeDetected
                            )
                        }
                    }
                
                // Seleccionar cámara trasera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    // Desvincular todos los casos de uso
                    cameraProvider.unbindAll()
                    
                    // Vincular casos de uso a la cámara
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error binding camera", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }
}

/**
 * Procesa la imagen de la cámara para detectar códigos de barras
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        // Solo procesar códigos EAN (productos)
                        if (barcode.valueType == Barcode.TYPE_PRODUCT ||
                            value.length == 13 || value.length == 8) {
                            onBarcodeDetected(value)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeScanner", "Error procesando imagen", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

/**
 * Busca el producto en Open Food Facts
 */
private suspend fun searchProductInOpenFoodFacts(barcode: String): ScannedProduct {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "ListaCompraApp - Android")
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            
            if (json.getInt("status") == 1) {
                val product = json.getJSONObject("product")
                
                ScannedProduct(
                    barcode = barcode,
                    name = product.optString("product_name", null),
                    brand = product.optString("brands", null)?.split(",")?.firstOrNull(),
                    imageUrl = product.optString("image_url", null),
                    category = product.optJSONArray("categories_tags")?.let { tags ->
                        if (tags.length() > 0) tags.getString(0).replace("en:", "")
                        else null
                    },
                    found = true
                )
            } else {
                ScannedProduct(
                    barcode = barcode,
                    name = null,
                    brand = null,
                    imageUrl = null,
                    category = null,
                    found = false
                )
            }
        } catch (e: Exception) {
            Log.e("OpenFoodFacts", "Error buscando producto", e)
            ScannedProduct(
                barcode = barcode,
                name = null,
                brand = null,
                imageUrl = null,
                category = null,
                found = false
            )
        }
    }
}
