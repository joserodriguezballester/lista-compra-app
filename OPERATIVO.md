# lista-compra-app — operativo vivo

**Última actualización:** 2026-04-19  
**Ruta principal:** `~/proyectos/privado/Jose/lista-compra-app/`  
**Rama actual:** `feat/import-export-datos`  
**Base de trabajo:** `origin/main` en `5e71732`  
**Versión release declarada:** `0.8.13-test28`

> **Regla fija al entrar en este proyecto:** leer este `OPERATIVO.md` antes de tocar código, builds o releases.
>
> **Regla fija para releases a GitHub:** cuando Jose pida “subir la release” o “subir a GH con APK”, usar por defecto el flujo de **release de test/prerelease con APK adjunto** pensado para actualizar fácil: misma firma canónica, nuevo `APP_TEST_NUMBER`/`versionCode`, tag tipo `vX.Y.Z-testNN` y publicación compatible con que la app detecte la última prerelease con APK. Solo usar otro tipo de release si Jose lo pide explícitamente.
>
> **Bloque siguiente acordado con Jose (v1): import/export de datos.** En esta rama se implementará un **backup lógico** en JSON para poder exportar datos reales e importarlos/restaurarlos en otra versión de la app. La regla de diseño acordada es: **no separar base vs custom dentro de un bloque exportado**; si un conjunto de tablas entra en el backup, debe entrar como **bloque completo y coherente**, salvo razón fuerte en contra. La importación v1 será en modo **restaurar reemplazando**, no merge. La ubicación UI acordada es el **overflow de "Datos"**.

---

## 1. Propósito de este fichero

Este documento sirve como **sitio único operativo** del proyecto para concentrar:
- estado real del bloque vigente,
- bugs y rarezas activas,
- deuda técnica,
- tests útiles y tests legacy desfasados,
- verificaciones manuales,
- próximos pasos.

La idea es no repartir esto entre chat, memoria diaria, notas sueltas y varios markdown antiguos.

---

## 2. Estado actual del bloque en curso — navegación centralizada / drawer / test27

### Objetivo
Cerrar bien el refactor reciente de navegación principal de la app:
1. centralizar la navegación en `AppNavigator` + `DrawerDestination`,
2. unificar `drawer` + `top bar` + `scaffold` en pantallas principales,
3. evitar estados raros durante transiciones de navegación,
4. asegurar que `Inicio` siga visible en el drawer,
5. integrar `Importar ticket` en el mismo armazón general,
6. dejar el updater/overflow apuntando a la última prerelease correcta.

### Estado actual
**Bloque cerrado y mergeado en `main`:**
- Se introdujo navegación centralizada con `AppNavigator` y `DrawerDestination`.
- Se creó `AppDrawerScaffold` como base compartida para pantallas principales.
- Se migraron varias pantallas al scaffold común del drawer.
- Se endureció `AppDrawer` para tolerar mejor transiciones con destino actual nulo.
- Se forzó que `Inicio` siga visible en el drawer.
- `TicketImportScreen` quedó alineada con el scaffold común.
- El updater del overflow quedó ajustado para seguir la última prerelease.
- La build de Milo quedó apuntando al keystore canónico del repo.
- La versión declarada actual es **`0.8.13-test27`**.

### Archivos tocados en este bloque
- `app/build.gradle.kts`
- `app/src/androidTest/java/com/jose/listacompra/ui/components/AppDrawerDestinationTest.kt`
- `app/src/main/java/com/jose/listacompra/ui/AppUiConfig.kt`
- `app/src/main/java/com/jose/listacompra/ui/components/AppDrawer.kt`
- `app/src/main/java/com/jose/listacompra/ui/components/AppDrawerScaffold.kt`
- `app/src/main/java/com/jose/listacompra/ui/components/CommonTopBar.kt`
- `app/src/main/java/com/jose/listacompra/ui/navigation/AppNavigation.kt`
- `app/src/main/java/com/jose/listacompra/ui/navigation/AppNavigator.kt`
- `app/src/main/java/com/jose/listacompra/ui/navigation/AppNavigatorImpl.kt`
- `app/src/main/java/com/jose/listacompra/ui/navigation/DrawerDestination.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/catalogo/CatalogoScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/categories/CategoriesScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/history/HistoryScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/home/HomeScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/offers/OffersScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/productlist/ProductListScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/supermarket/SupermarketListScreen.kt`
- `app/src/main/java/com/jose/listacompra/ui/screens/ticket/TicketImportScreen.kt`
- `gradle.properties`
- `signing/milo-debug.keystore`

### Verificación hecha hoy
- `git fetch origin --prune` OK.
- `HEAD` local = `origin/main` = `5e71732` (`merge: close app navigator centralization work`).
- Diferencia con remoto: **`0 ahead / 0 behind`**.
- El commit de `test27` (`01dd444`, `fix: make overflow updater track latest prerelease for test27`) está incluido en `HEAD`.
- Árbol de trabajo limpio.

### Observaciones operativas
- Este fichero estaba atrasado en el bloque `ticket-save-history`; queda realineado hoy al estado real de `main`.
- El versionado real lo marcan `APP_VERSION_BASE=0.8.13` y `APP_TEST_NUMBER=27`.
- En `release-artifacts/lista-compra-app/` solo están archivados `test22`, `test23` y `test24`.
- En el repo sí existe un `app/build/outputs/apk/release/app-release.apk` local reciente.
- Si queremos conservar `test27` como artefacto canónico fuera de `app/build/`, falta archivarlo explícitamente.
- **Semilla / producción:** para builds normales de la app se fija `DataConfig.LOAD_FULL_DATA = false`, dejando solo el llenado mínimo (supermercados, categorías, ofertas, pasillos y lista por defecto) y evitando catálogo/productos/historial de ejemplo.
- **Persistencia entre releases:** pasar de una release a otra del tipo `testNN` sin cambiar la versión Room (actual `14`) no debería borrar la BD existente: no hay `fallbackToDestructiveMigration()` y el seeder solo inserta mínimos si faltan registros, no hace wipe.
- **Ojo técnico pendiente:** en `DatabaseModule` no se está registrando `MIGRATION_10_11` aunque existe en `Database.kt`. Esto no afecta al salto normal `test27` → siguiente `testNN` si la BD sigue en `14`, pero conviene corregirlo antes de futuras migraciones reales o saltos desde bases antiguas.

---

## 3. Tests y verificaciones activas

### Tests útiles activos
- `app/src/test/java/com/jose/listacompra/SaveTicketUseCaseTest.kt`
  - Sigue cubriendo bien el bloque anterior de guardar ticket → histórico.
- `app/src/androidTest/java/com/jose/listacompra/ui/components/AppDrawerDestinationTest.kt`
  - Test útil del bloque actual de drawer / navegación.

### Tests legacy desactivados temporalmente
- `app/src/test/java/com/jose/listacompra/DomainModelTest.kt.disabled`
- `app/src/test/java/com/jose/listacompra/JsonExportTest.kt.disabled`
- `app/src/test/java/com/jose/listacompra/OfferCalculationTest.kt.disabled`

#### Motivo heredado
No estaban rompiendo por el drawer, sino por desincronización previa con el modelo actual.

### Alcance de la verificación hecha hoy
En esta actualización del operativo **no** se ha relanzado compilación ni suite de tests.
Lo verificado hoy ha sido:
- sincronía real con remoto,
- versión declarada actual,
- presencia efectiva de `test27` en `HEAD`,
- ramas vivas y estado limpio del repo.

---

## 4. Estado git funcional actual

### Rama principal
- `main` → `5e71732` → `merge: close app navigator centralization work`

### Ramas auxiliares que siguen presentes
- `refactor/app-navigator-centralized` → `737b994`
- `fix/tickets-import` → `6f6c026`
- `fix/navigation-drawer-home-list` → `d9c9974`
- `backup/navigation-drawer-home-list-2026-04-17` → `d9c9974`

### Tags útiles del tramo reciente
- `v0.8.13-test22`
- `v0.8.13-test23`
- `v0.8.13-test24`
- `v0.8.13-test25`
- `v0.8.13-test26`
- `v0.8.13-test27`

---

## 5. Bugs, deuda y rarezas heredadas del repo

### 5.1 Críticos de auditoría
Cruce de `AUDITORIA.md` y `AUDITORIA-2026-04-10.md`:

- **Fuga de arquitectura domain → data**
  - ejemplo: `ResetDataToProductionUseCase.kt` importa DAOs directamente.
- **`IHistoryRepository` expone Entities de Room**
  - la capa domain no debería exponer entities de data.
- **UI / ViewModels dependen de data layer directamente**
  - prefs, repos y entities de data usados en capa superior.
- **`fallbackToDestructiveMigration()`**
  - riesgo de pérdida de datos en migraciones no cubiertas.
- **`exportSchema = false`**
  - dificulta probar migraciones correctamente.
- **Archivo duplicado obsoleto**
  - `ui/screens/EditProductDialog1.kt` parece backup/duplicado antiguo.
- **Dependencias duplicadas e inconsistentes en `app/build.gradle.kts`**
  - CameraX / ML Kit duplicados y versiones mezcladas.

### 5.2 Importantes
- `ProductListViewModel` demasiado grande y con muchas dependencias.
- Screens muy largas (`HistoryScreen`, `ProductListScreen`, `MainScreen`, `AddProductToListDialog`).
- Patrones CRUD repetidos entre pantallas y ViewModels.
- Repositorios/DAO con mucho código repetido.
- Dropdowns o componentes duplicados.

### 5.3 TODOs visibles ahora mismo en código
- `ui/screens/supermarket/SupermarketListScreen.kt`
  - varias acciones siguen con “implementar con ViewModel”.
- `ui/screens/main/MainScreen.kt`
  - restos comentados y wiring pendiente (`deshacer`, diálogo de voz, placeholders viejos de offers/suggestions).
- `ui/viewmodel/ProductListViewModel.kt`
  - política de añadir producto sin artículo pendiente de definir.
- `data/repository/HistoryRepositoryImpl.kt`
  - estadísticas de gasto sin implementar.

### 5.4 Warnings / suciedad técnica detectada
De auditorías + compilaciones previas:
- parámetros no usados,
- variables no usadas,
- imports no usados,
- APIs deprecadas (`Icons.Default.ArrowBack` y similares),
- shadowing de variables,
- archivos backup / duplicados (`EditProductDialog1.kt`, quizá otros análogos).

---

## 6. Documentos útiles ahora mismo

### `AUDITORIA.md`
Útil para:
- mapa general de problemas estructurales,
- deuda de arquitectura,
- warnings de compilación,
- prioridades de corrección.

### `AUDITORIA-2026-04-10.md`
Útil para:
- auditoría más detallada,
- fugas de capa,
- duplicaciones,
- tamaño excesivo de ciertas piezas.

### `TAREAS.md`
Útil para:
- histórico de tareas realizadas,
- bugs antiguos,
- roadmap funcional,
- rastrear qué ideas ya existían.

### `VERIFICAR.md`
Útil para:
- checklist manual,
- pruebas de voz, supermercados, filtros y bugs de UI.

### `docs/test-v0.7.4-tickets.md`
Útil para:
- flujo de prueba manual de importación de tickets,
- revisar el bloque ticket → histórico cuando volvamos a tocarlo.

### `DOCUMENTACION.md`
Útil para:
- arquitectura general,
- contexto del refactor de supermercados,
- problemas conocidos y decisiones previas.

### `PLAN-SUPERMERCADO-PRODUCTO-2026-04-06.md`
Útil como referencia histórica del bloque supermercado por producto.

---

## 7. Pruebas manuales recomendadas ahora

### Navegación / drawer / test27
- Abrir la app en `Inicio`.
- Abrir/cerrar el drawer desde Home y desde varias pantallas principales.
- Navegar entre Home, Catálogo, Categorías, Historial, Ofertas, Supermercados e Importar ticket.
- Confirmar que título/top bar y acción principal siguen siendo coherentes.
- Comprobar que `Inicio` no desaparece del drawer durante la navegación.
- Comprobar que no aparece estado raro por destino actual nulo al cambiar rápido de pantalla.
- Revisar que `Importar ticket` usa el scaffold común y no queda “fuera” del drawer.
- Revisar el overflow/updater y confirmar que sigue la última prerelease esperada.

### Ticket importado / histórico (arrastrado del bloque anterior)
- Usar como base `docs/test-v0.7.4-tickets.md`.
- Comprobar guardar ticket → histórico global.
- Comprobar histórico por producto de líneas macheadas.
- Comprobar fecha del ticket y precio unitario.
- Comprobar que líneas no macheadas o descuentos no contaminan histórico por producto.

---

## 8. Próximos pasos recomendados

### Opción A — cerrar bien `test27`
1. Validación manual de navegación/drawer en dispositivo.
2. Si va bien, archivar artefacto `test27` en `release-artifacts/lista-compra-app/`.
3. Apuntar cualquier matiz real de UX o navegación que aparezca al probar.

### Opción B — arrancar `test28`
1. Elegir siguiente bug/ajuste.
2. Subir `APP_TEST_NUMBER` a `28` cuando toque.
3. Compilar en Milo y guardar artefacto canónico.

### Opción C — sanear base de tests y deuda
1. Rehacer `DomainModelTest`.
2. Rehacer `JsonExportTest`.
3. Rehacer `OfferCalculationTest`.
4. Restaurarlos a `.kt`.
5. Ejecutar `testDebugUnitTest` completo.
6. Retomar limpieza de `build.gradle.kts` y fugas entre capas.

### Opción D — implementar import/export de datos (rama actual)
1. Ajustar el backup al criterio acordado con Jose: exportar **bloques completos y coherentes de tablas**, sin separar base/custom dentro del bloque salvo motivo fuerte.
2. Implementar exportación de datos reales.
3. Implementar importación en modo **restaurar reemplazando**.
4. Exponer la función en el **overflow de Datos**.
5. Verificar restauración entre releases de test sin perder la BD real del usuario.
