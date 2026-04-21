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

- [urgente] Revisar error al añadir a la lista un artículo recién creado desde ticket
  - Secuencia observada por Jose: crear artículo desde una línea de ticket -> volver a "Mi lista" -> intentar añadir ese artículo a la lista de la compra -> salta "Error al añadir" con `FOREIGN KEY constraint failed`.
  - Diagnóstico actual: el flujo de `addProduct` probablemente intenta guardar el producto en `products` y después registrar histórico/frecuencia en la misma acción; hay una sospecha fuerte de que el fallo venga del guardado de histórico de precio con `purchaseId = 0` cuando el artículo arrastra precio desde el ticket.
  - Duda pendiente a confirmar: separar qué paso falla exactamente (alta en lista, frecuencia o histórico) porque el mensaje actual mezcla todo bajo "Error al añadir" y puede ocultar que el producto sí se haya insertado antes del fallo posterior.
  - Hecho cuando: quede identificado con certeza el paso que rompe, se corrija el flujo para que no falle al añadir desde ticket y el mensaje de error deje de mezclar operaciones distintas.

- [urgente] Reparar y renombrar el ticket de prueba/debug en `Importar ticket`
  - Síntoma observado por Jose: al pulsar el botón del ticket de prueba en la pantalla `Importar ticket`, aparece `PDF error`.
  - Traza visible en captura: `Seleccionado debug asset: aaa.pdf` seguido de `PDF error`.
  - Diagnóstico rápido: ahora mismo el proyecto no tiene `app/src/main/assets` ni existe el asset `aaa.pdf`; además, `ImportTicketUseCase` oculta la causa real devolviendo solo `PDF error`.
  - Cambio funcional deseado: renombrar **el archivo real de ejemplo** a un nombre con sentido (por ejemplo `ticket-prueba.pdf` o similar) y dejar el texto visible del botón alineado con ese nombre/uso. `aaa.pdf` no es un nombre válido a nivel de producto ni de mantenimiento.
  - Contexto: no está claro qué cambio reciente ha roto este flujo, pero ahora mismo el ticket de prueba ya no sirve para validar ni depurar la importación.
  - Hecho cuando: exista de nuevo el PDF de ejemplo con un nombre claro, el botón/use case/logs de debug apunten a ese nombre correcto y el error deje de quedar oculto tras un `PDF error` genérico.

## Media

- [media] Revisar y reorganizar los overflow de todas las screens
  - Contexto: no se trata solo del bloque de import/export; la revisión debe mirar **todas las acciones** disponibles en los overflow de las distintas pantallas.
  - Objetivo: que la organización de acciones por screen sea más coherente, mantenible y fácil de usar.
  - Hecho cuando: quede definida y aplicada una estructura más clara de overflow a nivel transversal de app.

- [media] Compactar el layout de `AddProductToListDialog` para que el campo `Notas` se vea completo
  - Contexto: en el diálogo de añadir hay demasiada separación vertical entre componentes y el campo de notas no llega a verse completo de forma cómoda.
  - Objetivo: reducir espacios/márgenes entre componentes sin perder legibilidad general del formulario.
  - Hecho cuando: el diálogo de añadir muestre completo el campo `Notas` en pantalla normal, sin que quede cortado por exceso de separación entre bloques.

- [media] Reestructurar `EditProductDialog` para que tenga una organización similar a `AddProductToListDialog`
  - Contexto: ahora el diálogo de editar tiene una composición distinta al de añadir producto, y eso rompe consistencia visual y de uso.
  - Objetivo: acercar orden de bloques, jerarquía visual y disposición general de controles al patrón de `AddProductToListDialog`, adaptando solo lo necesario a acciones propias de edición.
  - Hecho cuando: editar y añadir producto compartan una estructura claramente parecida y la experiencia resulte coherente entre ambos diálogos.

- [media] Unificar la edición de artículo en catálogo para evitar el doble camino `AddEditArticuloDialog` / `ArticuloDetailDialog`
  - Contexto: ahora mismo la creación/edición de artículo y la edición desde detalle no siguen exactamente el mismo patrón, y `ArticuloDetailDialog` en modo edición queda sin cabecera clara.
  - Objetivo: dejar un flujo coherente y unificado para editar artículos del catálogo, evitando divergencias visuales y de comportamiento entre ambos diálogos.
  - Hecho cuando: la edición de artículo use un único patrón claro, con estructura consistente y sin pérdidas de contexto como la falta de título/cabecera en edición.

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
