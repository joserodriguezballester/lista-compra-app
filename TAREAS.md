# 📋 TAREAS - Lista Compra App

**Última actualización:** 2026-04-02 05:55

---

## 🌿 Ramas disponibles para probar

| Rama | Contenido | Estado |
|------|-----------|--------|
| `feature/grid-collapse` | Grid 2 columnas + colapso pasillos | ✅ Listo |
| `feature/dialogs-enhanced` | Grid + notas + scanner | ✅ Listo |
| `feature/product-card-enhanced` | Grid + logos + oferta + swipe | ✅ Listo |
| `feature/supermarket-tabs` | Pestañas supermercados | ✅ Listo |
| `feature/seed` | 20 productos de ejemplo | ✅ Listo |
| `feature/microphone` | Micrófono en TopBar + voz | ✅ Listo |

---

## 🔴 Prioridad Alta - Implementables AHORA

> No tienen dependencias, se pueden hacer inmediatamente

| # | Tarea | Pantalla | Esfuerzo |
|---|------|----------|----------|
| 1 | **Lista en grid 2 columnas** | ProductListScreen | Medio |
| 2 | **Autocompletado al añadir** | AddProductToListDialog | Bajo |
| 3 | **Autofill al seleccionar sugerencia** | AddProductToListDialog | Bajo |
| 4 | **Selector de ofertas** | AddProductToListDialog | Verificar |
| 5 | **Visualizar cumplimiento de oferta** | ProductCard | Medio |
| 6 | **Supermercados como pestañas** | SupermarketBottomBar | Bajo |
| 7 | **Scanner en diálogo añadir** | AddProductToListDialog | Bajo |
| 8 | **Supermercado preferido en producto** | ProductCard + formularios | Medio |
| 9 | **Campo notas en formularios** | AddProductToListDialog + EditProductDialog | Bajo |
| 10 | **Logos de supermercados** | Assets | Bajo |
| 11 | **Semilla de productos** | DataSeeder | Bajo |
| 12 | **Borrado con swipe** | ProductCard | Medio |
| 13 | **Colapso de pasillos** | ProductListScreen | Medio |
| 14 | **Revisar campo "unidad"** | AddProductToListDialog | Decisión |

---

## 🟠 Prioridad Media - Verificaciones y UI

| # | Tarea | Pantalla | Notas |
|---|------|----------|-------|
| 15 | Editar precio/oferta | EditProductDialog | Verificar que funciona |
| 16 | Pasillos destacados | ProductListScreen | Verificar visualización |
| 17 | Mover toggle tema | TopBar → Menú lateral | Liberar espacio |
| 18 | Probar en móvil real | Todas | Testing completo |
| 19 | Merge feature/offers-screen | main | Cuando esté probado |

---

## 🔵 Prioridad Baja / Futuro - Dependen de HISTORIAL

> Requieren que la funcionalidad de historial esté implementada primero

| # | Tarea | Dependencia |
|---|------|-------------|
| 20 | **Productos comprados → Historial** | Necesita pantalla historial |
| 21 | **Micrófono en TopBar** | SpeechRecognizer + parseo voz |
| 22 | **Historial compras** | Nueva pantalla completa |
| 23 | **Sugerir pasillo desde historial** | Necesita datos históricos |
| 24 | **Estadísticas de compras** | Necesita historial |

---

## 📝 Detalles de implementación

---

### #1 - Lista en grid 2 columnas

**Objetivo:**
```
┌─────────────┬─────────────┐
│  Producto 1 │  Producto 2 │
├─────────────┼─────────────┤
│  Producto 3 │  Producto 4 │
└─────────────┴─────────────┘
```

**Implementación:**
- `LazyVerticalGrid` con `GridCells.Fixed(2)`
- Headers de pasillo a ancho completo
- Cards ajustadas para 2 por fila

---

### #2 y #3 - Autocompletado y Autofill

**Estado actual:** Ya existe `ExposedDropdownMenuBox` con sugerencias

**Verificar:**
- Debounce 300ms
- Buscar en `articuloRepository.searchArticulos(query)`
- Al seleccionar: rellenar nombre, precio, imagen

**El pasillo:** No disponible en `Articulo` (es genérico). El usuario lo selecciona manualmente.

---

### #4 - Selector de ofertas

**Ya implementado.** Verificar:
- Dropdown con 5 ofertas predefinidas
- Cálculo automático del precio final
- Se guarda `offerId` en el producto

---

### #5 - Visualizar cumplimiento de oferta

**Problema:** Ofertas tienen requisitos mínimos

| Oferta | Requisito |
|--------|-----------|
| 3x2 | 3 uds |
| 2x1 | 2 uds |
| 2ª-50% | 2 uds |
| 4x3 | 4 uds |

**Mostrar en ProductCard:**
- No cumple: "⚠️ Añade 1 ud más"
- Cumple: "✅ Oferta aplicada" + ahorro

---

### #6 - Supermercados como pestañas

**Cambiar:** Chips → `PrimaryTabRow`

```kotlin
PrimaryTabRow(selectedTabIndex = selectedIndex) {
    supermarkets.forEachIndexed { index, supermarket ->
        Tab(
            selected = index == selectedIndex,
            onClick = { onSelected(supermarket.id) },
            text = { Text("${supermarket.emoji} ${supermarket.name}") }
        )
    }
}
```

---

### #7 - Scanner en diálogo añadir

**Flujo:**
```
[Añadir producto] → [📷 Scanner] → Escanea EAN → Rellena campos
```

**Implementación:**
- Botón con `Icons.Default.QrCodeScanner`
- Navegar a `BarcodeScannerScreen`
- Recibir datos al volver

---

### #8, #9, #10 - Supermercado preferido

**Idea:** Usar campo `notes` para indicar "comprar en Mercadona"

**Visual:**
```
┌─────────────────────────┐
│ 📦 Pan de molde      🏪 │ ← Logo pequeño
│     (del Mercadona)      │
└─────────────────────────┘
```

**Logos a descargar:**

| Supermercado | Archivo |
|--------------|---------|
| Carrefour | `logo_carrefour.png` |
| Mercadona | `logo_mercadona.png` |
| Lidl | `logo_lidl.png` |
| Aldi | `logo_aldi.png` |
| Dia | `logo_dia.png` |
| Consum | `logo_consum.png` |

---

### #11 - Semilla de productos

**15 productos de ejemplo:**

| Producto | Pasillo | Precio | Supermercado preferido |
|----------|---------|--------|----------------------|
| Leche entera | Lácteos | 1.15€ | |
| Pan de molde | Panadería | 1.50€ | Mercadona |
| Huevos | Charcutería | 2.10€ | |
| Tomates | Frutas/Verduras | 1.80€ | |
| Plátanos | Frutas/Verduras | 1.20€ | |
| Galletas María | Despensa | 1.00€ | |
| Yogures | Lácteos | 2.50€ | |
| Jamón york | Charcutería | 2.30€ | |
| Aceite oliva | Despensa | 4.50€ | |
| Café | Despensa | 3.20€ | |
| Queso rallado | Quesos | 2.00€ | |
| Zumo de naranja | Bebidas | 2.80€ | |
| Aceite girasol | Despensa | 3.00€ | Lidl |
| Leche desnatada | Lácteos | 1.20€ | Consum |
| Mantequilla | Lácteos | 1.80€ | |

---

### #12 - Borrado con swipe

**Implementación:** `SwipeToDismiss` de Material 3

```kotlin
SwipeToDismiss(
    state = dismissState,
    background = { /* Rojo con icono eliminar */ },
    dismissContent = { ProductCard(...) },
    directions = setOf(DismissDirection.EndToStart)
)
```

**Comportamiento:**
- Swipe izquierda → eliminar
- SnackBar con "Deshacer" (5 segundos)

---

### #13 - Colapso de pasillos

**Visual:**
```
🥛 Lácteos (3) [▼]  ← Expandido
🥖 Panadería (2) [▶]  ← Colapsado
```

**Implementación:**
- Estado: `collapsedAisles: Set<Long>` en ViewModel
- Icono en `AisleHeader`
- Condicional en `LazyColumn`

---

### #14 - Revisar campo "unidad"

**Pregunta:** ¿Qué representa?
- ¿Tipo de envase? (brik, botella...)
- ¿Unidad de medida? (kg, l...)

**Soluciones:**
- A) Eliminar si no tiene sentido
- B) Renombrar a "Presentación"
- C) Mostrar en card

---

## 🎨 Decisiones de Diseño

### Productos sin artículo en catálogo

**Cuando se añade un producto que no existe:**

```
Diálogo: "¿Guardar en tu catálogo?"

[No, esta vez no] → Producto libre, sin historial
[Sí, guardar rápido] → Crea Articulo mínimo (nombre + fecha)
[Editar completo] → Abre diálogo de añadir artículo completo
```

**Justificación:**
- Facilita el historial futuro
- Opciones flexibles según necesidad del usuario

---

## ✅ Completado

| Fecha | Tarea | Rama |
|-------|-------|------|
| 2026-04-02 | Grid 2 columnas | `feature/grid-collapse` |
| 2026-04-02 | Colapso de pasillos | `feature/grid-collapse` |
| 2026-04-02 | Campo notas en formularios | `feature/dialogs-enhanced` |
| 2026-04-02 | Scanner en diálogo añadir | `feature/dialogs-enhanced` |
| 2026-04-02 | Logos supermercados (6) | `feature/product-card-enhanced` |
| 2026-04-02 | Cumplimiento de oferta | `feature/product-card-enhanced` |
| 2026-04-02 | Supermercado preferido en card | `feature/product-card-enhanced` |
| 2026-04-02 | Swipe para eliminar | `feature/product-card-enhanced` |
| 2026-04-02 | Supermercados como pestañas | `feature/supermarket-tabs` |
| 2026-04-02 | Semilla 20 productos | `feature/seed` |
| 2026-04-02 | Micrófono TopBar + voz | `feature/microphone` |
