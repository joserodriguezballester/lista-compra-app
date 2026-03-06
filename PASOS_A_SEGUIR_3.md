# Pasos a Seguir 3 - UI de Fotos y Tarjetas

## Objetivo
Mostrar imágenes de productos de forma visual y atractiva: en la selección al añadir producto, y en las tarjetas de la lista.

---

## 1. Diseño de Tarjeta con Imagen Superior

```kotlin
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ========== IMAGEN SUPERIOR ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (product.hasPhoto()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.photoUri)
                            .crossfade(300)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder con emoji grande
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.getDefaultEmoji(),
                            fontSize = 48.sp
                        )
                    }
                }
                
                // Badge de cantidad (esquina superior derecha)
                QuantityBadge(
                    quantity = product.quantity,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            
            // ========== INFO INFERIOR ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Categoría o Pasillo
                    Text(
                        text = product.categoryName ?: "Sin categoría",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    // Precio si existe
                    product.estimatedPrice?.let { price ->
                        Text(
                            text = "%.2f €".format(price * product.quantity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Checkbox para marcar comprado
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}

@Composable
fun QuantityBadge(quantity: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "×%.1f".format(quantity).replace(".0", ""),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
```

---

## 2. Pantalla "Añadir Producto" con Preview de Imagen

```kotlin
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onNavigateBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableFloatStateOf(1f) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedPhotoUri = uri
    }
    
    Scaffold(
        topBar = { /* ... */ },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveProduct(
                        name = productName,
                        quantity = quantity,
                        categoryId = selectedCategory?.id,
                        photoUri = selectedPhotoUri
                    )
                    onNavigateBack()
                }
            ) {
                Icon(Icons.Default.Save, "Guardar")
            }
        }
    ) { padding ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            
            // ========== PREVIEW DE IMAGEN GRANDE ==========
            ProductPhotoPreview(
                photoUri = selectedPhotoUri,
                categoryId = selectedCategory?.id,
                productName = productName,
                onClick = { photoLauncher.launch(PickVisualMediaRequest()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            )
            
            // Botones de acción de foto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = { photoLauncher.launch(PickVisualMediaRequest()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }
                
                Spacer(Modifier.width(12.dp))
                
                OutlinedButton(
                    onClick = { /* Abrir cámara */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }
                
                if (selectedPhotoUri != null) {
                    Spacer(Modifier.width(12.dp))
                    IconButton(
                        onClick = { selectedPhotoUri = null },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar foto")
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // ==========