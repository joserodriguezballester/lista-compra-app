# 🎮 Especificaciones de Interacción (UI/UX)

**Fecha:** 2026-02-27  
**Decisión:** Sistema de gestos para la lista de productos

---

## 📱 Modelo de Interacción Seleccionado

### Opción elegida:
**✅ CHECKBOX + TOQUE LARGO + SWIPE**

---

## 🎯 Gestos Definidos

| Zona | Gesto | Acción | Prioridad |
|------|-------|--------|-----------|
| **Checkbox ☑** | Toque simple | Marcar/Desmarcar como comprado | **ALTA** (captura primero) |
| **Card/Nombre** | Toque simple | También marca (opcional/redundante) | MEDIA |
| **Card (fuera checkbox)** | Toque largo | Abrir diálogo de edición | ALTA |
| **Card completa** | Swipe ← | Borrar producto | MEDIA |

---

## 🖼️ Comportamiento Visual

```
┌────────────────────────────┐  ← Card con combinedClickable
│ ☑                          │  ← Checkbox independiente (clickable)
│                            │
│   Leche semidesnatada      │  ← Nombre (hereda click de card)
│                            │
│   6 uds · 1.15€            │
└────────────────────────────┘
       ↑
    Swipe left = 🗑️ Borrar
```

---

## 📋 Detalles Técnicos

### Checkbox
- Siempre **visible** y funcional
- Ocupa espacio fijo a la izquierda
- Color: `MaterialTheme.colorScheme.primary` cuando marcado
- Toque inmediato (sin delay)

### Toque Largo (Long Press)
- Duración: 400-500ms (estándar Android)
- Feedback: Vibración ligera + highlight visual
- Acción: Abrir `EditProductDialog` con:
  - Precio editable
  - Campo "Notas/oferta" (ej: "2x1", "caduca pronto")
  - Cambiar cantidad
  - Mover a otro pasillo

### Swipe para Borrar
- Dirección: Izquierda → Derecha (o viceversa, definir)
- Background rojo con icono 🗑️ durante el swipe
- Confirmación: **NO** (se borra directamente, puede deshacerse con Snackbar)
- Snackbar: "Producto eliminado" + botón "DESHACER"

---

## 🎨 Estados del Producto

### No comprado:
```
☐ Leche semidesnatada
   6 uds · 1.15€
```
- Texto: Negro, normal
- Checkbox: Vacío ☐

### Comprado:
```
☑ Leche semidescatada  (tachado)
   6 uds · 1.15€       (gris, sin tachar)
```
- Nombre: Gris + `TextDecoration.LineThrough`
- Cantidad/Precio: Gris pero **sin** tachar (se lee mejor)
- Checkbox: Marcado ☑

### Con nota/oferta:
```
☐ Leche semidesnatada
   6 uds · 1.15€  🏷️ 2x1
```
- Badge pequeño 🏷️ o 📋 junto al precio
- Indica que hay información extra

---

## 🛠️ Implementación (Compose)

### Componentes necesarios:
1. `Card` con `combinedClickable()`
2. `Checkbox` dentro (independiente)
3. `Dismissible` o `SwipeToDismiss` wrapper
4. `AnimatedVisibility` para efectos

### Librerías:
```kotlin
// Ya incluidas en build.gradle:
androidx.compose.material3
androidx.compose.foundation
```

### Código esqueleto:
```kotlin
SwipeToDismiss(
    state = dismissState,
    background = { DeleteBackground() },
    dismissContent = {
        Card(
            modifier = Modifier.combinedClickable(
                onClick = { togglePurchased() },
                onLongClick = { showEditDialog() }
            )
        ) {
            Row {
                Checkbox(
                    checked = isPurchased,
                    onCheckedChange = { togglePurchased() }
                )
                Column {
                    Text(name)
                    Text("$quantity uds · ${price}€")
                }
            }
        }
    }
)
```

---

## ⚠️ Consideraciones UX

1. **Descubribilidad del toque largo:**
   - Añadir hint visual primera vez: "Mantén pulsado para editar"
   - O mostrar tooltip después de 3 usos sin editar

2. **Swipe accidental:**
   - Umbral de swipe: 25% de ancho (no demasiado sensible)
   - Snackbar con "Deshacer" obligatorio

3. **Accesibilidad:**
   - ContentDescription para cada elemento
   - Soporte para TalkBack (lectores de pantalla)

---

## 🔄 Alternativas descartadas (para referencia)

| Opción | Por qué no | 
|--------|-----------|
| Solo toque largo (sin checkbox) | Poco descubrible, lento para marcar |
| Menú contextual (toque largo con opciones) | Un paso más para borrar |
| Doble toque para editar | No estándar en Android |
| Botón ✏️ visible siempre | Ocupaba mucho espacio en cards pequeñas |

---

## ✅ Checklist para implementación

- [ ] Implementar `combinedClickable` en Card
- [ ] Implementar `SwipeToDismiss` wrapper
- [ ] Crear `EditProductDialog` con campos: precio, notas, cantidad, pasillo
- [ ] Añadir Snackbar "Deshacer" al borrar
- [ ] Feedback táctil (vibración) en toque largo
- [ ] Hint educativo primera vez
- [ ] Probar accesibilidad con TalkBack

---

---

## 🏷️ Sistema de Ofertas (NUEVO - 2026-02-27)

### Modelo de Datos

#### Tabla `offers` (Tipos de Oferta)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long (PK) | ID único |
| code | String | Código corto: "3x2", "2nd_50", "custom" |
| name | String | Nombre visible: "3x2", "2ª unidad -50%" |
| description | String | Descripción larga |
| isDefault | Boolean | true = predefinida, false = custom del usuario |
| formula | String | Fórmula de cálculo (para evaluar) |

**Ofertas predefinidas:**
- `3x2` → Lleva 3, paga 2
- `2x1` → Lleva 2, paga 1
- `2nd_50` → 2ª unidad 50% descuento
- `2nd_70` → 2ª unidad 70% descuento
- `4x3` → Lleva 4, paga 3
- `custom` → Personalizado (usuario introduce cálculo)

#### Tabla `products` (modificada)

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val aisleId: Long,
    val quantity: Float,
    val unitPrice: Float?,           // Precio unitario normal
    val offerId: Long?,              // FK a offers (nullable)
    val finalPrice: Float?,          // Calculado automáticamente
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int
)
```

### Cálculo de Precios

```kotlin
fun calculateFinalPrice(quantity: Float, unitPrice: Float, offer: Offer?): Float {
    return when (offer?.code) {
        "3x2" -> {
            val groups = (quantity / 3).toInt()
            val remainder = quantity % 3
            (groups * 2 + remainder) * unitPrice
        }
        "2nd_50" -> {
            val pairs = (quantity / 2).toInt()
            val remainder = quantity % 2
            (pairs * 1.5f + remainder) * unitPrice
        }
        "2nd_70" -> {
            val pairs = (quantity / 2).toInt()
            val remainder = quantity % 2
            (pairs * 1.3f + remainder) * unitPrice  // 100% + 30%
        }
        "2x1" -> {
            val groups = (quantity / 2).toInt()
            val remainder = quantity % 2
            (groups * 1 + remainder) * unitPrice
        }
        "4x3" -> {
            val groups = (quantity / 4).toInt()
            val remainder = quantity % 4
            (groups * 3 + remainder) * unitPrice
        }
        else -> quantity * unitPrice  // Sin oferta o custom manual
    }
}
```

### UI - Añadir/Editar Producto con Oferta

```
┌─────────────────────────────┐
│ Nombre: [Leche      ]       │
│ Cantidad: [3        ]       │
│ Precio ud: [1.15    ]       │
│                             │
│ Oferta: [3x2        ▼]      │  ← Dropdown con ofertas
│                             │
│ ┌─────────────────────────┐ │
│ │ 🏷️ OFERTA APLICADA     │ │
│ │                         │ │
│ │ Sin oferta:   3.45€    │ │
│ │ Con oferta:   2.30€    │ │  ← Precio final
│ │ Ahorras:      1.15€ ✅ │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

### UI - Lista con Ofertas

```
┌────────────────────────────┐
│ ☑ Leche semidesnatada 🏷️  │  ← Badge 🏷️ si tiene oferta
│    3 uds · 2.30€           │  ← Precio final (no unitario)
│    🏷️ OFERTA: 3x2          │  ← Indicador de oferta
└────────────────────────────┘
```

### CRUD de Ofertas (Pantalla Admin)

Accesible desde: Menú ⋮ → "Gestionar ofertas"

**Listado:**
- Ofertas predefinidas (solo lectura)
- Ofertas personalizadas (editables/eliminables)

**Añadir oferta personalizada:**
- Nombre: "Mi oferta"
- Descripción: "Explicación"
- Tipo: Porcentaje / Fijo / Formula custom

### Actualización Automática

Cuando el usuario cambie:
- Cantidad → Recalcular finalPrice
- Oferta → Recalcular finalPrice
- Precio unitario → Recalcular finalPrice

### Total de la Lista

```kotlin
// En el bottom bar:
val totalWithoutOffers = products.sumOf { it.quantity * (it.unitPrice ?: 0f) }
val totalWithOffers = products.sumOf { it.finalPrice ?: (it.quantity * (it.unitPrice ?: 0f)) }
val savings = totalWithoutOffers - totalWithOffers

// Mostrar:
"Total: 45.20€"           // Con ofertas aplicadas
"Ahorrado: 8.50€ 🎉"      // Si savings > 0
```

---

---

## 👻 Card "Fantasma" para Productos Comprados (NUEVO - 2026-02-27)

### Estado Visual del Producto

| Estado | Apariencia |
|--------|------------|
| **No comprado** | Card normal, opacidad 100%, info completa |
| **Comprado** | Card "fantasma", opacidad 40%, info reducida |

### Card NO COMPRADO (normal):
```
┌─────────────────────────┐
│ ☐                       │
│   LECHE SEMIDESNATADA   │  ← Nombre bold, negro
│                         │
│   🏷️ OFERTA: 3x2        │  ← Oferta visible
│                         │
│   6 uds │ 1.15€ │ 2.30€ │  ← Datos completos
└─────────────────────────┘
```

### Card COMPRADO (fantasma):
```
┌─────────────────────────┐
│ ☑ ✓                     │  ← Checkbox marcado + check extra
│   LECHE SEMIDESNATADA   │  ← Nombre tachado, gris
│   (comprado)            │  ← Label pequeño "comprado"
│                         │
│                         │  ← Sin datos de precio/cantidad
└─────────────────────────┘
```

### Especificaciones Técnicas

**Opacidad y Color:**
- `alpha: 0.4f` (40% opacidad)
- Background: `MaterialTheme.colorScheme.surfaceVariant`
- Texto: `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)`
- TextDecoration: `LineThrough` en el nombre

**Elementos que DESAPARECEN al comprar:**
- Cantidad / Precio / Total
- Badge de oferta 🏷️
- Botón de papelera (se usa swipe para borrar)

**Elementos que permanecen:**
- Checkbox marcado ☑
- Nombre del producto (tachado)
- Card clicable (para desmarcar si error)

### Comportamiento

```kotlin
if (product.isPurchased) {
    // Card "fantasma"
    Card(
        modifier = Modifier.alpha(0.4f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = product.name,
            textDecoration = TextDecoration.LineThrough,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        // Sin precios ni ofertas
    }
} else {
    // Card normal (con todos los datos)
}
```

### Razones de diseño

- ✅ **Limpieza visual:** Los comprados "desaparecen" del foco
- ✅ **Histórico visible:** Sigues viendo qué ya cogiste
- ✅ **Deshacer fácil:** Tocas el fantasma y vuelve a la vida
- ✅ **Sin perder info:** Si te equivocas, ves qué era

---

---

## 🧮 Calculadora Comparadora de Precios (NUEVO - 2026-02-27)

### Descripción
Herramienta standalone para comparar dos productos en oferta y ver cuál sale más barato por unidad.
**NO guarda en base de datos**, es calculadora rápida.

### UI - Pantalla Comparador

```
┌─────────────────────────────────┐
│ 🔍 COMPARADOR DE PRECIOS    ✕   │  ← Botón cerrar
├─────────────────────────────────┤
│                                 │
│  PRODUCTO A                     │
│  ┌─────────────────────────┐    │
│  │ Oferta: [3x2        ▼]  │    │  ← Selector de ofertas BD
│  │ Cantidad: [100      ]   │    │
│  │ Unidad: [gramos    ▼]   │    │  ← gr, ml, lavados, ud
│  │ Precio: [6        ] €   │    │
│  └─────────────────────────┘    │
│                                 │
│  💰 4.00 €/100g                 │  ← Precio por unidad calculado
│  📋 Paga: 12€, Llevas: 300g     │  ← Resumen oferta
│                                 │
├─────────────────────────────────┤
│                                 │
│  PRODUCTO B                     │
│  ┌─────────────────────────┐    │
│  │ Oferta: [2x1        ▼]  │    │
│  │ Cantidad: [150      ]   │    │
│  │ Unidad: [gramos    ▼]   │    │
│  │ Precio: [9        ] €   │    │
│  └─────────────────────────┘    │
│  🟢 FONDO VERDE CLARO           │  ← Winner!
│                                 │
│  💰 3.00 €/100g  ✅ MEJOR       │
│  📋 Paga: 9€, Llevas: 300g      │
│                                 │
├─────────────────────────────────┤
│  📊 Ahorras: 3.00 €             │
│                                 │
│  [🔄 NUEVA COMPARACIÓN]         │
└─────────────────────────────────┘
```

### Restricciones de Ofertas

Cada oferta tiene restricciones automáticas:

| Oferta | Restricción | Validación |
|--------|-------------|------------|
| **3x2** | Cantidad mínima: 3 | Alerta si < 3 |
| **2x1** | Cantidad mínima: 2 | Alerta si < 2 |
| **2ª -50%** | Cantidad mínima: 2 | Alerta si < 2 |
| **4x3** | Cantidad mínima: 4 | Alerta si < 4 |
| **-20%** | Sin mínimo | - |

**Mensaje de error:**
```
⚠️ La oferta 3x2 requiere mínimo 3 unidades
   Tienes: 2
```

### Cálculos Automáticos

**Precio por unidad:**
```kotlin
// Fórmula base
val totalUnits = cantidad * unidadBase
val finalPrice = calcularOferta(offerType, cantidad, precio)
val pricePerUnit = finalPrice / totalUnits

// Ejemplo:
// Café: 150g, 2x1, 9€
// Paga: 9€ (1 gratis)
// Llevas: 300g (2 x 150g)
// Precio/g: 9€ / 300g = 0.03 €/g = 3€/100g
```

**Visualización del cálculo:**
```
📋 Detalle:
   Precio normal: 18€ (2 x 9€)
   Oferta aplicada: -9€ (2x1)
   Pagas: 9€
   Cantidad real: 300g (2 x 150g)
   Precio/100g: 3.00€
```

### Unidades Soportadas

| Unidad | Uso típico | Ejemplo |
|--------|------------|---------|
| **gramos (g)** | Comida, café | Café 100g |
| **kilogramos (kg)** | Arroz, legumbres | Arroz 1kg |
| **mililitros (ml)** | Bebidas, detergente | Cola 2L = 2000ml |
| **litros (L)** | Leche, agua | Leche 1.5L |
| **lavados** | Detergente, suavizante | Detergente 30 lavados |
| **unidades (ud)** | Yogures, latas | Yogures pack 8 |
| **metros (m)** | Papel higiénico | Papel 50m |
| **hojas** | Servilletas, pañuelos | Servilletas 100 hojas |

**Conversión automática:**
- Si Producto A es en gramos y B en kg → convierte todo a gramos
- Resultado siempre en la unidad más pequeña (para precisión)

### Visualización del Ganador

**Producto A más barato:**
- Card A: Fondo verde claro (`Color.Green.copy(alpha = 0.1f)`)
- Card B: Fondo normal
- Badge: "✅ MEJOR OPCIÓN" en verde

**Empate:**
- Ambos: Fondo amarillo muy claro
- Badge: "⚖️ IGUAL DE BARATOS"

**Diferencia grande (>50%):**
- Badge extra: "🔥 GRAN AHORRO"

### Acceso desde la App

**Opción 1:** FAB (botón +) → Menú desplegable:
- "Añadir producto"
- "Comparar precios" ← Aquí

**Opción 2:** Menú ⋮ → "Calculadora de ofertas"

**Opción 3:** Icono calculadora en la toolbar

### Persistencia Temporal

**NO guarda en BD**, pero:
- Mantiene valores al rotar pantalla (ViewModel)
- "Historial de comparaciones" en memoria (últimas 5)
- Botón "Usar estos datos" → Rellena formulario de añadir producto

---

**Decisión tomada por:** Jose (Xoce)  
**Fecha:** 2026-02-27  
**Estado:** PENDIENTE DE IMPLEMENTACIÓN

---

## 📋 FAENA PENDIENTE (TODO List)

### ✅ Prioridad ALTA (MVP)
- [ ] Implementar estructura base de datos (Room)
- [ ] Crear modelos: Product, Aisle, Offer, ProductHistory
- [ ] Pantalla principal con Grid de productos
- [ ] Card "fantasma" para productos comprados
- [ ] Checkbox + Toque largo + Swipe para borrar
- [ ] Añadir producto con autocompletado
- [ ] Precarga inicial con datos de Carrefour
- [ ] Exportar/Importar JSON

### 🔄 Prioridad MEDIA (Post-MVP)
- [ ] Sistema de ofertas (3x2, 2x1, 2ª-50%)
- [ ] Calcular precio final automáticamente
- [ ] Mostrar "Ahorrado: X€" en el total
- [ ] Gestión de pasillos (añadir/reordenar)
- [ ] **Calculadora comparadora de precios** ← NUEVO (27/02)
- [ ] Selección de ofertas con restricciones
- [ ] Unidades: gramos, lavados, litros, etc.
- [ ] Visualización "más barato" en verde

### 🔮 Prioridad BAJA (Futuro)
- [ ] **Añadir productos por voz** ← NUEVO (27/02)
- [ ] **Lectura de código de barras** (escanear EAN y buscar en Open Food Facts) ← NUEVO (27/02)
- [ ] **Añadir imágenes a los productos** (foto desde cámara o galería) ← NUEVO (27/02)
- [ ] Histórico de compras con gráficos
- [ ] Sincronización entre dispositivos
- [ ] Widget para pantalla de inicio
- [ ] Modo oscuro personalizado

### ✅ Tests Unitarios (JUnit)
- [x] OfferCalculationTest - Cálculo de ofertas (3x2, 2x1, 2ª-50%, etc.)
- [x] DomainModelTest - Modelos Product, Aisle, precios
- [x] JsonExportTest - Exportación/importación JSON
- [ ] RepositoryTest - Test de base de datos (Room)
- [ ] ViewModelTest - Lógica de UI

---

**Última actualización:** 2026-02-27 04:20  
**Próxima revisión:** Cuando Jose habilite GitHub

---

## 🧪 Tests y Verificación

### Script de Verificación Automática

**Archivo:** `verify-app.py`

Ejecutar antes de compilar:
```bash
cd ~/private-users/Jose/proyectos/lista-compra-app
python3 verify-app.py
```

**Qué verifica:**
- ✅ Archivos esenciales existen
- ✅ Sintaxis Kotlin (llaves/paréntesis balanceados)
- ✅ Configuración Room (@Entity, @Dao)
- ✅ Dependencias Gradle críticas
- ⚠️ Imports posiblemente sin usar (falsos positivos)

**Resultado:**
- ❌ ERRORES → Corregir antes de compilar
- ⚠️ ADVERTENCIAS → Revisar, pero puede compilar
- ✅ OK → Listo para `./gradlew build`
