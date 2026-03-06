# Pasos a Seguir 5 - Arreglar Drag & Drop de Pasillos

## Problema
El drag & drop en `ManageAislesDialog.kt` no responde (no hace nada al mantener pulsado y arrastrar).

## Causas Comunes y Soluciones

---

## 1. Verificar que `detectDragGesturesAfterLongPress` está bien implementado

El problema suele ser que los índices no se actualizan correctamente o el `itemHeight` está mal calculado.

### Código corregido para ManageAislesDialog.kt

```kotlin
@Composable
fun ManageAislesDialog(
    aisles: List<Aisle>,
    onDismiss: () -> Unit,
    onAddAisle: (String) -> Unit,
    onDeleteAisle: (Aisle) -> Unit,
    onReorderAisles: (List<Aisle>) -> Unit
) {
    var localAisles by remember(aisles) { mutableStateOf(aisles.sortedBy { it.orderIndex }) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    
    // Para calcular la posición Y correctamente
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar Pasillos") },
        text = {
            Column {
                // ... resto del UI ...
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(
                        items = localAisles,
                        key = { _, aisle -> aisle.id }
                    ) { index, aisle ->
                        DraggableAisleItem(
                            aisle = aisle,
                            index = index,
                            isDragging = draggedItemIndex == index,
                            isTarget = targetIndex == index && draggedItemIndex != index,
                            onDragStart = { draggedItemIndex = index },
                            onDragEnd = {
                                val from = draggedItemIndex
                                val to = targetIndex
                                
                                if (from != null && to != null && from != to) {
                                    val newList = localAisles.toMutableList()
                                    val item = newList.removeAt(from)
                                    newList.add(to, item)
                                    
                                    // Actualizar orderIndex
                                    localAisles = newList.mapIndexed { i, a ->
                                        a.copy(orderIndex = i)
                                    }
                                    
                                    // Persistir cambios
                                    onReorderAisles(localAisles)
                                }
                                
                                draggedItemIndex = null
                                targetIndex = null
                            },
                            onDragCancel = {
                                draggedItemIndex = null
                                targetIndex = null
                            },
                            onMove = { from, to ->
                                if (from != to) {
                                    targetIndex = to
                                }
                            },
                            onDelete = { onDeleteAisle(aisle) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun DraggableAisleItem(
    aisle: Aisle,
    index: Int,
    isDragging: Boolean,
    isTarget: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onDelete: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else if (isTarget) 4.dp else 1.dp,
        label = "elevation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isDragging -> MaterialTheme.colorScheme.primaryContainer
            isTarget -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "background"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .zIndex(if (isDragging) 1f else 0f)
            .pointerInput(index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { 
                        onDragStart()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        // Calcular nueva posición basada en arrastre vertical
                        val itemHeight = 60.dp.toPx()
                        val dragOffset = dragAmount.y
                        
                        // Solo actualizar si se movió significativamente
                        val itemsMoved = (dragOffset / itemHeight).toInt()
                        
                        if (itemsMoved != 0) {
                            val newIndex = (index + itemsMoved).coerceIn(0, Int.MAX_VALUE)
                            onMove(index, newIndex)
                        }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de drag
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Arrastrar",
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Emoji y nombre
            Text(
                text = "${aisle.emoji} ${aisle.name}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Indicador de orden
            Text(
                text = "#${aisle.orderIndex}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            // Botón eliminar (solo si no es default)
            if (!aisle.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

---

## 2. Alternativa: Usar librería androidx.compose.foundation.lazy.dragAndDrop

Si la solución manual da problemas, usa la API experimental de Compose:

```kotlin
// build.gradle
implementation "androidx.compose.foundation:foundation:1.6.0"
```

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableAisleList(
    aisles: List<Aisle>,
    onReorder: (List<Aisle>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Guardar estado de arrastre