# Plan Refactorización - 2026-03-27

**Estado:** Planificación

---

## 🎯 Objetivo Principal

Reestructurar la navegación y pantallas de la app.

---

## 📱 Pantallas

### 1. HomeScreen (nueva)
- **BottomBar:** ❌ No
- **Contenido:** Cards de navegación (Catálogo, Lista Compra, Ofertas*, Supermercados*, Historial*)
- **TopBar:** CommonTopBar (ya existe)
- *Los marcados con * son placeholders sin enlace

### 2. ProductListScreen (ex-ShoppingListScreen/MainScreen)
- **BottomBar:** ✅ Sí (¿qué botones?)
- **Contenido:** Productos ordenados por pasillo
- **FAB:** Añadir producto
- **NOTA:** Hay que darle otra pensada

### 3. CatalogoScreen
- **BottomBar:** ✅ Sí con lupa y filtro
- **FAB:** Añadir artículo (NO producto) → O quitar FAB y poner + en la barra
- **TopBar:** CommonTopBar

---

## 🔧 Tema/Color (Settings)

### Problema actual
- `MainActivity` declara `var primaryColor by remember { mutableStateOf<Int?>(null) }` local
- Nunca se conecta con `ThemeViewModel.primaryColor`
- El color no persiste ni funciona

### Solución
1. MainActivity observa `themeViewModel.primaryColor.collectAsState()`
2. Pasar el color real al `ListaCompraTheme`
3. ThemeViewModel: eliminar duplicado (hay 2)

### ShoppingViewModel
- Jose quiere eliminarlo
- ¿Qué lo sustituye? ¿Dónde va la lógica de lista?

---

## 🗺️ Navegación (no urge)

```
Splash → Home → ProductList
              ↘ Catalogo
              ↘ (Ofertas - placeholder)
              ↘ (Supermercados - placeholder)
              ↘ (Historial - placeholder)
```

Pendiente: añadir rutas nuevas

---

## 📋 Orden de trabajo

1. **Arreglar Settings** (tema/color) ← EMPEZAMOS AQUÍ
2. Crear HomeScreen
3. Refactor ProductListScreen
4. Modificar CatalogoScreen (BottomBar + FAB)
5. Actualizar navegación

---

## ❓ Dudas pendientes

- ProductListScreen: ¿qué botones en BottomBar?
- ShoppingViewModel: ¿qué lo sustituye?
- Catálogo: ¿FAB o botón en barra?

---

*Actualizado: 2026-03-27 00:14*
