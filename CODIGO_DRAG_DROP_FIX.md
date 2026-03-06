# Fix para Drag & Drop de Pasillos

## Problema
El índice `draggedItemIndex` se corrompe durante el arrastre porque se modifica `localAisles` en medio del drag.

## Solución
Separar la lógica: calcular índice objetivo SIN modificar la lista hasta `onDragEnd`.

## Código Reemplazado (ManageAislesDialog.kt)

Reemplazar TODA la sección del `LazyColumn` (aprox líneas 115-200):

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxWidth()
) {
    items(localAisles, key = { it.id }) { aisle ->
        val index = localAisles.indexOf(aisle)
        val isDragging = draggedItemIndex == index
        val isTarget = currentDragIndex == index && draggedItemIndex != index
        
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
        
        // Calculamos "from" y "to" para animación visual
        val isBeingDraggedFrom = draggedItemIndex == index
        val isBeingDraggedTo = currentDragIndex == index
        
        val offsetY by animateDpAsState(
            targetValue = when {
                draggedItemIndex == null -> 0.dp
                index == draggedItemIndex -> 0.dp // El dragged no tiene offset
                // Items entre dragged y target se desplazan
                draggedItemIndex != null && currentDragIndex != null -> {
                    val from = draggedItemIndex!!
                    val to = currentDragIndex!!
                    if ((from < to && index in (from+1..to)) || 
                        (from > to && index in (to..from-1))) {
                        if (from < to) (-60).dp else 60.dp
                    } else 0.dp
                }
                else -> 0.dp
            },
            label = "offset"
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .offset(y = offsetY)
                .shadow(elevation, shape = MaterialTheme.shapes.small)
                .zIndex(if (isDragging) 10f else 1f)
                .pointerInput(aisle.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { 
                            draggedItemIndex = index
                            currentDragIndex = index
                        },
                        onDragEnd = {
                            val from = draggedItemIndex
                            val to = currentDragIndex
                            
                            if (from != null && to != null && from != to) {
                                // REORDENAR AL SOLTAR
                                val newList = localAisles.toMutableList()
                                val item = newList.removeAt(from)
                                newList.add(to, item)
                                
                                // Actualizar orderIndex
                                localAisles = newList.mapIndexed { i, a ->
                                    a.copy(orderIndex = i)
                                }
                                
                                // Persistir
                                onReorderAisles(localAisles)
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
                            
                            val itemHeight = 60.dp.toPx()
                            val dragOffset = dragAmount.y
                            
                            // Solo actualizar índice visual objetivo
                            val currentDragIdx = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                            val itemsMoved = (dragOffset / itemHeight).toInt()
                            
                            if (itemsMoved != 0) {
                                val newTarget = (currentDragIdx + itemsMoved)
                                    .coerceIn(0, localAisles.size - 1)
                                if (newTarget != currentDragIndex) {
                                    currentDragIndex = newTarget
                                }
                            }
                        }
                    )
                },
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
                
                // Emoji + Nombre
                Text(
                    text = "${aisle.emoji} ${aisle.name}",
                    modifier = Modifier.weight(1f)
                )
                
                // Indicador de orden
                Text(
                    text = "#${aisle.orderIndex}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                // Botón eliminar
                if (!aisle.isDefault) {
                    IconButton(onClick = { onDeleteAisle(aisle) }) {
                        Icon(
                            Icons.Default.Delete,
                            "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
```

## Cambios Clave

1. **NO modificar `localAisles` en `onDrag`** - Solo calcular `currentDragIndex`
2. **Offset visual** con `animateDpAsState` - Items se desplazan visualmente sin moverse en la lista
3. **Reordenar SOLO en `onDragEnd`** - Persistir al soltar
4. **`pointerInput(aisle.id)`** en lugar de `pointerInput(Unit)` - Evita conflictos

## También Añadir Import

```kotlin
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
```

Si falla la detección, prueba con la alternativa:
- Usar `LazyColumn` con `dragAndDrop` experimental de Compose Foundation
- Usar librería externa como `androidx.compose.foundation:foundation:1.7.0-alpha` (que tiene dragAndDrop oficial)