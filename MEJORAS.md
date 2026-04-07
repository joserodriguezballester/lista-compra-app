# MEJORAS Y ERRORES - 2026-04-07

## HISTORIAL - Gráficas

**H1** - Pestañas 3 y 4: Fuente eje Y más grande
- Ahora: textSize = 20f y 18f
- Cambiar a: textSize = 24f

**H2** - Pestaña 4: Eliminar texto redundante
- Quitar: "Selecciona productos (máx 6)"
- Añadir "(máx 6)" al título

**H3** - Pestaña 4: Productos en 2 filas
- Ahora: LazyRow (1 fila)
- Cambiar: LazyVerticalGrid o 2 filas

**H4** - Pestaña 4: Leyenda cortada
- Solución: Scroll en la columna

**H5** - Todas las pestañas: Tamaño fuente nombres
- Aplicar: labelSmall (11sp, estilo BottomBar)

**H6** - Pestaña 2: Dropdown visual
- Opción B: Cards horizontales con imagen y nombre
- Cards pequeñas con icono/emoji arriba y nombre abajo
- Borde coloreado cuando está seleccionado
- Grid horizontal (2 filas)

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

**S1** - Lista: Coherencia UI
- Quitar FAB de añadir
- Quitar botones de editar y papelera de las cards
- Añadir "Añadir supermercado" en overflow
- Swipe para borrar (verificar restricciones: pasillos asociados)
- Cambiar flecha atrás por drawer en TopBar

**S2** - Cards: Usar logos en vez de emojis
- Mostrar logo del supermercado (Carrefour, Mercadona, etc.)
- Preparar para eliminar emojis de BD en el futuro
- Mantener compatibilidad mientras tanto

**S3** - Cards: Editar
- Swipe hacia la derecha o menú contextual para editar
- Abrir diálogo con datos actuales
- Funcionalidad completa: actualizar en BD

**S4** - Detail (pasillos): Mantener flecha atrás
- En la pantalla de pasillos, usar flecha atrás en TopBar
- No usar drawer aquí

---

## PASILLOS (dentro de supermercado)

**P1** - Overflow: Añadir pasillo
- Añadir opción "Añadir pasillo" en el menú overflow
- Abrir diálogo para crear nuevo pasillo

**P2** - Cards: Editar
- Añadir botón lápiz para editar pasillo
- Abrir diálogo con nombre actual
- Funcionalidad completa: actualizar en BD

**P3** - Cards: Swipe para borrar
- Implementar swipe para eliminar
- Verificar restricciones de BD (productos asociados)
- Mostrar diálogo de confirmación

**P4** - Ordenar pasillos: Arreglar
- El botón de ordenar no funciona
- Implementar reordenamiento con drag & drop o flechas

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

**PC1** - Layout con oferta: Reorganizar líneas
- Cuando el producto tiene oferta, la última línea queda: unidades, precio, total sin oferta tachado y total final
- Cambio propuesto: Poner el total sin oferta tachado en la línea de arriba
- Línea superior: oferta (badge a la izquierda) y total sin oferta tachado (a la derecha)
- Línea inferior: unidades, precio, total final

**PC2** - Imagen no se guarda al añadir desde artículo
- Al añadir un producto desde el catálogo de artículos, el artículo tiene imagen pero el producto creado no la guarda
- Verificar AddProductToListDialog y ProductListViewModel: se debe pasar y guardar la imagen del artículo seleccionado

---

## ARQUITECTURA

**A1** - Valores de UI en archivo aparte
- Ubicación: ui/theme/Dimensions.kt
- Incluir: tamaños de fuente, alturas, paddings comunes
