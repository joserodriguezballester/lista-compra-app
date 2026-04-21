# lista-compra-app — operativo vivo

**Última actualización:** 2026-04-21
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

## 3. Frente recién cerrado

### Rama cerrada
- **Rama:** `feat/centralizar-fotos-articulos`
- **Base usada:** `origin/main` en `23f11a7`
- **Bloque cerrado:** centralización de fotos nuevas de artículos

### Resultado estable que queda en código
- existe `ArticuloPhotoStorage` como contrato de dominio;
- existe `MediaStoreArticuloPhotoStorage` como implementación real;
- Hilt lo inyecta mediante `StorageModule`;
- `SaveArticuloUseCase` centraliza la foto antes de persistir cuando procede;
- `UpdateArticuloUseCase` evita duplicar si la foto no cambia y centraliza si cambia;
- la ubicación canónica queda en `Pictures/ListaCompra/Articulos/`;
- los nombres de archivo pasan a ser legibles con slug + identificador corto.

### Fuera de alcance que sigue fuera
- migrar fotos antiguas;
- borrar automáticamente fotos sustituidas;
- meter binarios en el backup JSON;
- extender todavía este patrón a otros bloques fuera de artículos.

### Documentación viva del bloque
- `docs/fotos-articulos-centralizacion.md`

## 4. Tests vivos relevantes

- `app/src/test/java/com/jose/listacompra/domain/usecase/articulo/ArticuloPhotoCentralizationUseCaseTest.kt`
  - cobertura útil del bloque de centralización de fotos nuevas de artículos

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
  - comportamiento actual, diseño técnico y límites del bloque de centralización de fotos

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

1. Partir de `main` actualizado para una rama nueva de saneamiento/errores.
2. Reparar y renombrar el ticket de prueba de `Importar ticket` para que deje de depender de `aaa.pdf`.
3. Revisar el `FOREIGN KEY constraint failed` al añadir a la lista un artículo recién creado desde ticket.
4. Ajustar `AddProductToListDialog` para que el campo `Notas` se vea completo.
5. Reestructurar `EditProductDialog` para acercarlo a `AddProductToListDialog`.
6. Unificar la edición de artículos de catálogo entre `AddEditArticuloDialog` y `ArticuloDetailDialog`.
