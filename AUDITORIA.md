# Auditoría Profunda - Lista Compra App

**Fecha:** 2026-03-30
**Rama:** `feature/supermarket-refactor`
**Archivos analizados:** 167 archivos Kotlin

---

## 🔴 CRÍTICO - Repositorios con Problemas

### ProductRepository - CÓDIGO MUERTO
| Archivo | Estado |
|---------|--------|
| `data/repository/ProductRepository.kt` | ✅ USADO (`@Singleton`, en `DatabaseModule`) |
| `data/repository/ProductRepositoryImpl.kt` | ❌ **CÓDIGO MUERTO** - Eliminar |

**Acción:** Borrar `ProductRepositoryImpl.kt`

### ShoppingListRepository - DISEÑO CONFUSO
| Archivo | Líneas | Propósito |
|---------|--------|-----------|
| `ShoppingListRepository.kt` | 239 | "Repositorio gordo" con 8 DAOs |
| `ShoppingListRepositoryImpl.kt` | 61 | Implementación de `IShoppingListRepository` |

**Problema:** Dos clases con nombres similares pero propósitos diferentes.
- `ShoppingListRepository` NO implementa interfaz
- Rompe principio de responsabilidad única (accede a 8 DAOs)
- Debería refactorizarse o renombrarse

---

## 🟠 GRAVE - Clean Architecture

### ViewModels que acceden directamente a Repositorios (sin UseCases)

| ViewModel | Repositorio usado | Debería usar |
|-----------|-------------------|--------------|
| `ListsManagementViewModel` | `ShoppingListRepository` + `ListPreferences` | UseCases |
| `SupermarketAislesViewModel` | `IAisleRepository` + `ISupermarketRepository` | UseCases |
| `SupermarketListViewModel` | `ISupermarketRepository` | UseCases |

**Problema:** La lógica de negocio está en los ViewModels en lugar de en UseCases.

---

## 🟡 MEDIO - Archivos Duplicados

### BarcodeScannerScreen - DUPLICADO
| Ubicación | Diferencia |
|-----------|------------|
| `ui/screens/BarcodeScannerScreen.kt` | Versión completa con OpenFoodFacts |
| `ui/screens/scanner/BarcodeScannerScreen.kt` | Versión simplificada |

### ArticuloCard - CÓDIGO SIMILAR
| Archivo | Descripción |
|---------|-------------|
| `ui/screens/catalogo/ArticuloCard.kt` | Card normal |
| `ui/screens/catalogo/ArticuloCardGemini.kt` | Card alternativa (posiblemente generated) |

---

## 🟡 MEDIO - Nomenclatura Inconsistente

### Repositorios sin convención
| Nombre actual | Debería ser |
|---------------|-------------|
| `CategoryRepository` | `CategoryRepositoryImpl` |
| `SupermarketRepository` | `SupermarketRepositoryImpl` |
| `ArticuloSupermarketDefaultRepository` | `ArticuloSupermarketDefaultRepositoryImpl` |

**Convención correcta:** `I{Nombre}Repository` (interfaz) → `{Nombre}RepositoryImpl` (implementación)

### Paquetes mezclados
| Problema | Archivos |
|----------|----------|
| `ui/screens/` archivos sueltos | `BarcodeScannerScreen.kt`, `AddProductDialog.kt`, etc. |
| `ui/screens/*/` subcarpetas | `catalogo/`, `scanner/`, etc. |

---

## 🔵 MENOR - Código No Usado / Pendiente

### DAOs con tablas que no se poblan
| DAO | Tabla | ¿Se usa? |
|-----|-------|----------|
| `ProductFrequencyDao` | `product_frequency` | ⚠️ Verificar |
| `ProductPriceHistoryDao` | `product_price_history` | ⚠️ Verificar |
| `PurchaseHistoryDao` | `purchase_history` | ⚠️ Verificar |
| `OfferDao` | `offers` | ⚠️ Verificar |

### UseCases no usados desde ViewModels
| UseCase | ¿Se usa? |
|---------|----------|
| `InitializeAislesUseCase` | ✅ En `ListsManagementViewModel` |
| `GetCategoriesOrderedForSupermarketUseCase` | ⚠️ Verificar |
| `UpdateCategoryOrderForSupermarketUseCase` | ⚠️ Verificar |
| `CompletePurchaseUseCase` | ⚠️ Verificar |

---

## 📊 Resumen de Problemas

| Categoría | Cantidad | Prioridad |
|-----------|----------|-----------|
| Repositorios duplicados | 2 | 🔴 CRÍTICO |
| ViewModels sin UseCases | 3 | 🟠 GRAVE |
| Archivos duplicados | 2 | 🟡 MEDIO |
| Nomenclatura inconsistente | 5+ | 🟡 MEDIO |
| DAOs posiblemente no usados | 4+ | 🔵 MENOR |

---

## 🛠️ Plan de Acción Sugerido

### Fase 1: CRÍTICO (bloquea compilación)
1. Eliminar `ProductRepository.kt` (mantener `ProductRepositoryImpl.kt`)
2. Eliminar `ShoppingListRepository.kt` (mantener `ShoppingListRepositoryImpl.kt`)
3. Actualizar referencias en `DatabaseModule.kt`

### Fase 2: Clean Architecture
4. Crear UseCases para `SupermarketAislesViewModel`
5. Crear UseCases para `SupermarketListViewModel`
6. Crear UseCases para `ListsManagementViewModel`

### Fase 3: Limpieza
7. Eliminar `ui/screens/BarcodeScannerScreen.kt` (mantener `scanner/BarcodeScannerScreen.kt`)
8. Decidir qué `ArticuloCard` mantener
9. Renombrar repositorios para consistencia (`*Impl`)

### Fase 4: Verificación
10. Revisar qué DAOs/UseCases realmente se usan
11. Eliminar código muerto

---

*Auditoría completada - Sin cambios realizados*
