# lista-compra-app — operativo vivo

**Última actualización:** 2026-04-20  
**Ruta principal:** `~/proyectos/privado/Jose/lista-compra-app/`

---

## 1. Qué es este fichero

Este documento describe solo:
- las **normas de trabajo estables** del proyecto,
- el **frente activo real** cuando exista,
- la validación útil del bloque en curso,
- los documentos vivos relacionados,
- y los siguientes pasos inmediatos del bloque abierto.

No es backlog, no es changelog, no es histórico de ramas cerradas.

Cuando una rama se cierra:
- conocimiento estable -> `DOCUMENTACION.md` o `docs/...`
- pendientes futuros -> `TAREAS.md`
- foto temporal de esa rama -> borrar

---

## 2. Normas de trabajo estables

### Leer este fichero al entrar
Antes de tocar código, builds o releases, leer este `OPERATIVO.md`.

### Compilaciones en Milo
Antes de compilar en Milo:
- comprobar e informar del estado de Milo,
- mirar si hay otra compilación o proceso pesado en marcha,
- informar de la situación relevante (carga, RAM disponible, temperatura/procesos pesados si aplica),
- si Milo está ocupado, priorizar compilación educada o esperar,
- no compilar tests y app en paralelo.

### Releases de GitHub
Cuando Jose pida "subir la release" o "subir a GH con APK", usar por defecto el flujo de:
- **release de test / prerelease**,
- **APK adjunto**,
- **misma firma canónica**,
- **nuevo `APP_TEST_NUMBER` / `versionCode`**,
- tag tipo **`vX.Y.Z-testNN`**,
- publicación compatible con que la app detecte la última prerelease con APK y pueda actualizar fácilmente.

Solo usar otro tipo de release si Jose lo pide explícitamente.

---

## 3. Rama activa

### Identificación de la rama
- **Rama actual:** `feat/centralizar-fotos-articulos`
- **Base de trabajo:** `origin/main` en `23f11a7`
- **Frente funcional:** centralización de fotos nuevas de artículos

### Objetivo
Centralizar las **fotos nuevas de artículos** en una carpeta visible y canónica del dispositivo, sin migrar por ahora las fotos antiguas.

### Decisiones vigentes
- **Alcance v1:** solo fotos nuevas de artículos.
- **Carpeta canónica visible:** `Pictures/ListaCompra/Articulos/`
- **Migración de fotos antiguas:** no en esta fase.
- **Referencia en BD:** se mantiene `photoUri: String?`.
- **Normalización:** al guardar/editar artículo, no en el momento de preview.
- **Nombres de fichero:** `articulo-<uuid>.jpg`.

### Regla operativa
La normalización de la imagen se hará al confirmar `Guardar`:
- **Galería** -> copiar a carpeta canónica.
- **Cámara** -> copiar/mover desde temporal a carpeta canónica.
- **Escáner/OpenFoodFacts** -> descargar URL remota a carpeta canónica.
- **Imagen ya centralizada** -> no duplicar.

### Diseño técnico previsto
1. Crear `ArticuloPhotoStorage` (o `PhotoStorage` genérico).
2. Inyectarlo con Hilt.
3. Integrarlo en `SaveArticuloUseCase` y `UpdateArticuloUseCase`.
4. Mantener la UI ligera: preview con URI temporal/remota, normalización solo al guardar.

### Fuera de alcance en esta fase
- migrar fotos antiguas;
- borrar automáticamente fotos antiguas sustituidas;
- meter binarios de imagen dentro del backup JSON;
- centralizar fotos de `products` salvo rebote natural desde `articulos`.

### Documentación de referencia
- Plan completo: `docs/fotos-articulos-centralizacion.md`

---

## 4. Tests vivos relevantes

- `app/src/test/java/com/jose/listacompra/SaveTicketUseCaseTest.kt`
  - cobertura útil del bloque guardar ticket -> histórico

- `app/src/androidTest/java/com/jose/listacompra/ui/components/AppDrawerDestinationTest.kt`
  - cobertura útil del bloque de navegación/drawer

- `app/src/androidTest/java/com/jose/listacompra/domain/usecase/data/UserDataBackupIntegrationTest.kt`
  - cobertura útil del bloque import/export de datos

---

## 5. Documentos vivos

- `DOCUMENTACION.md`
  - índice corto de documentación técnica viva

- `docs/fotos-articulos-centralizacion.md`
  - plan completo de centralización de fotos

- `docs/import-export-datos-uso.md`
  - uso funcional del backup/import de datos de usuario

- `docs/import-export-datos-tecnico.md`
  - diseño técnico y límites actuales del bloque import/export

- `docs/modelo-bd-entidad-relacion.md`
  - modelo real de BD, relaciones y contexto útil

- `docs/modelo-bd-diagrama-er.md`
  - diagrama ER complementario

- `docs/navegacion.md`
  - navegación actual, drawer y rutas principales

---

## 6. Siguientes pasos inmediatos

1. Crear interfaz `ArticuloPhotoStorage`.
2. Implementar copia/descarga a carpeta canónica.
3. Inyectar con Hilt.
4. Integrar en `SaveArticuloUseCase`.
5. Integrar en `UpdateArticuloUseCase`.
6. Compilar y validar manualmente los flujos de galería, cámara y escáner.
