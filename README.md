# 🛒 Lista Compra - Android App

**Plataforma:** Android Nativo (Kotlin)  
**Almacenamiento:** Local (Room Database) + Export/Import JSON  
**Usuario:** Jose (Xoce)  
**Fecha:** 2026-02-26

---

## 📱 Características

### ✅ Implementado (MVP Listo)
- ✅ Lista de productos organizada por pasillos (orden Mislata Edition)
- ✅ Añadir/quitar productos
- ✅ Marcar como comprado (checkbox)
- ✅ Calcular total estimado
- ✅ Gestión de pasillos (añadir nuevos pasillos)
- ✅ Exportar lista a JSON
- ✅ Importar lista desde JSON
- ✅ Pasillos por defecto (19 pasillos del super de Mislata)
- ✅ **Autocompletado inteligente** (sugiere pasillo y precio basado en historial)
- ✅ **Precarga inicial** (tus productos habituales de Carrefour ya están en la app)

### 🔄 Futuras mejoras (v2)
- [ ] Añadir productos por voz
- [ ] **Lectura de código de barras** (escanear EAN para autocompletar producto)
- [ ] **Añadir imágenes a los productos** (foto desde cámara o galería) ← NUEVO (27/02)
- [ ] Reordenar pasillos (drag & drop)
- [ ] Histórico de compras con gráficos
- [ ] Fotos de productos

---

## 🗺️ Pasillos por Defecto

1. 🧴 Higiene y Belleza
2. 🍎 Fruta y Verdura
3. 🥓 Charcutería
4. 🥩 Carnicería
5. 🥫 Despensa - Pasillo 1: Galletas
6. 🥫 Despensa - Pasillo 2: Chocolates
7. 🥫 Despensa - Pasillo 3: Azúcar y Café
8. 🥫 Despensa - Pasillo 4: Tomate Frito y Legumbres
9. 🥫 Despensa - Pasillo 5: Aceite y Pastas
10. 🧻 Papel
11. 🧼 Droguería y Limpieza
12. 🥤 Bebidas
13. 🥜 Papas y Snacks
14. 🥐 Bollería
15. 🥛 Lácteos
16. 🥪 Preparados
17. 🧀 Quesos
18. 🎁 Regalo (fidelización)
19. 🧊 Congelados

---

## 🎮 Cómo Usar la App

### 📱 Pantalla Principal

**¿Se guardan los productos?** ✅ **SÍ.** Todo se guarda en la base de datos del móvil. Cuando vuelvas a abrir la app, tus productos seguirán ahí.

Al abrir la app verás tu lista organizada por **pasillos del supermercado**:

```
┌─────────────────────────────┐
│  🛒 Lista de Compra         │
├─────────────────────────────┤
│ 🧴 HIGIENE Y BELLEZA        │ ← Nombre del pasillo
│   ☐ Champú             🗑️   │ ← Producto (tocar ☐ para marcar)
│   ☑ Gel de ducha       🗑️   │ ← ☑ = Comprado (tachado)
├─────────────────────────────┤
│ 🥫 DESPENSA - GALLETAS      │
│   ☐ Galletas María     🗑️   │
│   ☐ Digestives         🗑️   │
├─────────────────────────────┤
│         [ + ]               │ ← Botón flotante (añadir)
├─────────────────────────────┤
│ 2 de 4 productos    12.50€  │ ← Total estimado
└─────────────────────────────┘
```

### ➕ Añadir un Producto

**¿Dónde se guarda?** En la **base de datos del móvil** (Room). No se pierden al cerrar la app.

1. Toca el botón **+** (abajo derecha)
2. Escribe el **nombre** del producto (ej: "Leche entera")
3. Selecciona el **pasillo** donde está (ej: 🥛 Lácteos)
4. (Opcional) Pon **cantidad** (ej: 2 unidades)
5. (Opcional) Pon **precio estimado** (ej: 1.20€)
6. Toca **"Añadir"**

✅ **El producto aparece en la lista principal** y se guarda automáticamente.

### ✅ Marcar como Comprado

- Toca el **cuadrado ☐** a la izquierda del producto
- Se marcará como **☑** y aparecerá tachado
- El contador abajo se actualiza automáticamente

### 🗑️ Eliminar un Producto

- Toca el **icono de papelera 🗑️** a la derecha del producto
- ¡Desaparece de la lista!

### 🏪 Gestionar Pasillos

Quieres añadir tu propio pasillo (ej: "Chucherías"):

1. Toca el **menú ⋮** (arriba derecha)
2. Selecciona **"Gestionar pasillos"**
3. Toca **"Nuevo pasillo"**
4. Pon nombre: "Chucherías"
5. Pon emoji: 🍬
6. Toca **"Añadir"**

**Para eliminar un pasillo:**
- En la misma pantalla, toca la **🗑️** del pasillo que quieres quitar
- **Nota:** Los pasillos por defecto no se pueden borrar

### 💾 Exportar tu Lista (Backup)

Para guardar tu lista o pasarla a otro móvil:

```kotlin
// En el código (versión futura tendrá botón en UI)
val json = viewModel.exportToJson()
// Guardar en archivo o compartir
```

### 🚀 Precarga Inicial (Tus Productos de Carrefour)

**¿La app ya viene con datos?** ✅ **SÍ.** La primera vez que abres la app, ya tiene precargados tus productos habituales de Carrefour:

- 🥛 **Leche** → Lácteos, 6 unidades, 1.15€
- 🍎 **Frutas y verduras** → Tomates, plátanos, manzanas...
- 🥓 **Charcutería** → Taquitos de jamón/chorizo, huevos
- 🥫 **Despensa** → Galletas, sal, azúcar, fideuá
- 🧀 **Quesos** → Fresco, rallado
- 🥤 **Bebidas** → Zumo, gaseosas
- 🥪 **Preparados** → Capuchinos, pizza
- 🧴 **Higiene** → Máquina de afeitar

**Total:** 30+ productos preconfigurados

**¿Para qué sirve?**
- Escribe **"Leche"** → sugiere automáticamente: 🥛 Lácteos, 6 uds, 1.15€
- Escribe **"Tomates"** → sugiere: 🍎 Fruta/Verdura, 1 ud
- **No tienes que configurar nada**, la app ya "te conoce"

---

### 💾 ¿Se Borran los Productos al Cerrar la App?

**¡NO!** Tus datos se guardan automáticamente:

- ✅ **Base de datos local** (Room) en el móvil
- ✅ **Sin internet** (todo offline)
- ✅ **Persistente** (cierras la app → vuelves → todo sigue ahí)
- ✅ **Solo se borra** si tú pulsas la papelera 🗑️

**Ejemplo:**
1. Añades "Leche" hoy
2. Cierras la app
3. Mañana la abres → **La leche sigue en la lista**

---

### 📥 Importar una Lista

Para cargar una lista guardada:

```kotlin
// En el código (versión futura tendrá botón en UI)
viewModel.importFromJson(jsonString)
```

---

## 🚀 Compilar y Ejecutar

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 34
- Kotlin 1.9+

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/joserodriguezballester/lista-compra-app.git
cd lista-compra-app

# 2. Compilar
./gradlew build

# 3. Instalar en dispositivo/emulador
./gradlew installDebug
```

### O desde Android Studio
1. Abrir proyecto en Android Studio
2. Click en `Sync Project with Gradle Files`
3. Click en `Run 'app'` (▶️)

---

## 🏗️ Arquitectura

```
MVVM Pattern
├── UI Layer (Compose)
│   ├── screens/
│   │   ├── MainScreen.kt
│   │   ├── AddProductDialog.kt
│   │   └── ManageAislesDialog.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── Domain Layer
│   ├── models/
│   │   ├── Product.kt
│   │   ├── Aisle.kt
│   │   └── ShoppingList.kt
│   └── repository/
│       └── ShoppingListRepository.kt
└── Data Layer
    └── local/
        └── Database.kt (Room)
```

---

## 📦 Tech Stack

| Componente | Librería |
|------------|----------|
| Lenguaje | Kotlin 1.9+ |
| UI | Jetpack Compose |
| Database | Room 2.6.1 |
| Arquitectura | MVVM |
| Navegación | Compose Navigation |
| JSON | Gson |
| Build | Gradle 8.2 |

---

## 📁 Estructura del Proyecto

```
lista-compra-app/
├── app/
│   ├── src/main/java/com/jose/listacompra/
│   │   ├── ListaCompraApplication.kt
│   │   ├── data/
│   │   │   ├── local/Database.kt
│   │   │   └── repository/ShoppingListRepository.kt
│   │   ├── domain/
│   │   │   └── model/
│   │   │       ├── Product.kt
│   │   │       ├── Aisle.kt
│   │   │       └── ShoppingList.kt
│   │   └── ui/
│   │       ├── MainActivity.kt
│   │       ├── screens/
│   │       │   ├── MainScreen.kt
│   │       │   ├── AddProductDialog.kt
│   │       │   └── ManageAislesDialog.kt
│   │       ├── theme/
│   │       │   ├── Color.kt
│   │       │   ├── Theme.kt
│   │       │   └── Type.kt
│   │       └── viewmodel/
│   │           └── ShoppingListViewModel.kt
│   └── src/main/res/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml
│       │   └── themes.xml
│       └── xml/
│           ├── backup_rules.xml
│           └── data_extraction_rules.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew
```

---

## 🔧 Configuración

### Cambiar pasillos por defecto
Editar: `app/src/main/java/com/jose/listacompra/domain/model/Aisle.kt`

```kotlin
companion object {
    fun getDefaultAisles(): List<Aisle> = listOf(
        Aisle(1, "Tu pasillo", "🎯", 0),
        // ... más pasillos
    )
}
```

---

## 📝 Changelog

### v1.0.0 (2026-02-26)
- ✅ Primera versión funcional
- ✅ Gestión completa de lista de compra
- ✅ 19 pasillos por defecto
- ✅ Export/Import JSON

---

## 📄 Licencia

Privado - Uso personal

---

*Desarrollado para Jose (Xoce) - Mislata, Valencia*
