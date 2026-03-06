# Código a Añadir en MainScreen.kt

## Ubicación
Buscar dentro del `TopAppBar` → `actions = {` ... `}`

## Código a Añadir

Después del cierre del `VoiceInputButton`, añadir:

```kotlin
            )
            
            // Botón VACIAR LISTA (visible si hay productos)
            if (uiState.totalCount > 0) {
                IconButton(
                    onClick = { viewModel.showEmptyListConfirmDialog() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Vaciar lista"
                    )
                }
            }
            
            // Botón de menú de opciones (EXISTENTE - no modificar)
```

## También añadir el Diálogo

Al final de la función `MainScreen`, antes del cierre final, añadir:

```kotlin
    // Diálogo de confirmación para vaciar lista
    if (uiState.showEmptyListConfirmDialog) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.dismissEmptyListConfirmDialog() 
            },
            icon = { 
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Vaciar lista?") },
            text = { 
                Text(
                    "Se eliminarán ${uiState.totalCount} productos de la lista actual."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.emptyCurrentList() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissEmptyListConfirmDialog() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
```

## Notas
- Asegúrate de tener `import androidx.compose.material.icons.automirrored.outlined` o cambiar a `Icons.Default.DeleteSweep`
- El ViewModel ya tiene las funciones (commit anterior)