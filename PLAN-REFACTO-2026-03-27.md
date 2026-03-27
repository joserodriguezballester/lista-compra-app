# Plan Refactorización - 2026-03-27

**Estado:** ✅ COMPLETADO (Fases 1-5)

**Rama:** `feature/supermarket-refactor`

---

## 🎯 Objetivo Principal

Reestructurar la app para soportar múltiples supermercados con pasillos específicos.

---

## ✅ Fases completadas

### Fase 1: Modelos nuevos
| Modelo | Estado | Archivo |
|--------|--------|---------|
| `Supermarket` | ✅ | `domain/model/Supermarket.kt` |
| `Category` | ✅ | `domain/model/Category.kt` |
| `ArticuloSupermarketDefault` | ✅ | `domain/model/ArticuloSupermarketDefault.kt` |
| `SupermarketEntity` | ✅ | `data/local/entities/SupermarketEntity.kt` |
| `CategoryEntity` | ✅ | `data/local/entities/CategoryEntity.kt` |
| `ArticuloSupermarketDefaultEntity` | ✅ | `data/local/entities/ArticuloSupermarketDefaultEntity.kt` |

### Fase 2: Modelos modificados
| Modelo | Cambio | Estado |
|--------|--------|--------|
| `Aisle` | + `supermarketId` | ✅ |
| `Product` | + `articuloId`, `supermarketId` | ✅ |

### Fase 3: Repositorios y UseCases
| Componente | Estado |
|------------|--------|
| `SupermarketRepository` | ✅ |
| `CategoryRepository` | ✅ |
| `ArticuloSupermarketDefaultRepository` | ✅ |
| `GetAllSupermarketsUseCase` | ✅ |
| `GetAislesBySupermarketUseCase` | ✅ |

### Fase 4: Base de datos
| Cambio | Estado |
|--------|--------|
| Migración 10→11 | ✅ |
| DataSeeder actualizado | ✅ |
| DI Module actualizado | ✅ |

### Fase 5: UI
| Pantalla/Componente | Estado |
|---------------------|--------|
| `SupermarketBottomBar` | ✅ |
| `ProductListViewModel` | ✅ |
| `ProductListScreen` | ✅ |
| `HomeScreen` | ✅ |
| `CatalogoScreen` refactorizado | ✅ |
| Navegación actualizada | ✅ |

---

## 🗺️ Flujo de la app

```
Splash (2.5s)
    ↓
Home (cards de navegación)
    ├── Mi Lista → ProductListScreen (con BottomBar de supermercados)
    └── Catálogo → CatalogoScreen

ProductListScreen:
├── TopBar: CommonTopBar
├── Lista: productos agrupados por pasillo
├── TotalsBar: totales y ahorro
├── BottomBar: chips de supermercados
└── FAB: añadir producto
```

---

## 📋 Cambios en modelos

### Supermarket (nuevo)
```kotlin
data class Supermarket(
    val id: Long,
    val name: String,          // "Carrefour La Alberca"
    val emoji: String,         // "🛒"
    val isDefault: Boolean
)
```

### Category (nuevo)
```kotlin
data class Category(
    val id: Long,
    val name: String,          // "Frutas y Verduras"
    val icon: String           // "🍎"
)
```

### Aisle (modificado)
```kotlin
data class Aisle(
    val id: Long,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val supermarketId: Long,   // NUEVO: FK al supermercado
    val isDefault: Boolean
)
```

### Product (modificado)
```kotlin
data class Product(
    // ... campos existentes ...
    val articuloId: Long?,      // NUEVO: FK al artículo del catálogo
    val supermarketId: Long    // NUEVO: FK al supermercado
)
```

---

## ⚠️ Pendiente / Mejoras futuras

1. **Probar compilación** - Muchos cambios, puede haber errores
2. **Migrar datos existentes** - Los pasillos actuales deben asociarse a un supermercado
3. **Integrar FAB de Home y ProductList** - Añadir producto desde catálogo
4. **UI de selección de pasillo inteligente** - Usar `ArticuloSupermarketDefault`
5. **Eliminar ShoppingViewModel** - Ya no se usa
6. **Marcar productos de tienda fija** - Aunque el producto aparezca en la lista de otro supermercado, poder indicar "esto siempre lo compro en X tienda". Útil para cuando vas a varios supermercados y quieres recordar dónde comprar cada cosa

---

*Actualizado: 2026-03-27 04:05*
