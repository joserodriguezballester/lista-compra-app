# MEJORAS Y ERRORES - 2026-04-07

## HISTORIAL - Gráficas

**H1** - ✅ Pestañas 3 y 4: Fuente eje Y más grande
- textSize = 24f (antes 18f/20f)
- Commit: 88a2655

**H2** - ✅ Pestaña 4: Eliminar texto redundante
- Título: "📊 Comparativa de productos (máx 6)"
- Commit: 88a2655

**H3** - ✅ Pestaña 4: Productos en grid 2 columnas
- LazyVerticalGrid con GridCells.Fixed(2)
- Commit: 88a2655

**H4** - ✅ Pestaña 4: Leyenda con scroll
- LazyColumn dentro de Card con altura fija
- Commit: 88a2655

**H5** - ✅ Todas las pestañas: Tamaño fuente nombres
- labelSmall (11sp) para todos los textos de producto
- Commit: 88a2655

**H6** - ✅ Pestaña 2: Dropdown visual tipo cards
- ProductSelectorCards: Cards horizontales con imagen/emoji
- Borde coloreado cuando está seleccionado
- Commit: 88a2655

---

## UI GENERAL

**U1** - BottomBar: Sin texto
- Solo iconos
- Quitar label de NavigationBarItem

**U2** - BottomBar: Más estrecha
- Reducir altura (modificar modifier)

**U3** - Drawer: Solo navegación
- Quitar opciones que no sean navegación

**U4** - Overflow: Categoría Ajustes
- Agrupar: modo oscuro, cambiar color bajo Ajustes

---

## CATEGORÍAS

**C1** - Overflow: Añadir categoría
- Añadir opción "Añadir categoría" en el menú overflow
- Abrir diálogo para crear nueva categoría

**C2** - Cards: Editar
- Añadir botón lápiz para editar categoría
- Abrir diálogo con nombre e icono actuales

**C3** - Cards: Swipe para borrar
- Implementar swipe para eliminar
- Verificar restricciones de BD antes de borrar (productos asociados)
- Mostrar diálogo de confirmación
- Si tiene productos asociados: mostrar advertencia y no permitir borrar

---

## OFERTAS

**O1** - Overflow: Añadir oferta
- Añadir opción "Añadir oferta" en el menú overflow
- Abrir diálogo para crear nueva oferta (nombre, código, descripción)
- Funcionalidad completa: guardar en BD

**O2** - Cards: Editar
- Añadir botón lápiz para editar oferta
- Abrir diálogo con datos actuales
- Funcionalidad completa: actualizar en BD

**O3** - Cards: Swipe para borrar
- Implementar swipe para eliminar
- Verificar restricciones de BD (productos que usan la oferta)
- Mostrar diálogo de confirmación
- Si tiene productos asociados: mostrar advertencia y no permitir borrar
- Funcionalidad completa: eliminar de BD

---

## SUPERMERCADOS

**S1** - ✅ Lista: Coherencia UI
- Quitado FAB de añadir
- Añadido "Añadir supermercado" en overflow
- Cambiado flecha atrás por drawer en TopBar
- Drawer funcional

**S2** - ✅ Cards: Usar logos en vez de emojis
- Función getSupermarketLogo() detecta nombre
- Muestra PNG (Carrefour, Mercadona, Lidl, Consum) o XML (Aldi, Dia)
- Mantiene emoji como fallback

**S3** - ✅ Cards: Editar
- Botones editar/eliminar en cada card
- Diálogo reutilizable para editar

**S4** - ✅ Detail (pasillos): Mantener flecha atrás
- SupermarketAislesScreen usa CommonTopBar con onNavigateBack
- No usa drawer aquí

---

## PASILLOS (dentro de supermercado)

**P1** - ✅ Overflow: Añadir pasillo
- "Añadir pasillo" en overflow de SupermarketAislesScreen

**P2** - ✅ Cards: Editar
- Botones editar/eliminar en cada card

**P3** - ✅ Cards: Borrar
- Diálogo de confirmación antes de eliminar

**P4** - ✅ Ordenar pasillos: Funcional
- Botón "Reordenar pasillos" en overflow
- Flechas arriba/abajo para mover
- Guardar con ✓ al terminar

---

## CATÁLOGO

**CA1** - TopBar: Usar drawer
- Cambiar flecha atrás por drawer

**CA2** - Overflow: Opciones
- Añadir "Añadir manual" (abrir diálogo)
- Añadir "Añadir por scanner" (abrir cámara)

**CA3** - BottomBar: Iconos funcionales
- Home: navegar a Home
- Buscar: abrir campo de búsqueda
- Filtrar: abrir panel de filtros
- Scanner: abrir cámara
- Añadir: abrir diálogo de nuevo artículo
- Solo iconos, sin texto

**CA4** - Quitar FAB
- Ya están las acciones en BottomBar y Overflow

**CA5** - Filtro: Implementar funcionalidad
- El filtro actual no funciona
- Filtrar por categoría, supermercado, etc.

---

## MI LISTA

**ML1** - TopBar: Cambios
- Cambiar título a "Mi lista"
- Añadir botón + para añadir productos
- El botón + abre el mismo diálogo de añadir manual

**ML2** - BottomBar: Supermercados
- Mostrar iconos de supermercados
- Sin nombre, solo iconos

**ML3** - Quitar FAB
- Ya está el + en TopBar y opciones en overflow

**ML4** - Overflow: Opciones organizadas por categorías
- 📁 Añadir productos: Manual, Scanner, Desde historial (placeholder)
- 📁 Lista: Vaciar
- 📁 Ajustes: Modo oscuro, Cambiar color

---

## PRODUCT_CARD (componente)

**PC1** - ✅ Layout con oferta: Reorganizar líneas
- Línea superior: badge oferta + total sin oferta tachado
- Línea inferior: unidades x precio + total final
- Commit: 8a644d8

**PC2** - ✅ Imagen se guarda al añadir desde artículo
- AddProductToListDialog: asigna photoUri del artículo seleccionado
- Commit: 018127b

---

## ARQUITECTURA

**A1** - Valores de UI en archivo aparte
- Ubicación: ui/theme/Dimensions.kt
- Incluir: tamaños de fuente, alturas, paddings comunes
rtículo
- Al añadir un producto desde el catálogo de artículos, el artículo tiene imagen pero el producto creado no la guarda
- Verificar AddProductToListDialog y ProductListViewModel: se debe pasar y guardar la imagen del artículo seleccionado

---

## ARQUITECTURA

**A1** - Valores de UI en archivo aparte
- Ubicación: ui/theme/Dimensions.kt
- Incluir: tamaños de fuente, alturas, paddings comunes
