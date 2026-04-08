# 📋 TAREAS - Lista Compra App

**Última actualización:** 2026-04-07

---

## ✅ COMPLETADO

### UI General

| # | Tarea | Commit |
|---|-------|--------|
| U1 | BottomBar sin texto, solo iconos | anterior |
| U2 | BottomBar altura 56dp | anterior |
| U3 | Drawer solo navegación (ajustes en overflow) | 26de2b8 |
| U4 | Overflow con categoría 📁 Ajustes | anterior |

### ProductCard

| # | Tarea | Commit |
|---|-------|--------|
| PC1 | Layout oferta reorganizado | 8a644d8 |
| PC2 | Imagen guardada al añadir desde artículo | 018127b |

### Ofertas

| # | Tarea | Commit |
|---|-------|--------|
| O1 | Overflow "Añadir oferta" en OffersScreen | 5ffea60 |
| O2 | Cards con botones editar/eliminar | 5ffea60 |
| O3 | Diálogo confirmación para borrar | 5ffea60 |

### Categorías

| # | Tarea | Commit |
|---|-------|--------|
| C1 | Overflow "Añadir categoría" en CategoriesScreen | 5ffea60 |
| C2 | Cards con botones editar/eliminar | 5ffea60 |
| C3 | Diálogo confirmación para borrar | 5ffea60 |

### Supermercados

| # | Tarea | Commit |
|---|-------|--------|
| S1 | Drawer en lista, quitado FAB, overflow para añadir | 88a2655 |
| S2 | Logos PNG/XML según nombre del supermercado | 88a2655 |
| S3 | Botones editar/eliminar en cards | 88a2655 |
| S4 | Flecha atrás en pantalla de pasillos | 88a2655 |

### Pasillos

| # | Tarea | Commit |
|---|-------|--------|
| P1 | "Añadir pasillo" en overflow | 88a2655 |
| P2 | Botones editar/eliminar en cards | 88a2655 |
| P3 | Diálogo confirmación para borrar | 88a2655 |
| P4 | Reordenamiento con flechas ↑↓ | 88a2655 |

### Catálogo

| # | Tarea | Commit |
|---|-------|--------|
| CA1 | TopBar con drawer | 6443037 |
| CA2 | Overflow con "Añadir manual" y "Escanear código" | 6443037 |
| CA3 | BottomBar 6 iconos sin texto | 6443037 |
| CA4 | Quitados FABs redundantes | 6443037 |

### Historial

| # | Tarea | Commit |
|---|-------|--------|
| H1 | textSize = 24f para eje Y en gráficas | 6443037 |
| H2 | Título incluye "(máx 6)" en comparativa | 6443037 |
| H3 | Grid de 2 columnas para selector | 6443037 |
| H4 | Leyenda con scroll vertical | 6443037 |
| H5 | labelSmall para todos los nombres | 6443037 |
| H6 | Selector tipo cards con imagen/emoji | 6443037 |

### Funcionalidades previas

| # | Tarea | Rama |
|---|-------|------|
| - | Grid 2 columnas | `feature/grid-collapse` |
| - | Colapso de pasillos | `feature/grid-collapse` |
| - | Campo notas en formularios | `feature/dialogs-enhanced` |
| - | Scanner en diálogo añadir | `feature/dialogs-enhanced` |
| - | Logos supermercados (6) | `feature/product-card-enhanced` |
| - | Cumplimiento de oferta | `feature/product-card-enhanced` |
| - | Supermercado preferido en card | `feature/product-card-enhanced` |
| - | Swipe para eliminar | `feature/product-card-enhanced` |
| - | Supermercados como pestañas | `feature/supermarket-tabs` |
| - | Semilla 20 productos | `feature/seed` |
| - | Micrófono TopBar + voz | `feature/microphone` |
| - | Toggle tema en drawer | `feature/theme-toggle-drawer` |
| - | Historial productos + pasillos sugeridos | `feature/product-history` |
| - | Pantalla ofertas + navegación | `main` |

---

## ⬜ PENDIENTE

### Funcionalidades

| # | Tarea | Prioridad | Notas |
|---|-------|-----------|-------|
| T1 | CRUD ofertas completo (falta delete) | Alta | O3 lo implementa |
| T2 | Pantalla añadir oferta personalizada | Media | |
| T4 | Supermercado por producto | Alta | ✅ Completado | Dropdown en añadir/editar + filtro |
| T5 | Mejorar micrófono | Media | ✅ Completado | Beeps + selección múltiple |
| T7 | Exportar BD | Media | JSON/CSV para backup |
| T8 | Importar BD | Media | Restaurar desde backup |

### Catálogo

| # | Tarea | Notas |
|---|-------|-------|
| CA5 | Filtro catálogo funcional | ✅ Completado | Grid chips + búsqueda + categoría en card |

### Mi Lista

| # | Tarea | Notas |
|---|-------|-------|
| ML1 | Cambiar título a "Mi lista" | |
| ML2 | BottomBar con logos supermercados | Sin nombre, solo iconos |
| ML3 | Quitar FAB | + en TopBar y opciones en overflow |
| ML4 | Overflow organizado por categorías | 📁 Añadir, 📁 Lista, 📁 Ajustes |

### Historial (mejoras visuales)

| # | Tarea | Notas |
|---|-------|-------|
| - | Todo completado | H1-H6 implementados |

### Arquitectura

| # | Tarea | Notas |
|---|-------|-------|
| A1 | Valores de UI en archivo aparte | `ui/theme/Dimensions.kt` |

### Funcionalidades futuras

| # | Tarea | Dependencia |
|---|-------|-------------|
| - | Productos comprados → Historial | Necesita pantalla historial |
| - | Historial compras completa | Nueva pantalla |
| - | Sugerir pasillo desde historial | Necesita datos históricos |
| - | Estadísticas de compras | Necesita historial |

---

## 🐛 BUGS

### Alta Prioridad

| # | Bug | Pantalla | Estado | Notas |
|---|-----|----------|--------|-------|
| B1 | Botón scanner no actúa | AddProductToListDialog | ✅ Fixed | Conectar navegación |
| B2 | No se ven las ofertas | ProductCard | ✅ Fixed | Badge visible |
| B3 | Cambiar color no funciona | Navigation + TopBar | - | Dejar para usuarios o eliminar |
| B4 | Falta botón scanner en Editar | EditProductDialog | ✅ Fixed | Añadido igual que en añadir |
| B16 | Drawer no se abre en Home | HomeScreen | ✅ Fixed | ModalNavigationDrawer |
| B17 | Scanner no muestra datos del producto | AddProductToListDialog | - | Escanea pero no rellena |
| B18 | Scanner no asigna categoría | AddArticuloDialog | - | Falta asignación |

### Media Prioridad

| # | Bug | Pantalla | Estado | Notas |
|---|-----|----------|--------|-------|
| B5 | ProductCard layout | ProductCard | ✅ Fixed | Reorganizado |
| B6 | No se ven imágenes | ProductCard | ✅ Fixed | Carga correcta |
| B7 | No hay opción añadir imagen | Add/EditDialog | - | Falta campo/selección |
| B8 | Etiquetas sin delimitación | SupermarketBottomBar | ✅ Fixed | Dividers añadidos |
| B22 | ProductCard precio mal si línea larga | ProductCard | ✅ Fixed | Verificar en móvil |
| B23 | Total de lista incorrecto | ProductListScreen | ✅ Fixed | Resumen con totales |

### Baja Prioridad

| # | Bug | Pantalla | Estado | Notas |
|---|-----|----------|--------|-------|
| B19 | Drawer sí abre en Mi Lista | ProductListScreen | ✅ Fixed | Funciona correctamente |

---

## 📝 Detalles de tareas pendientes

### T4 - Supermercado por producto

**Objetivo:**
1. Cada producto puede asignarse a un supermercado o "Cualquiera"
2. Al filtrar por supermercado X, mostrar productos de X + "Cualquiera"
3. Añadir opción "Todos" en la bottom bar

**Implementación:**

| Paso | Archivo | Cambio |
|------|---------|--------|
| 1 | InitialDataSeeder.kt | Añadir supermercado "Cualquiera" (id=0) |
| 2 | ProductDao.kt | `getProductsBySupermarketOrAnyFlow()` |
| 3 | IProductRepository.kt | Método `getProductsBySupermarketOrAny()` |
| 4 | GetProductsByListUseCase.kt | Flag `showAll` + lógica filtrado |
| 5 | ProductListViewModel.kt | Manejar `showAllProducts` y `selectedSupermarketId` |
| 6 | SupermarketBottomBar.kt | Tab "📦 Todos" al principio |
| 7 | AddProductToListDialog.kt | Dropdown de supermercado |
| 8 | EditProductDialog.kt | Dropdown de supermercado |

**Ver plan detallado:** `PLAN-SUPERMERCADO-PRODUCTO-2026-04-06.md`

---

### T5 - Mejorar micrófono

**Estado actual:**
- Solo aparece en algunas pantallas (Mi Lista)
- Abre un diálogo de instrucciones cada vez
- Busca solo en productos de la lista actual

**Objetivo:**
1. **Aparecer en TODAS las TopBar** - Icono micrófono visible en Home, Mi Lista, Catálogo, Categorías, Ofertas, Supermercados, Historial
2. **Activación directa** - Al pulsar, empieza a escuchar inmediatamente (sin diálogo de instrucciones)
3. **Buscar en catálogo de artículos** - La búsqueda por voz debe consultar `articuloRepository.searchArticulos()` para encontrar coincidencias en toda la BD, no solo en la lista actual

**Implementación:**

| Paso | Archivo | Cambio |
|------|---------|--------|
| 1 | PreferencesManager.kt | Añadir `hasSeenMicInstructions: Boolean` para guardar que ya vio las instrucciones |
| 2 | MicrophoneDialog.kt | Si `hasSeenMicInstructions == true`, iniciar escucha directa sin mostrar diálogo |
| 3 | CommonTopBar.kt | Parámetro `onMicrophoneClick: () -> Unit` obligatorio (no opcional) |
| 4 | HomeScreen.kt | Pasar `onMicrophoneClick` a CommonTopBar |
| 5 | ProductListScreen.kt | Ya tiene micrófono, verificar que usa búsqueda en artículos |
| 6 | OffersScreen.kt | Añadir `onMicrophoneClick` a CommonTopBar |
| 7 | CategoriesScreen.kt | Añadir `onMicrophoneClick` a CommonTopBar |
| 8 | SupermarketListScreen.kt | Añadir `onMicrophoneClick` a CommonTopBar |
| 9 | HistoryScreen.kt | Añadir `onMicrophoneClick` a CommonTopBar |
| 10 | CatalogoScreen.kt | Añadir `onMicrophoneClick` a CommonTopBar |
| 11 | ProductListViewModel.kt | Al recibir texto del micrófono, buscar en `articuloRepository.searchArticulos(query)` además de productos de la lista |
| 12 | AppNavigation.kt | Proporcionar callback de micrófono que navegue a Mi Lista y añada el producto encontrado |

**Comportamiento esperado:**
1. Usuario pulsa 🎤 en cualquier pantalla
2. App empieza a escuchar inmediatamente
3. Usuario dice "leche"
4. App busca "leche" en catálogo de artículos
5. Si encuentra coincidencia → navega a Mi Lista y muestra sugerencia de añadir
6. Si no encuentra → muestra mensaje "No encontrado"

---

### ML4 - Overflow organizado por categorías

**Estructura:**
```
📁 Añadir productos
    Manual
    Scanner
    Desde historial (placeholder)
📁 Lista
    Vaciar
📁 Ajustes
    Modo oscuro
    Cambiar color
```

**Implementación:** Usar headers disabled con estilo primary para separar secciones.

---

### T6 - Sistema de datos precargados

**Objetivo:** Control de qué datos se cargan según entorno

**Modos:**
- **Desarrollo (LOAD_FULL_DATA = true):** Carga todo (supermercados, pasillos Carrefour, categorías, ofertas, artículos ejemplo, productos en lista)
- **Producción (LOAD_FULL_DATA = false):** Carga mínimo (supermercados, pasillos Carrefour, ofertas, categorías)

**Categorías:** ✅ Actualizado - 36 categorías alineadas con Open Food Facts
- ID 1 = "Sin categoría" (fallback obligatorio)
- Mapeo de 90+ tags OFF → categorías locales

**Implementación:**

| Paso | Archivo | Cambio |
|------|---------|--------|
| 1 | DataConfig.kt (nuevo) | `object DataConfig { const val LOAD_FULL_DATA = true }` |
| 2 | InitialDataSeeder.kt | `if (DataConfig.LOAD_FULL_DATA) { seedCatalogIfNeeded() }` |
| 3 | InitialDataSeeder.kt | `if (DataConfig.LOAD_FULL_DATA) { seedProductsIfNeeded() }` |

---

## 🌿 Ramas disponibles

| Rama | Contenido | Estado |
|------|-----------|--------|
| `feature/grid-collapse` | Grid 2 columnas + colapso pasillos | ✅ Mergeado |
| `feature/dialogs-enhanced` | Grid + notas + scanner | ✅ Mergeado |
| `feature/product-card-enhanced` | Grid + logos + oferta + swipe | ✅ Mergeado |
| `feature/supermarket-tabs` | Pestañas supermercados | ✅ Mergeado |
| `feature/seed` | 20 productos de ejemplo | ✅ Mergeado |
| `feature/microphone` | Micrófono en TopBar + voz | ✅ Mergeado |
| `feature/theme-toggle-drawer` | Toggle tema en drawer | ✅ Mergeado |
| `feature/all-ui-complete` | INTEGRACIÓN | ✅ Mergeado |
| `feature/product-history` | Historial productos | ✅ Mergeado |
| `main` | Rama principal | ✅ Actual |

---

## 📋 QUÉ QUEDA POR PROBAR

| Pantalla/Feature | Estado | Notas |
|------------------|--------|-------|
| Grid 2 columnas | ✅ OK | Funciona |
| Pestañas supermercados | ✅ OK | Visibles |
| Colapso pasillos | ✅ OK | Click en header |
| Swipe eliminar | ✅ OK | Swipe izquierda |
| Logos en cards | ✅ OK | Se ven |
| Micrófono TopBar | ✅ OK | Funciona |
| Drawer lateral | ✅ OK | Se abre con ☰ |
| Toggle tema drawer | ✅ OK | Cambia modo |
| Autocompletado | ✅ OK | Sugerencias |
| Campo notas | ✅ OK | Se guarda |
| Selector ofertas | ✅ OK | Dropdown |
| Scanner diálogo añadir | ✅ OK | Navega |
| Editar producto | ✅ OK | Click en card |
| Añadir producto | ✅ OK | FAB |
| Historial gráficas | ✅ OK | 4 pestañas |

---

## 📊 Resumen

| Categoría | Completado | Pendiente |
|-----------|------------|-----------|
| UI General | 4 | 0 |
| ProductCard | 2 | 0 |
| Ofertas | 3 | 0 |
| Categorías | 3 | 0 |
| Supermercados | 4 | 0 |
| Pasillos | 4 | 0 |
| Catálogo | 4 | 1 |
| Historial | 6 | 0 |
| Mi Lista | 0 | 4 |
| Bugs fixed | 8 | 6 |

**Total completado:** 38 items
**Total pendiente:** 11 items

Ofertas.- Borrar con swipe no con papelera. resto ok
Categorias .- Borrar con swipe no con papelera. resto ok
Supermercados .- Borrar con swipe no con papelera. resto ok
Catalogo home ok, buscar ok, filtro mejorable, scanner ok, añadir no lo hace bien . Modificar card
Historial. Tamaño fuente Eje Y mas grande aun
Bottombar, hacerla aun mas estrecha. 
overflow correcto
Drawer. Por orden Home, mi lista, catalogo, categorias, supermercados, ofertas, historial. Ahora creo que faltan algunas
istorial. Ahora creo que faltan algunas

---

### T7 - Exportar BD

**Objetivo:** Permitir exportar todos los datos de la app a archivo JSON/CSV

**Datos a exportar:**
- Artículos del catálogo
- Productos de listas
- Categorías
- Supermercados y pasillos
- Ofertas
- Historial de precios

**Implementación:**
| Paso | Archivo | Cambio |
|------|---------|--------|
| 1 | ExportImportRepository.kt | Nuevo repositorio |
| 2 | ExportImportUseCase.kt | Lógica de exportación |
| 3 | SettingsScreen.kt | Botón "Exportar datos" |
| 4 | StorageAccessFramework | Permiso escritura |

**Formato:** JSON estructurado con versionado

---

### T8 - Importar BD

**Objetivo:** Restaurar datos desde backup

**Implementación:**
| Paso | Archivo | Cambio |
|------|---------|--------|
| 1 | ExportImportRepository.kt | Método import() |
| 2 | ExportImportUseCase.kt | Validación + importación |
| 3 | SettingsScreen.kt | Botón "Importar datos" |
| 4 | ConflictDialog.kt | Resolución de conflictos |

**Conflictos:**
- Duplicados: preguntar (sobrescribir/saltar/renombrar)
- IDs: regenerar si hay conflicto
