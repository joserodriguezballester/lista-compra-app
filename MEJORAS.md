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

## ARQUITECTURA

**A1** - Valores de UI en archivo aparte
- Ubicación: ui/theme/Dimensions.kt
- Incluir: tamaños de fuente, alturas, paddings comunes
