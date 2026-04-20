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
Cuando Jose pida “subir la release” o “subir a GH con APK”, usar por defecto el flujo de:
- **release de test / prerelease**,
- **APK adjunto**,
- **misma firma canónica**,
- **nuevo `APP_TEST_NUMBER` / `versionCode`**,
- tag tipo **`vX.Y.Z-testNN`**,
- publicación compatible con que la app detecte la última prerelease con APK y pueda actualizar fácilmente.

Solo usar otro tipo de release si Jose lo pide explícitamente.

---

## 3. Estado activo del repo base

### Frente funcional abierto
- **En este estado base no hay un frente funcional activo declarado todavía.**
- El último bloque cerrado ha sido **import/export de datos de usuario**.
- Su conocimiento estable ya no vive en este fichero, sino en documentación permanente.

### Cierre del bloque import/export
Al cerrar ese bloque, queda como conocimiento estable:
- documentación de uso en `docs/import-export-datos-uso.md`;
- documentación técnica en `docs/import-export-datos-tecnico.md`;
- validación manual hecha por Jose;
- compilación OK de `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin` y `:app:assembleRelease`;
- prerelease publicada `v0.8.13-test28`.

Los pendientes que sobreviven al cierre se gestionan desde `TAREAS.md`, no desde este fichero.

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

- `docs/import-export-datos-uso.md`
  - uso funcional del backup/import de datos de usuario

- `docs/import-export-datos-tecnico.md`
  - diseño técnico y límites actuales del bloque import/export

- `docs/modelo-bd-entidad-relacion.md`
  - modelo real de BD, relaciones y contexto útil para import/export

- `docs/modelo-bd-diagrama-er.md`
  - diagrama ER complementario basado en las `Entity` reales

- `docs/navegacion.md`
  - navegación actual, drawer y rutas principales

- `docs/fotos-articulos-centralizacion.md`
  - plan aprobado para el siguiente frente sobre fotos de artículos

---

## 6. Qué hacer cuando se abra una rama nueva

Cuando un nuevo frente pase a ser trabajo activo:
1. declararlo aquí con su rama real,
2. mover aquí sus decisiones vigentes,
3. dejar en `TAREAS.md` solo lo que no forme parte del bloque activo.
