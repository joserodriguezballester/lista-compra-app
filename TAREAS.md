# Tareas Pendientes - Lista Compra App

**Fecha:** 2026-03-30
**Rama:** `feature/supermarket-refactor`

---

## UI/UX Cambios

### Catálogo (CatalogoScreen)
- [x] **Imagen por defecto** cuando el artículo no tiene foto ✅
- [ ] **Selector de imagen (cámara/galería)** al editar artículo
  - Añadir permisos de cámara
  - ActivityResultLauncher para cámara y galería
  - Guardar URI de la imagen seleccionada
- [ ] **Investigar**: ¿Qué son los elementos en esquina inferior derecha de algunos artículos? → **Es el icono QrCode cuando tiene EAN** ✅
- [x] **Al editar artículo (AddEditArticuloDialog/ArticuloDetailDialog)**:
  - [ ] No deja clickar en imagen para cambiarla
  - [ ] No deja cambiar categoría
  - [ ] Mostrar **nombre de categoría** en lugar de categoryId
  - [ ] Añadir funcionalidad de **scanner** de códigos de barras

### Modo Claro/Oscuro
- [ ] Mover toggle de tema de TopAppBar al menú lateral
- [ ] En su lugar (TopAppBar) poner botón de micrófono para añadir productos por voz

### TopAppBar
- [ ] Reemplazar botón de tema por botón de micrófono
- [ ] Micrófono abre input de voz para añadir producto

---

## Funcionalidad

### Lista de la Compra (ProductListScreen)
- [ ] **Opción de vaciar lista** completa
  - Añadir botón/acción en menú o toolbar
  - Confirmación antes de vaciar

### Historial de Compras
- [ ] **Investigar**: ¿Ya tenemos historial de compras?
  - Tabla `purchase_history`
  - Tabla `product_history`
  - ¿Se está usando?

### Importar Lista/Ticket
- [ ] **Estudiar viabilidad**:
  - Importar lista de texto
  - Escanear ticket (OCR)
  - Importar desde archivo

### Añadir Producto - Autocompletado/Sugerencias
- [ ] Sugerir nombres de productos mientras se escribe
  - Fuente: catálogo de `articulos`
  - Buscar por nombre parcial
- [ ] Sugerir pasillo según:
  - Artículo seleccionado del catálogo
  - Historial: `articulo_supermarket_defaults` (dónde se puso antes)
  - Categoría del artículo → mapeo a pasillo por supermercado
- [ ] Orden de sugerencias:
  1. Coincidencia exacta en catálogo
  2. Historial del usuario en ese supermercado
  3. Por categoría

### Añadir Producto por Voz
- [ ] Botón micrófono en TopAppBar
- [ ] Reconocimiento de voz → texto
- [ ] Parsear texto para extraer:
  - Nombre del producto
  - Cantidad (opcional)
  - Categoría (opcional, por inferencia)
- [ ] Añadir producto a la lista actual

---

## Base de Datos

### Estado actual
- ✅ Supermercados (5)
- ✅ Categorías (19)
- ✅ Pasillos Carrefour (19) + genéricos otros supermercados
- ✅ Artículos (15)
- ❌ **ShoppingList por defecto** (falta, causa FOREIGN KEY error)
- ⚠️ **Ordenación de pasillos por supermercado** → ¿Implementada?

### Verificar
- [ ] ¿Está implementada la ordenación de pasillos por supermercado?
- [ ] Tabla `category_supermarket_orders` → ¿Se usa?
- [ ] ¿Cómo se ordenan los productos en la lista según el pasillo?

---

## Errores Conocidos

### Críticos (bloquean funcionalidad)
- [ ] **FOREIGN KEY constraint failed al añadir producto**
  - Causa: `shoppingListId = 1` pero NO existe lista con id=1
  - Solución: Crear lista por defecto en DataSeeder
  - Archivo: `InitialDataSeeder.kt` → añadir `seedShoppingListIfNeeded()`

### Resueltos
- ✅ DataSeeder solo cargaba artículos → Ahora usa Initializer
- ✅ RepositoryModule duplicado → Renombrado
- ✅ IProductRepository/ProductRepositoryImpl desincronizados → Sincronizados

### Pendientes de verificar
- [ ] ¿Se poblan todas las tablas correctamente?
- [ ] ¿Los pasillos de Carrefour son correctos?
- [ ] ¿Las categorías coinciden con los artículos?

---

## Notas

- Los pasillos de Carrefour son importantes, no tocarlos
- Usar `applicationIdSuffix = ".dev"` para versión debug (no sobrescribir original)
- Repositorio: `https://github.com/joserodriguezballester/lista-compra-app.git`

---

## Commits Importantes

| Hash | Descripción |
|------|-------------|
| `ab27ed2` | Cargar Datos (fix errores) |
| `f9476f5` | DatabaseSeedInitializer |
| `c212de2` | DataSeeder usa repositorios |

---

## Auditoría / Refactorización

### Clean Architecture
- [ ] Revisar que los ViewModels NO accedan directamente a repositorios
- [ ] Verificar que UseCases encapsulan lógica de negocio
- [ ] Comprobar que entidades de dominio NO dependen de framework Android

### Código Duplicado
- [ ] Buscar pantallas/componentes duplicados
- [ ] Revisar ViewModels con lógica similar
- [ ] Unificar UseCases que hacen lo mismo

### Código Inservible / Obsoleto
- [ ] Buscar clases/métodos no usados
- [ ] Eliminar imports no usados
- [ ] Revisar si hay código comentado que se puede borrar

### Organización de Paquetes
- [ ] Verificar estructura por capas (data/domain/presentation)
- [ ] Nombres de paquetes consistentes (¿`catalogo` vs `catalog`?)
- [ ] Archivos en ubicación correcta

### Dependencias
- [ ] Revisar dependencias no usadas en `build.gradle.kts`
- [ ] Versiones desactualizadas

---

## Prioridad

### Alta (bloquea uso básico)
1. **FOREIGN KEY failed** → Crear lista por defecto en DataSeeder
2. Verificar que se poblan todas las tablas

### Media (bugs de UI / funcionalidad)
3. **Catálogo - Edición de artículos**:
   - No deja clickar en imagen para cambiarla
   - No deja cambiar categoría
   - Mostrar nombre de categoría (no ID)
4. Imagen por defecto en artículos sin foto
5. Añadir scanner al editar artículo
6. **Opción vaciar lista completa**

### Investigar (estudio previo)
7. ¿Tenemos historial de compras funcional?
8. Viabilidad de importar lista/ticket

### Baja (mejoras UX)
9. Sugerencias de nombre/pasillo al añadir producto
10. Mover toggle tema al menú
11. Botón micrófono en TopAppBar
12. Añadir producto por voz

---

*Actualizar este archivo según avance*
