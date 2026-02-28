# 🌐 FASE 2 - DISEÑO RED Y COMPARTIR

**Estado:** Documentado, pendiente de implementación  
**Tecnología:** Supabase (nube) → Futuro: servidor propio de Cenito  
**Fecha diseño:** 2026-02-28

---

## 📋 RESUMEN DE DECISIONES

| Aspecto | Decisión | Notas |
|---------|----------|-------|
| **Unirse a familia** | PIN (6 dígitos) + QR | Ambos métodos disponibles. Sin aprobación, entra directo |
| **Compartir listas** | Lista por lista | El usuario elige qué lista compartir, no todas automáticamente |
| **Aprobación miembros** | No | Con PIN/QR válido, entra inmediatamente |
| **Roles** | Admin / Editor / Lector | Permisos diferenciados |
| **Modo offline** | Sí | Guarda local, sincroniza cuando hay red |
| **Conflictos** | Último gana | O mostrar aviso de conflicto |

---

## 📱 ESTRUCTURA DEL MENÚ

```
Menú ⋮ → "🌐 Mi Red"
         ├── "👥 Familia" (ver miembros conectados)
         ├── "📋 Compartir lista" (elige lista a compartir)
         ├── "🔗 Invitar a familia" (generar PIN o QR)
         └── "⚙️ Configuración de sincronización"
```

---

## 🎯 FUNCIONALIDADES DETALLADAS

### 1. CREAR/UNIRSE A FAMILIA

#### **Método A: PIN Numérico (fácil para la yaya)**
```
Crear familia:
1. Nombre: "Casa Jose"
2. Genera PIN automático: 847291
3. Muestras el PIN a la yaya
4. Ella va a "Unirse a familia" → Introduce PIN 847291
5. ¡Listo! Ahora comparten listas
```

#### **Método B: Código QR (más moderno)**
```
Crear familia:
1. Genera código QR en pantalla
2. La yaya abre cámara o escáner QR
3. Escanea el código
4. Se une automáticamente
```

**Nota:** Ambos métodos disponibles. La yaya puede usar el que prefiera.

---

### 2. COMPARTIR LISTAS (LISTA POR LISTA)

**NO** todas las listas se comparten automáticamente. El usuario elige:

```
Lista "Carrefour" → Menú ⋮ → "📤 Compartir"
                    ├── ✅ Compartir con familia
                    ├── 👤 Solo yo (privada)
                    └── ❌ Dejar de compartir
```

**Visualización en app:**
- Listas compartidas: Icono 🌐 junto al nombre
- Listas privadas: Sin icono

**Ejemplo de uso:**
- Lista "Carrefour" → Compartida con familia 🌐
- Lista "Compra secreta cumple yaya" → Privada 👤 (solo yo)
- Lista "Lidl" → Compartida con familia 🌐

---

### 3. PANTALLA "👥 MIEMBROS DE LA FAMILIA"

```
┌─────────────────────────────┐
│  👥 Mi Familia              │
├─────────────────────────────┤
│ 👤 Jose (Tú) - Admin        │
│ 👤 Yaya - Editor            │
│ 👤 OtroHermano - Editor     │
│                             │
│ [🔗 Invitar más]            │
│ [⚙️ Gestionar permisos]     │
└─────────────────────────────┘
```

#### **Roles y Permisos:**

| Rol | Permisos |
|-----|----------|
| **Admin** | Todo + expulsar miembros + cambiar roles |
| **Editor** | Añadir/quitar productos, marcar comprado, crear listas |
| **Lector** | Solo ver listas, no modificar nada |

**Caso de uso:**
- Jose (Admin): Control total
- Yaya (Editor): Puede añadir cosas a la lista
- Hermano pequeño (Lector): Solo ve, no toca

---

### 4. SINCRONIZACIÓN

#### **Tiempo real (o casi)**
- Jose añade "Leche" → Aparece en el móvil de la yaya en segundos
- Yaya marca "Pan" como comprado → Se marca también en el móvil de Jose

#### **Modo Offline**
```
Sin internet:
1. Jose añade "Leche" (se guarda local)
2. Sube a la nube cuando recupera conexión
3. La yaya lo recibe cuando ella tenga internet
```

#### **Conflictos (resolución)**
- **Caso:** Jose y Yaya editan el mismo producto a la vez
- **Solución:** Gana el último en guardar (timestamp)
- **Alternativa:** Mostrar aviso "Conflicto, ¿cuál versión quieres?"

---

## 🔧 IMPLEMENTACIÓN TÉCNICA (Fase 2)

### **Backend: Supabase**

**Tablas necesarias:**
```sql
-- Familias/Grupos
familias: id, nombre, codigo_invitacion, creado_por, fecha_creacion

-- Miembros de familia
miembros: id, familia_id, usuario_id, rol (admin/editor/lector), fecha_union

-- Usuarios (extensión de auth de Supabase)
usuarios: id, email, nombre, avatar_url

-- Listas (modificación de actual)
listas: id, nombre, familia_id (nullable), creado_por, es_compartida

-- Productos (ya existe, añadir lista_id)
productos: id, lista_id, nombre, pasillo_id, ...

-- Sincronización (log de cambios)
cambios: id, lista_id, producto_id, tipo_cambio, datos, timestamp, usuario_id
```

### **Lógica de sincronización:**
1. App guarda cambio local (Room)
2. Intenta enviar a Supabase (si hay internet)
3. Si falla, queda en cola
4. Cuando hay internet, sincroniza cola
5. Escucha cambios de otros (realtime subscriptions)

---

## 📱 FLUJOS DE USUARIO

### **Flujo 1: Jose crea familia e invita a Yaya**
```
1. Jose abre app → Menú → "🌐 Mi Red"
2. Toca "Crear familia"
3. Pone nombre: "Casa Jose"
4. Elige método: "Generar PIN" o "Mostrar QR"
5. Genera PIN: 847291
6. Se lo dice a la yaya (o muestra QR)
7. Yaya: Menú → "Unirse a familia" → Introduce 847291
8. ¡Ambos comparten listas!
```

### **Flujo 2: Compartir una lista específica**
```
1. Jose crea lista "Carrefour"
2. Menú de la lista → "📤 Compartir"
3. Selecciona "Compartir con familia"
4. Aparece icono 🌐 junto a "Carrefour"
5. Yaya ve "Carrefour" en su app automáticamente
6. Ambos pueden añadir/quitar productos
```

### **Flujo 3: Lista privada (no compartida)**
```
1. Jose crea lista "Regalo sorpresa yaya"
2. Menú → "📤 Compartir" → "Solo yo (privada)"
3. No aparece icono 🌐
4. Yaya NO ve esta lista en su app
5. Solo Jose puede verla y editarla
```

---

## ✅ CHECKLIST IMPLEMENTACIÓN

### **Backend (Supabase):**
- [ ] Configurar proyecto Supabase
- [ ] Crear tablas: familias, miembros, usuarios, cambios
- [ ] Modificar tablas: listas, productos
- [ ] Configurar RLS (Row Level Security)
- [ ] Configurar realtime subscriptions
- [ ] Crear función generar PIN aleatorio

### **Frontend (Android):**
- [ ] Pantalla "Mi Red" (con pestañas)
- [ ] Pantalla "Crear familia"
- [ ] Pantalla "Unirse a familia" (PIN + QR)
- [ ] Diálogo "Compartir lista"
- [ ] Mostrar icono 🌐 en listas compartidas
- [ ] Indicador de sincronización (online/offline)
- [ ] Notificaciones de cambios (opcional)

### **Sincronización:**
- [ ] Lógica guardar local + intentar nube
- [ ] Cola de sincronización pendiente
- [ ] Listener de cambios de otros usuarios
- [ ] Resolución de conflictos

---

## 🚀 FASES DE IMPLEMENTACIÓN

### **Fase 2A: Básico**
- Crear/unirse a familia (PIN)
- Compartir todas las listas (sin opción privada)
- Sincronización básica

### **Fase 2B: Completo**
- PIN + QR
- Compartir lista por lista (privadas posibles)
- Roles (Admin/Editor/Lector)
- Offline completo

### **Fase 2C: Extra**
- Notificaciones push
- Historial de cambios
- Migrar a servidor de Cenito

---

**Documentado por:** Hal  
**Para:** Jose (Xoce)  
**Fecha:** 2026-02-28
