# Auditoría Profesional - Lista Compra App

**Fecha:** 10/04/2026
**Archivos Kotlin:** 207
**Líneas de código:** ~20,000
**Versión base de datos:** 13

---

## 🔴 PROBLEMAS CRÍTICOS

### 1. Base de Datos: Migraciones Faltantes
**Archivo:** `data/local/Database.kt:25`
```kotlin
fallbackToDestructiveMigration()
```
**Problema:** Si un usuario actualiza la app de versión 12 a 13, **pierde todos los datos**. Solo hay migraciones para 7→8, 10→11, 11→12.

**Solución:** Implementar migración 12→13 o documentar que es aceptable en esta fase de desarrollo.

### 2. Base de Datos: Schema No Exportado
**Archivo:** `data/local/Database.kt`
```kotlin
exportSchema = false
```
**Problema:** Imposible probar migraciones automáticamente. Mala práctica de Room.

**Solución:** Habilitar `exportSchema = true` y configurar `room.schemaLocation`.

### 3. ViewModel Gigante
**Archivo:** `ui/viewmodel/ProductListViewModel.kt` (511 líneas)
**Problema:** 19 UseCases inyectados. Violación de Single Responsibility Principle.

**Solución:** Dividir en ViewModels más pequeños:
- `ProductListViewModel` (gestión de lista)
- `ProductAddViewModel` (añadir productos)
- `ProductStatsViewModel` (historial y estadísticas)

---

## 🟠 PROBLEMAS IMPORTANTES

### 4. Screens Demasiado Grandes
| Archivo | Líneas | Recomendación |
|---------|--------|---------------|
| `HistoryScreen.kt` | 831 | Dividir en componentes |
| `ProductListScreen.kt` | 640 | Extraer diálogos |
| `MainScreen.kt` | 607 | Simplificar navegación |
| `AddProductToListDialog.kt` | 512 | Demasiado para un diálogo |

**Solución:** Extraer componentes a archivos separados. Regla general: <300 líneas por archivo.

### 5. TODOs Sin Resolver
| Archivo | Cantidad |
|---------|----------|
| `HistoryRepositoryImpl.kt` | 1+ |
| `ProductListViewModel.kt` | 1+ |
| `MainScreen.kt` | 1+ |
| `ProductListScreen.kt` | 1+ |
| `SupermarketListScreen.kt` | 1+ |
| `ProductHistoryScreen.kt` | 1+ |

**Solución:** Revisar y resolver o eliminar.

### 6. Parámetros No Usados (detectados en compilación)
| Archivo | Parámetro |
|---------|-----------|
| `CommonTopBar.kt:299` | `onProgress` |
| `CatalogoScreen.kt:76` | `onNavigateBack` |
| `CategoriesScreen.kt:27` | `onNavigateBack` |
| `HistoryScreen.kt:56` | `onNavigateBack` |
| `OffersScreen.kt:27` | `onNavigateBack` |
| `ProductListScreen.kt:72` | `onNavigateBack` |
| `ProductListScreen.kt:80` | `isDarkMode` |
| `ProductListScreen.kt:81` | `onToggleDarkMode` |

**Solución:** Eliminar o usar los parámetros.

### 7. Código en Deprecación
| Archivo | Uso Deprecado |
|---------|---------------|
| `CommonTopBar.kt:136` | `Icons.Default.ArrowBack` → usar `Icons.AutoMirrored.Filled.ArrowBack` |
| `CommonTopBar.kt:281` | `Icons.Default.OpenInNew` → versión AutoMirrored |
| `SupermarketAislesScreen.kt:73` | `Icons.Default.Sort` → versión AutoMirrored |
| `EditProductDialog1.kt:316` | `Divider` → `HorizontalDivider` |
| `ListsScreen.kt:237` | `Icons.Default.OpenInNew` → versión AutoMirrored |

---

## 🟡 PROBLEMAS MENORES

### 8. Variables No Usadas
| Archivo | Variable |
|---------|----------|
| `ImagePicker.kt:34` | `showPermissionDialog` |
| `ImagePicker.kt:146` | `context` |
| `PriceHistoryChart.kt:47` | `dates` |
| `SupermarketBottomBar.kt:50` | Condición siempre false |
| `TotalsBar.kt:19` | `totalWithoutOffers` |
| `TotalsBar.kt:71` | `savings` |
| `VoiceCommandParser.kt:58` | `product` |
| `VoiceCommandParser.kt:69` | `group3` |
| `VoiceInputButton.kt:50` | `scope` |
| `VoiceInputButton.kt:149` | `intent` |
| `VoiceInputDialog.kt:46` | `modifier` |
| `VoiceSelectionDialog.kt:28` | `modifier` |
| `SplashScreen.kt:37` | `navController` |
| `AddEditArticuloDialog.kt:69` | `size` |
| `AddEditArticuloDialog.kt:70` | `unit` |
| `CatalogoScreen.kt:166` | `categoryMap` |
| `SupermarketAislesScreen.kt:35` | `scope` |
| `SupermarketAislesScreen.kt:358` | `supermarketId` |
| `BeepHelper.kt:37` | `context` |
| `ShoppingListExporter.kt:19` | `shoppingListRepository` |

### 9. Shadowing de Variables
| Archivo | Problema |
|---------|----------|
| `CommonTopBar.kt:340` | `query` redefine variable |
| `CommonTopBar.kt:341` | `cursor` redefine variable |
| `Converters.kt:35` | `toDomain()` extensión oculta miembro |
| `Converters.kt:91` | `toDomain()` extensión oculta miembro |

### 10. Imports No Usados
Varios archivos tienen imports sin usar. Ejecutar "Optimize Imports" en Android Studio.

---

## 🔵 ARQUITECTURA

### ✅ Aspectos Positivos

1. **Estructura de capas correcta:**
   ```
   data/        → Entities, DAOs, Repositories, Database
   domain/      → Models, Repository interfaces, Use Cases
   ui/          → ViewModels, Screens, Components, Navigation
   di/          → Hilt modules
   ```

2. **Clean Architecture:** 55 Use Cases organizados por dominio.

3. **Inyección de dependencias:** Hilt bien configurado con módulos separados.

4. **Patrón MVVM:** ViewModels exponen StateFlows, Screens observan.

### ⚠️ Aspectos a Mejorar

1. **Falta de Domain Layer completo:** Algunos ViewModels llaman directamente a Repositories.

2. **State hoisting inconsistente:** Algunos Screens manejan estado complejo internamente.

3. **Navegación:** MainScreen.kt tiene lógica de navegación mezclada con UI.

---

## 🔵 COMPOSE BEST PRACTICES

### ✅ Correcto
- Uso de `remember` para operaciones costosas (164 usos)
- `mutableStateOf` para estado local (169 usos)
- `LaunchedEffect` para side effects (70 usos)
- No hay `GlobalScope` ✅

### ⚠️ A Mejorar
- Solo 1 `DisposableEffect` y 1 `onDispose` - revisar si hay recursos que limpiar
- Algunos `remember` sin clave podrían causar recomputaciones innecesarias

---

## 📊 ESTADÍSTICAS

| Métrica | Valor |
|---------|-------|
| Archivos Kotlin | 207 |
| Líneas totales | ~20,000 |
| ViewModels | 8 |
| Use Cases | 55 |
| Screens | 15+ |
| Components | 20+ |
| DAOs | 12 |
| Entities | 13 |
| Warnings compilación | ~55 |

---

## 🎯 PRIORIDAD DE CORRECCIÓN

### Inmediato (esta semana)
1. ✅ Implementar migración 12→13 o documentar que es aceptable
2. ✅ Dividir ProductListViewModel en ViewModels más pequeños

### Corto plazo (2 semanas)
3. Dividir screens grandes en componentes
4. Resolver TODOs pendientes
5. Eliminar parámetros no usados

### Medio plazo (1 mes)
6. Reemplazar APIs deprecadas
7. Habilitar exportSchema para Room
8. Limpiar variables no usadas

---

## 📝 RECOMENDACIONES GENERALES

1. **Establecer límites de líneas por archivo:** Max 300 líneas.

2. **Code coverage:** Añadir tests unitarios para Use Cases y ViewModels.

3. **Lint rules:** Configurar detección automática de código duplicado.

4. **Documentar arquitectura:** Crear ARCHITECTURE.md con decisiones de diseño.

5. **CI/CD:** Añadir verificación de warnings en pipeline.

---

*Auditoría generada el 10/04/2026*
