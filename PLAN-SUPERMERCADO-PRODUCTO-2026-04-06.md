# Análisis: Selección de Supermercado por Producto

## 📊 ESTADO ACTUAL

### ✅ Lo que YA existe:

| Componente | Estado | Descripción |
|------------|--------|-------------|
| `ProductEntity.supermarketId` | ✅ | Cada producto ya tiene supermercado asignado |
| `ProductDao.getProductsBySupermarketFlow()` | ✅ | Query para filtrar por supermercado |
| `GetProductsByListUseCase` | ✅ | Ya filtra si `supermarketId > 0` |
| `SupermarketBottomBar` | ✅ | Tabs de supermercados en Mi Lista |
| `ProductListViewModel.selectSupermarket()` | ✅ | Cambia de supermercado |

---

## ❌ Lo que FALTA

### 1. Supermercado "Cualquiera"

**Problema:** No existe un supermercado con id=0 llamado "Cualquiera"

**Solución:**
- Añadir supermercado "Cualquiera" con `id = 0` en `InitialDataSeeder`
- Los productos con `supermarketId = 0` son para cualquier supermercado
- Ejemplo: "Pan", "Leche" que compras en cualquier sitio

---

### 2. Opción "Todos" en la Bottom Bar

**Problema:** Solo muestra supermercados individuales

**Solución:**
- Añadir tab "📦 Todos" al principio de `SupermarketBottomBar`
- Cuando se selecciona "Todos", no filtrar por supermercado
- Mostrar TODOS los productos de la lista

---

### 3. Filtrado inteligente

**Problema:** Al seleccionar supermercado X, solo muestra productos de X

**Solución:**
```
Al seleccionar Supermercado X → Mostrar:
  - Productos con supermarketId = X
  - Productos con supermarketId = 0 (Cualquiera)

Al seleccionar "Todos" → Mostrar:
  - Todos los productos sin filtrar
```

---

### 4. Selector de supermercado al añadir/editar

**Problema:** No se puede elegir el supermercado al añadir producto

**Solución:**
- Añadir dropdown de supermercado en `AddProductToListDialog`
- Añadir dropdown de supermercado en `EditProductDialog`
- Valor por defecto: supermercado seleccionado en la bottom bar
- Opción "Cualquiera" disponible

---

## 🔧 IMPLEMENTACIÓN

### Paso 1: Crear supermercado "Cualquiera"

**Archivo:** `InitialDataSeeder.kt`

```kotlin
private val defaultSupermarkets = listOf(
    Supermarket(id = 0, name = "Cualquiera", emoji = "📦", isDefault = false),
    Supermarket(id = 1, name = "Carrefour", emoji = "🔵", isDefault = true),
    Supermarket(id = 2, name = "Mercadona", emoji = "🟡", isDefault = false),
    Supermarket(id = 3, name = "Lidl", emoji = "🟢", isDefault = false),
    Supermarket(id = 4, name = "Dia", emoji = "🟠", isDefault = false),
    Supermarket(id = 5, name = "Aldi", emoji = "🔷", isDefault = false),
    Supermarket(id = 6, name = "Consum", emoji = "🟤", isDefault = false)
)
```

---

### Paso 2: Modificar `GetProductsByListUseCase`

**Archivo:** `GetProductsByListUseCase.kt`

```kotlin
operator fun invoke(listId: Long, supermarketId: Long? = null, showAll: Boolean = false): Flow<List<Product>> {
    return when {
        showAll -> productRepository.getProductsByListFlow(listId)
        supermarketId != null && supermarketId > 0 -> {
            // Mostrar productos del supermercado + productos "Cualquiera" (id=0)
            productRepository.getProductsBySupermarketOrAny(listId, supermarketId)
        }
        else -> productRepository.getProductsByListFlow(listId)
    }
}
```

---

### Paso 3: Añadir método al DAO

**Archivo:** `ProductDao.kt`

```kotlin
@Query("""
    SELECT * FROM products 
    WHERE shoppingListId = :listId 
    AND (supermarketId = :supermarketId OR supermarketId = 0)
    ORDER BY aisleId ASC, orderIndex ASC
""")
fun getProductsBySupermarketOrAnyFlow(listId: Long, supermarketId: Long): Flow<List<ProductEntity>>
```

---

### Paso 4: Modificar `SupermarketBottomBar`

**Archivo:** `SupermarketBottomBar.kt`

```kotlin
@Composable
fun SupermarketBottomBar(
    supermarkets: List<Supermarket>,
    selectedSupermarketId: Long?,
    onSupermarketSelected: (Long?) -> Unit,  // Ahora nullable
    showAllOption: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Añadir opción "Todos" al principio
    val allOption = Supermarket(id = -1, name = "Todos", emoji = "📦", isDefault = false)
    val displaySupermarkets = if (showAllOption) {
        listOf(allOption) + supermarkets
    } else {
        supermarkets
    }
    
    // ... resto del código
}
```

---

### Paso 5: Añadir selector en `AddProductToListDialog`

**Archivo:** `AddProductToListDialog.kt`

```kotlin
// Añadir dropdown de supermercado
var selectedSupermarketId by remember { mutableStateOf(preselectedSupermarketId) }

ExposedDropdownMenuBox(
    expanded = showSupermarketDropdown,
    onExpandedChange = { showSupermarketDropdown = it }
) {
    OutlinedTextField(
        value = supermarkets.find { it.id == selectedSupermarketId }?.let { "${it.emoji} ${it.name}" } ?: "Cualquiera",
        onValueChange = {},
        readOnly = true,
        label = { Text("Supermercado") },
        // ...
    )
    
    ExposedDropdownMenu(
        expanded = showSupermarketDropdown,
        onDismissRequest = { showSupermarketDropdown = false }
    ) {
        supermarkets.forEach { supermarket ->
            DropdownMenuItem(
                text = { Text("${supermarket.emoji} ${supermarket.name}") },
                onClick = {
                    selectedSupermarketId = supermarket.id
                    showSupermarketDropdown = false
                }
            )
        }
    }
}
```

---

### Paso 6: Modificar `ProductListViewModel`

**Archivo:** `ProductListViewModel.kt`

```kotlin
data class ProductListUiState(
    // ... campos existentes
    val showAllProducts: Boolean = false,  // Nueva flag
    val selectedSupermarketId: Long? = null  // Ahora nullable
)

fun selectSupermarket(supermarketId: Long?) {
    viewModelScope.launch {
        _uiState.update { 
            it.copy(
                selectedSupermarketId = supermarketId,
                showAllProducts = supermarketId == null || supermarketId == -1
            )
        }
        
        if (supermarketId == null || supermarketId == -1) {
            loadAllProducts()
        } else {
            loadAislesAndProducts(supermarketId)
        }
    }
}

private fun loadAllProducts() {
    viewModelScope.launch {
        getProductsByListUseCase(currentListId, showAll = true)
            .collect { productList ->
                // ... actualizar estado
            }
    }
}
```

---

## 📱 RESULTADO FINAL

### UI de la Bottom Bar:

```
┌──────────────────────────────────────────────────────┐
│  📦 Todos │ 📦 Cualquiera │ 🔵 Carrefour │ 🟡 Mercadona │ 🟢 Lidl │ ...  │
└──────────────────────────────────────────────────────┘
```

### Comportamiento:

| Selección | Productos mostrados |
|-----------|---------------------|
| **📦 Todos** | Todos los productos sin filtrar |
| **📦 Cualquiera** | Solo productos con `supermarketId = 0` |
| **🔵 Carrefour** | Productos de Carrefour + productos "Cualquiera" |
| **🟡 Mercadona** | Productos de Mercadona + productos "Cualquiera" |

### Al añadir producto:

```
┌─────────────────────────────────────┐
│ Añadir producto                     │
├─────────────────────────────────────┤
│ Nombre: [Leche entera      ]       │
│ Cantidad: [1            ]          │
│ Precio: [1.35          ]           │
│ Pasillo: [Lácteos ▼     ]          │
│ Supermercado: [Cualquiera ▼]       │  ← NUEVO
│   - 📦 Cualquiera                  │
│   - 🔵 Carrefour                   │
│   - 🟡 Mercadona                   │
│   - ...                            │
│ Oferta: [Sin oferta ▼   ]          │
└─────────────────────────────────────┘
```

---

## ⚠️ CONSIDERACIONES

### Migración de BD

**NO necesaria** porque:
- `ProductEntity.supermarketId` ya existe
- Solo añadimos un supermercado "Cualquiera" (id=0)
- El campo ya tiene valor por defecto `supermarketId = 1`

### Productos existentes

**Problema:** Los productos actuales tienen `supermarketId = 1` (Carrefour por defecto)

**Opción A:** Dejarlos así (correcto si se añadieron en contexto de Carrefour)

**Opción B:** Crear script de migración para cambiarlos a "Cualquiera" si el usuario quiere

---

## 🚀 ORDEN DE IMPLEMENTACIÓN

1. **Paso 1:** Crear supermercado "Cualquiera" en InitialDataSeeder
2. **Paso 2:** Añadir método `getProductsBySupermarketOrAnyFlow` al DAO
3. **Paso 3:** Añadir método `getProductsBySupermarketOrAny` al Repository
4. **Paso 4:** Modificar `GetProductsByListUseCase` con flag `showAll`
5. **Paso 5:** Modificar `ProductListViewModel` para manejar "Todos" y "Cualquiera"
6. **Paso 6:** Modificar `SupermarketBottomBar` con opción "Todos"
7. **Paso 7:** Añadir selector de supermercado en `AddProductToListDialog`
8. **Paso 8:** Añadir selector de supermercado en `EditProductDialog`

---

## ❓ PREGUNTAS PARA EL USUARIO

1. **¿Quieres que "Cualquiera" tenga id=0 o prefieres otro valor?**

2. **¿Los productos existentes (supermarketId=1) los dejamos en Carrefour o los movemos a "Cualquiera"?**

3. **¿Prefieres que "Todos" sea un tab más o un botón separado?**

4. **¿El dropdown de supermercado en añadir/editar debe estar siempre visible o colapsado?**
