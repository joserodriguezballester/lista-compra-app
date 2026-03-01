# 📝 MEJORAS Y BUGS PENDIENTES - App Lista Compra

**Repo:** https://github.com/joserodriguezballester/lista-compra-app  
**Usuario:** Jose (Xoce)  
**Última actualización:** 2026-03-01

---

## 🐛 BUGS ACTIVOS (Prioridad Alta)

### 1. ProductHistoryScreen no asigna pasillo al seleccionar
**Problema:** Cuando se añade un producto desde la pantalla "Historial" (`ProductHistoryScreen.kt`), no se asigna el pasillo correcto.

**Causa:** 
- `ProductHistoryScreen` usa datos **mockeados** (hardcodeados) en lugar de la BD real
- El callback `onProductSelected: (HistoricalProduct) -> Unit` pasa un objeto con `aisle: String` (nombre del pasillo) pero **sin `aisleId`**
- El MainScreen recibe el producto pero no puede mapear el nombre del pasillo a su ID

**Código problemático (ProductHistoryScreen.kt línea 28):**
```kotlin
data class HistoricalProduct(
    val name: String,
    val price: Float,
    val aisle: String,  // ← Solo nombre, sin ID!
    val emoji: String,
    // ...
)
```

**Solución propuesta:**
1. Conectar `ProductHistoryScreen` a la BD real (usar `ProductHistoryDao`)
2. Cambiar `HistoricalProduct` para incluir `aisleId: Long`
3. En `MainScreen.kt`, buscar el pasillo por nombre si no se tiene ID, o pasar el ID directamente

---

### 2. Códigos de barras sin categoría/pasillo asignado
**Problema:** Al escanear un código de barras, el producto se añade sin pasillo ni categoría asignada.

**Causa:**
- `BarcodeScannerScreen.kt` obtiene datos de Open Food Facts (categoría genérica como "en:dairy")
- No hay mapeo de categorías Open Food Facts → pasillos del supermercado
- El producto se crea sin `aisleId`

**Código (BarcodeScannerScreen.kt):**
```kotlin
ScannedProduct(
    barcode = barcode,
    name = product.optString("product_name", null),
    brand = product.optString("brands", null),
    imageUrl = product.optString("image_url", null),
    category = product.optJSONArray("categories_tags")?.let { tags ->
        if (tags.length() > 0) tags.getString(0).replace("en:", "")  // ← Categoría genérica
        else null
    },
    found = true
)
```

**Solución propuesta:**
1. Crear tabla de mapeo `category_aisle_mapping` (categoría OpenFoodFacts → aisleId por defecto)
2. Permitir al usuario asignar pasillo manualmente tras escanear
3. Recordar la asignación para futuras compras del mismo producto

---

## ✨ MEJORAS PROPUESTAS

### Valorar: Sistema de categorías + pasillos por supermercado
**Idea:** Separar conceptos:
- **Categoría:** Taxonomía del producto (lácteos, galletas, bebidas...) - viene de Open Food Facts
- **Pasillo:** Ubicación física específica del supermercado (depende del supermercado)

**Ejemplo:**
| Producto | Categoría | Supermercado | Pasillo |
|----------|-----------|--------------|---------|
| Leche | Lácteos | Carrefour | Pasillo 3 (Lácteos) |
| Leche | Lácteos | Mercadona | Pasillo 2 (Frescos) |
| Leche | Lácteos | Lidl | Pasillo 5 (Refrigerados) |

**Implementación:**
1. Nueva tabla `SupermarketAisle`:
   ```kotlin
   data class SupermarketAisle(
       val id: Long,
       val name: String,        // "Pasillo 3 - Lácteos"
       val category: String,    // "en:dairy" (de OpenFoodFacts)
       val supermarket: String // "Carrefour", "Mercadona", etc.
   )
   ```
2. Campo `supermarket` en lista de compras
3. Asignación por defecto cuando se escanea/añade producto nuevo

---

## 🔧 TAREAS TÉCNICAS PENDIENTES

### Arreglar ProductHistoryScreen
- [ ] Conectar a `ProductHistoryDao` real
- [ ] Incluir `aisleId` en `HistoricalProduct`
- [ ] Manejar caso: pasillo eliminado → mostrar "Sin pasillo" y permitir seleccionar

### Arreglar BarcodeScanner
- [ ] Añadir diálogo post-escaneo: "¿En qué pasillo está este producto?"
- [ ] Guardar preferencia en historial (`saveToHistory` con el pasillo seleccionado)
- [ ] Mostrar categoría detectada al usuario

### Sistema de categorías (Fase 2)
- [ ] Modelo `Category` con mapeo a Open Food Facts
- [ ] Tabla de supermercados y sus pasillos
- [ ] UI para cambiar pasillo por supermercado

---

## 📁 ARCHIVOS CLAVE

| Archivo | Propósito |
|---------|-----------|
| `ProductHistoryScreen.kt` | Pantalla historial (ACTUALMENTE CON DATOS MOCKEADOS) |
| `BarcodeScannerScreen.kt` | Escáner de códigos con Open Food Facts |
| `AddProductDialog.kt` | Diálogo añadir producto (con sugerencias funcionando) |
| `ShoppingListRepository.kt` | Acceso a BD incluyendo `saveToHistory` |
| `ProductHistory.kt` | Entity/Dao de historial real |

---

## ✅ ÚLTIMO COMMIT
`d9ee2c2` - Fix imports faltantes en BarcodeScannerScreen

---

**Nota para Hal-futuro:** Si lees esto, Jose está trabajando en la app de lista de compra Android. El repo es suyo (GitHub: joserodriguezballester). Pregúntale qué bug quiere arreglar primero antes de codear.
