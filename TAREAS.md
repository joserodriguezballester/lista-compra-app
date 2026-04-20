# TAREAS.md

Backlog vivo de `lista-compra-app`.

## Reglas de uso
- Este fichero guarda pendientes reales, deuda técnica e ideas aparcadas.
- No usar como diario, changelog ni histórico de ramas cerradas.
- Cuando una tarea pase a ser trabajo activo de una rama, moverla a `OPERATIVO.md`.
- Cuando una rama se cierre:
  - conocimiento estable -> `DOCUMENTACION.md` o `docs/...`
  - pendiente futuro -> `TAREAS.md`
  - ruido temporal -> borrar

---

## Urgente

- [urgente] Implementar la centralización de fotos nuevas de artículos en carpeta visible
  - Referencia: `docs/fotos-articulos-centralizacion.md`
  - Decisión vigente: **solo fotos nuevas**, carpeta visible **`Pictures/ListaCompra/Articulos/`**, sin migrar fotos antiguas en esta fase.
  - Contexto: hoy entran fotos desde galería, cámara y URL remota, y algunas rutas temporales o heterogéneas no son una base limpia para el producto.
  - Alcance v1: centralizar al guardar/editar artículo; mantener `photoUri` como `String?`; no abrir migración Room por este cambio; no borrar automáticamente fotos antiguas sustituidas; el backup JSON sigue exportando la URI pero no el binario.
  - Hecho cuando: las fotos nuevas de artículos quedan guardadas en la carpeta visible canónica, la app las sigue cargando tras reinicio y los flujos de galería, cámara y escáner quedan cubiertos.

---

## Media

- [media] Revisar y reorganizar los overflow de todas las screens
  - Contexto: no se trata solo del bloque de import/export; la revisión debe mirar **todas las acciones** disponibles en los overflow de las distintas pantallas.
  - Objetivo: que la organización de acciones por screen sea más coherente, mantenible y fácil de usar.
  - Hecho cuando: quede definida y aplicada una estructura más clara de overflow a nivel transversal de app.

---

## Parking

- [baja] Ejecutar y verificar de verdad el `androidTest` del bloque import/export cuando haya entorno práctico de `adb` / dispositivo / emulador
  - Contexto: el bloque ya quedó validado manualmente por Jose y la compilación de `:app:compileDebugAndroidTestKotlin` está OK, pero falta la ejecución real del test instrumental.
  - Hecho cuando: `UserDataBackupIntegrationTest` (o la batería equivalente) se ejecute en un entorno real y su resultado quede verificado.

- [baja] Registrar `MIGRATION_10_11` en `DatabaseModule`
  - Contexto: la migración existe en `Database.kt`, pero no está registrada en el módulo de base de datos.
  - Por qué importa: no afecta al salto normal entre releases `testNN` con base ya en versión 14, pero conviene cerrarlo antes de futuras migraciones reales o restauraciones desde bases antiguas.
  - Hecho cuando: la migración quede registrada o se documente/eliminen sus restos si ya no aplica.

- [baja] Revisar si la capa dominio está demasiado acoplada a `data` / Room
  - Contexto: hay use cases, interfaces y algunas piezas de UI que usan directamente DAOs, entities o tipos de la capa `data`.
  - Por qué importa: no es un bug urgente, pero puede complicar pruebas, mantenimiento y refactors futuros.
  - Hecho cuando: quede claro si merece una rama específica de saneamiento y con qué alcance.

- [baja] Completar el cableado funcional de `SupermarketListScreen.kt`
  - Contexto: la pantalla existe a nivel de UI, pero alta, edición y borrado de supermercados siguen sin conectarse al ViewModel.
  - Contexto: en el fragmento revisado, la lista de supermercados aparece además como estado local de pantalla, sin quedar clara la carga real de datos.
  - Por qué importa: no es urgente, pero sí afecta al uso real de la gestión de supermercados.
  - Hecho cuando: la pantalla cargue datos reales y las acciones de crear, editar y eliminar supermercados queden operativas.

- [baja] Revisar si `MainScreen.kt` es código legado eliminable
  - Contexto: el fichero contiene una implementación antigua muy comentada, con navegación, diálogos y varios TODOs/placeholders sin cerrar.
  - Por qué importa: no parece una pieza viva de producto, pero conviene decidir si se reutiliza algo o si debe purgarse para reducir ruido.
  - Hecho cuando: quede claro si se elimina, se trocea o se conserva por algún motivo real.

- [baja] Definir la política común al introducir productos sin artículo asociado
  - Contexto: el problema aparece explícito en el flujo de voz, pero la decisión afecta realmente a cualquier sistema de introducción de productos (voz, texto u otros).
  - Contexto: si no se fija una regla común, distintos flujos pueden acabar creando productos incompletos o con criterios distintos.
  - Por qué importa: conviene que todas las vías de entrada de productos se comporten de forma coherente.
  - Hecho cuando: la política quede decidida y aplicada de forma consistente en todos los sistemas de introducción de productos.

- [baja] Aclarar si `HistoryRepositoryImpl.getSpendingStats()` debe completarse o eliminarse
  - Contexto: ahora mismo devuelve estadísticas vacías (`0`) con un TODO explícito, así que la implementación no está cerrada.
  - Contexto: existe además otra vía en `ShoppingListRepository.kt` que sí calcula estadísticas de gasto, por lo que puede haber solape o duplicidad.
  - Por qué importa: conviene decidir si esta ruta debe implementarse de verdad o si sobra para no dejar comportamiento ambiguo.
  - Hecho cuando: quede claro si se completa esta implementación o se elimina/abandona por duplicada.
