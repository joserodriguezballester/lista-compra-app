# 🔍 VERIFICAR - Pruebas Manuales

**Fecha:** 2026-04-08
**Sesión:** T4 + CA5 + T5 refactor + voz con supermercado

---

## 🎤 T5 Refactor - Voz directa en todas las pantallas

| Pantalla | Click 🎤 | Esperado | Estado |
|----------|----------|----------|--------|
| Home | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |
| Mi Lista | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |
| Catálogo | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |
| Categorías | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |
| Ofertas | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |
| Historial | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |
| Supermercados | Abre diálogo voz | Sin navegar | ⬜ OK / ❌ NO OK |

---

## 📦 CA5 - Filtro Catálogo

### Categoría en ArticuloCard

| Prueba | Pasos | Estado |
|--------|-------|--------|
| Badge visible | 1. Abrir Catálogo<br>2. Ver cards de artículos<br>3. Comprobar que aparece emoji + nombre de categoría en esquina superior izquierda | ⬜ OK / ❌ NO OK |
| Sin categoría | 1. Ver artículo sin categoría<br>2. No debe mostrar badge (o mostrar "📦 Sin categoría") | ⬜ OK / ❌ NO OK |

### Filtro por categoría

| Prueba | Pasos | Estado |
|--------|-------|--------|
| Abrir filtro | 1. Click en botón filtro (bottom bar)<br>2. Debe abrir diálogo con grid de categorías | ⬜ OK / ❌ NO OK |
| Buscar categoría | 1. Escribir "lácteos" en búsqueda<br>2. Debe filtrar las categorías | ⬜ OK / ❌ NO OK |
| Aplicar filtro | 1. Seleccionar categoría<br>2. Click "Aplicar"<br>3. Solo artículos de esa categoría | ⬜ OK / ❌ NO OK |
| Quitar filtro | 1. Click filtro<br>2. Seleccionar "Todas"<br>3. Ver todos los artículos | ⬜ OK / ❌ NO OK |

---

## 🎤 T4 - Supermercado en Voz

### Parseo de supermercado explícito

| Prueba | Comando | Esperado | Estado |
|--------|---------|----------|--------|
| Mercadona | "3 litros de leche del Mercadona" | `supermarketId = 2` | ⬜ OK / ❌ NO OK |
| Carrefour | "2 kilos de patatas del Carrefour" | `supermarketId = 1` | ⬜ OK / ❌ NO OK |
| Lidl | "1 paquete de pasta del Lidl" | `supermarketId = 3` | ⬜ OK / ❌ NO OK |

### Sin supermercado explícito

| Prueba | Contexto | Comando | Esperado | Estado |
|--------|----------|---------|----------|--------|
| Desde Mi Lista (Carrefour) | Bottom bar en Carrefour | "3 de leche" | `supermarketId = 1` | ⬜ OK / ❌ NO OK |
| Desde Mi Lista (Todos) | Bottom bar en "Todos" | "3 de leche" | `supermarketId = 0` (Cualquiera) | ⬜ OK / ❌ NO OK |
| Desde Historial | Sin bottom bar | "3 de leche" | `supermarketId = 0` (Cualquiera) | ⬜ OK / ❌ NO OK |
| Desde Categorías | Sin bottom bar | "3 de leche" | `supermarketId = 0` (Cualquiera) | ⬜ OK / ❌ NO OK |

### Flujo completo de voz

| Prueba | Pasos | Estado |
|--------|-------|--------|
| 1 coincidencia | 1. Click 🎤 desde cualquier pantalla<br>2. Decir "3 de leche"<br>3. Si hay 1 match → añade directo + beep éxito | ⬜ OK / ❌ NO OK |
| Múltiples coincidencias | 1. Decir "leche" (genérico)<br>2. Si hay varios matches → diálogo selección | ⬜ OK / ❌ NO OK |
| Sin coincidencias | 1. Decir "xyzabc" (inventado)<br>2. Añade producto genérico + beep error | ⬜ OK / ❌ NO OK |

---

## 🛒 T4 - Supermercado por Producto

### Añadir producto manual

| Prueba | Pasos | Estado |
|--------|-------|--------|
| Dropdown visible | 1. Click + para añadir producto<br>2. Ver dropdown "Supermercado" con 📦 Cualquiera por defecto | ⬜ OK / ❌ NO OK |
| Seleccionar supermercado | 1. Abrir dropdown<br>2. Ver lista: Cualquiera, Carrefour, Mercadona, etc.<br>3. Seleccionar Mercadona | ⬜ OK / ❌ NO OK |
| Guardar con supermercado | 1. Completar formulario<br>2. Guardar<br>3. Verificar que aparece en filtrado correcto | ⬜ OK / ❌ NO OK |

### Editar producto

| Prueba | Pasos | Estado |
|--------|-------|--------|
| Ver supermercado actual | 1. Click en producto para editar<br>2. Ver dropdown con supermercado actual seleccionado | ⬜ OK / ❌ NO OK |
| Cambiar supermercado | 1. Cambiar de Mercadona a Carrefour<br>2. Guardar<br>3. Verificar cambio en lista | ⬜ OK / ❌ NO OK |

### Filtrado por supermercado

| Prueba | Pasos | Estado |
|--------|-------|--------|
| Tab "Todos" | 1. Click en 📦 Todos en bottom bar<br>2. Ver TODOS los productos | ⬜ OK / ❌ NO OK |
| Tab Carrefour | 1. Click en 🛒 Carrefour<br>2. Ver productos Carrefour + productos "Cualquiera" | ⬜ OK / ❌ NO OK |
| Tab Mercadona | 1. Click en 🟢 Mercadona<br>2. Ver productos Mercadona + productos "Cualquiera" | ⬜ OK / ❌ NO OK |
| Productos "Cualquiera" siempre visibles | 1. Añadir producto con "Cualquiera"<br>2. Cambiar entre tabs<br>3. El producto debe aparecer en TODAS las tabs | ⬜ OK / ❌ NO OK |

---

## 🐛 Bugs a verificar

| Bug | Prueba | Estado |
|-----|--------|--------|
| Imagen desde galería | 1. Editar producto<br>2. Seleccionar imagen de galería<br>3. Guardar<br>4. Verificar que la imagen se guarda | ⬜ OK / ❌ NO OK |
| Scanner en Editar | 1. Editar producto<br>2. Click botón scanner<br>3. Verificar que navega a scanner | ⬜ OK / ❌ NO OK |
| HomeScreen - showVoiceDialog | 1. Compilar app<br>2. Verificar que no hay errores de compilación | ✅ FIXED |
| HistoryScreen - showVoiceDialog | 1. Compilar app<br>2. Verificar que no hay errores de compilación | ✅ FIXED |
| CategoriesScreen - showVoiceDialog | 1. Compilar app<br>2. Verificar que no hay errores de compilación | ✅ FIXED |
| OffersScreen - showVoiceDialog | 1. Compilar app<br>2. Verificar que no hay errores de compilación | ✅ FIXED |
| SupermarketListScreen - showVoiceDialog | 1. Compilar app<br>2. Verificar que no hay errores de compilación | ✅ FIXED |

---

## 📝 Notas

### Supermercados reconocidos en voz:
- `mercadona`
- `mislata`
- `carrefour`
- `alberca`
- `lidl`
- `aldi`
- `consum`
- `dia`

### IDs de supermercado:
- `0` = Cualquiera (se muestra en todos los filtros)
- `1` = Carrefour La Alberca
- `2` = Mercadona Mislata
- `3` = Lidl
- `4` = Aldi
- `5` = Consum

---

## ✅ Checklist final

- [ ] T5: Voz directa en TODAS las pantallas (sin navegación)
- [ ] CA5: Categoría visible en cards
- [ ] CA5: Filtro funcional
- [ ] T4: Voz parsea supermercado
- [ ] T4: Voz usa bottom bar si no hay explícito
- [ ] T4: Dropdown en añadir producto
- [ ] T4: Dropdown en editar producto
- [ ] T4: Tab "Todos" funciona
- [ ] T4: Filtro X incluye "Cualquiera"
- [ ] Bugs: Imagen se guarda
- [ ] Bugs: Scanner en editar funciona

---

**Instrucciones:**
1. Hacer `git pull` en el móvil
2. Probar cada caso
3. Marcar ✅ o ❌ según resultado
4. Si falla, anotar qué pasa exactamente
