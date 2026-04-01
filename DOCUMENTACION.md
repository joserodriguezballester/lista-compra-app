# 📚 Documentación Técnica - Lista Compra App

**Última actualización:** 2026-04-01  
**Rama actual:** `main` (merge desde `feature/supermarket-refactor`)

---

## 📋 Índice

1. [Refactorización 2026-03-27](#refactorización-2026-03-27)
2. [Auditoría de Código](#auditoría-de-código)
3. [Problemas Conocidos](#problemas-conocidos)
4. [Arquitectura Detallada](#arquitectura-detallada)
5. [Modelos de Datos](#modelos-de-datos)
6. [Convenciones](#convenciones)

---

## Refactorización 2026-03-27

### Objetivo
Soportar múltiples supermercados con pasillos específicos.

### Cambios realizados

#### Fase 1: Modelos nuevos
| Modelo | Archivo |
|--------|---------|
| `Supermarket` | `domain/model/Supermarket.kt` |
| `Category` | `domain/model/Category.kt` |
| `ArticuloSupermarketDefault` | `domain/model/ArticuloSupermarketDefault.kt` |

#### Fase 2: Modelos modificados
| Modelo | Cambio |
|--------|--------|
| `Aisle` | + `supermarketId: Long` |
| `Product` | + `articuloId: Long?`, + `supermarketId: Long` |

#### Fase 3: Repositorios y UseCases
- ✅ `SupermarketRepository` + `GetAllSupermarketsUseCase`
- ✅ `CategoryRepository` + `GetAllCategoriesUseCase`
- ✅ `GetAislesBySupermarketUseCase`

#### Fase 4: Base de datos
- ✅ Migración 10→11
- ✅ DataSeeder actualizado con 5 supermercados
- ✅ DI Module actualizado

#### Fase 5: UI
- ✅ `SupermarketBottomBar` - chips para seleccionar supermercado
- ✅ `ProductListViewModel` refactorizado con UiState
- ✅ `ProductListScreen` con productos por pasillo
- ✅ Navegación actualizada

### Flujo de la app

```
Splash (2.5s)
    ↓
Home (cards de navegación)
    ├── Mi Lista → ProductListScreen (con BottomBar de supermercados)
    └── Catálogo → CatalogoScreen

ProductListScreen:
├── TopBar: CommonTopBar
├── Lista: productos agrupados por pasillo
├── BottomBar: chips de supermercados
└── FAB: añadir producto
```

---

## Auditoría de Código

### 🔴 CRÍTICO - Resuelto

| Problema | Estado | Solución |
|----------|--------|----------|
| `ProductRepositoryImpl.kt` código muerto | ✅ Resuelto | Eliminado |
| `ShoppingListRepository` accede a 8 DAOs | ✅ Documentado | Rompe SRP, refactorizar en futura versión |

### 🟠 GRAVE - ViewModels sin UseCases

| ViewModel | Estado | Nota |
|-----------|--------|------|
| `ListsManagementViewModel` | ⚠️ Pendiente | Accede a repositorios directamente |
| `SupermarketAislesViewModel` | ⚠️ Pendiente | Accede a repositorios directamente |
| `SupermarketListViewModel` | ⚠️ Pendiente | Accede a repositorios directamente |

**Recomendación:** Crear UseCases específicos para cada ViewModel.

### 🟡 MEDIO - Código duplicado

| Archivo | Problema |
|---------|----------|
| `EditProductDialog1.kt` | Backup, eliminar |
| `ProductCard1.kt` | Backup, eliminar |

### 🔵 MENOR - Nomenclatura inconsistente

| Nombre actual | Debería ser |
|---------------|-------------|
| `CategoryRepository` | `CategoryRepositoryImpl` |
| `SupermarketRepository` | `SupermarketRepositoryImpl` |

---

## Problemas Conocidos

### Bugs Activos

| # | Problema | Causa | Prioridad |
|---|----------|-------|-----------|
| 1 | Historial no asigna pasillo | Datos mockeados | Media |
| 2 | Scanner sin categoría | OpenFoodFacts genérico | Baja |

### Soluciones propuestas

**Bug 1 - Historial:**
- Conectar a `ProductHistoryDao` real
- Incluir `aisleId` en el modelo

**Bug 2 - Scanner:**
- Crear tabla `category_aisle_mapping`
- Diálogo post-escaneo: "¿En qué pasillo está?"

---

## Arquitectura Detallada

### Capas

```
┌─────────────────────────────────────┐
│         UI (Compose)                │
│  screens / components / theme       │
└───────────────┬─────────────────────┘
                │ StateFlow
┌───────────────▼─────────────────────┐
│         ViewModel                    │
│  Expone UiState + eventos           │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│         UseCase                      │
│  Lógica de negocio pura             │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│         Repository (Interface)      │
│  Abstracción de datos              │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│         Data Layer                   │
│  Room (local) / Retrofit (remote)  │
└─────────────────────────────────────┘
```

### Inyección de dependencias (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): IProductRepository
    
    // ... más bindings
}
```

---

## Modelos de Datos

### Product

```kotlin
data class Product(
    val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val shoppingListId: Long = 1,
    val articuloId: Long? = null,      // FK al catálogo
    val supermarketId: Long = 1,
    val quantity: Float = 1f,
    val estimatedPrice: Float? = null,
    val offerId: Long? = null,
    val finalPrice: Float? = null,
    val isPurchased: Boolean = false,
    val notes: String = "",
    val orderIndex: Int = 0,
    val photoUri: String? = null,
    val ean: String? = null,
)
```

### Supermarket

```kotlin
data class Supermarket(
    val id: Long,
    val name: String,        // "Carrefour La Alberca"
    val emoji: String,       // "🛒"
    val isDefault: Boolean = false
)
```

### Aisle

```kotlin
data class Aisle(
    val id: Long,
    val name: String,           // "Lácteos"
    val emoji: String,           // "🥛"
    val orderIndex: Int,         // orden visual
    val supermarketId: Long,     // FK al supermercado
    val isDefault: Boolean = false
)
```

### Category

```kotlin
data class Category(
    val id: Long,
    val name: String,        // "Lácteos"
    val icon: String         // "🥛"
)
```

---

## Convenciones

### Nomenclatura

| Tipo | Formato | Ejemplo |
|------|---------|---------|
| Interfaz repositorio | `I{Nombre}Repository` | `IProductRepository` |
| Implementación | `{Nombre}RepositoryImpl` | `ProductRepositoryImpl` |
| UseCase | `{Verbo}{Sustantivo}UseCase` | `GetAllProductsUseCase` |
| ViewModel | `{Pantalla}ViewModel` | `ProductListViewModel` |
| Screen | `{Nombre}Screen` | `ProductListScreen` |

### Directorios

```
ui/screens/{pantalla}/
├── {Pantalla}Screen.kt      # Composable principal
├── {Pantalla}ViewModel.kt   # ViewModel
└── components/              # Componentes específicos
```

### Git

- **Mensajes:** `tipo: descripción` (ej: `feat: añadir selector de imagen`)
- **Ramas:** `feature/{nombre}`, `fix/{nombre}`, `refactor/{nombre}`

---

## Datos Iniciales

### Supermercados (5)
- Carrefour (🛒) - Por defecto, 19 pasillos específicos
- Mercadona (🏪)
- Lidl (🛍️)
- Aldi (🦘)
- Dia (🏪)

### Categorías (19)
Frutas, Lácteos, Charcutería, Panadería, Bebidas, Higiene, Limpieza, etc.

### Artículos (15+)
Leche, tomates, plátanos, galletas, huevos, sal, azúcar, etc.

---

## Notas de mantenimiento

1. **Pasillos de Carrefour**: Son importantes, NO tocarlos sin consultar
2. **Versión debug**: Usa `.dev` suffix para no sobrescribir la app principal
3. **Migraciones**: Añadir nueva versión de DB si cambian entidades

---

*Documentación generada automáticamente desde análisis del código*
