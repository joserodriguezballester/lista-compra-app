# Import/export de datos de usuario — nota técnica

## Objetivo del bloque

Implementar un sistema de **backup lógico JSON** para exportar/importar datos reales del usuario sin depender de copiar el fichero SQLite en bruto.

## Punto de entrada en UI

El flujo se cablea desde:

- `app/src/main/java/com/jose/listacompra/ui/screens/productlist/ProductListScreen.kt`

Ahí se usan:

- `CreateDocument("application/json")` para exportar;
- `OpenDocument()` para importar;
- un diálogo de confirmación previo a la importación.

La escritura/lectura real del JSON se delega en el `ProductListViewModel`, que a su vez usa `contentResolver`.

## Modelo del backup

El modelo raíz es:

- `app/src/main/java/com/jose/listacompra/domain/model/UserDataBackup.kt`

Características vigentes:

- formato lógico JSON;
- `formatVersion = 1`;
- metadata con `exportedAt` y `appVersionName`;
- bloques completos y coherentes de tablas;
- sin separación base/custom dentro del bloque salvo que se diseñe expresamente otro criterio en el futuro.

## Bloques incluidos

La exportación incluye:

- `supermarkets`
- `categories`
- `offers`
- `aisles`
- `articulos`
- `listas`
- `productos`
- `tickets`
- `ticketLines`
- `purchaseHistory`
- `productPriceHistory`
- `productFrequency`
- `productHistory`
- `articuloSupermarketDefaults`
- `categorySupermarketOrders`

## Exportación

La lógica principal vive en:

- `app/src/main/java/com/jose/listacompra/domain/usecase/data/ExportUserDataBackupUseCase.kt`

Criterios técnicos relevantes:

- usa `GsonBuilder().setPrettyPrinting()`;
- lee cada bloque desde su DAO correspondiente;
- ordena por `id` antes de serializar para dar estabilidad razonable al JSON;
- exporta los bloques como conjuntos completos.

## Importación

La lógica principal vive en:

- `app/src/main/java/com/jose/listacompra/domain/usecase/data/ImportUserDataBackupUseCase.kt`

Criterios técnicos relevantes:

- parsea con `Gson`;
- ejecuta la restauración dentro de `database.withTransaction`;
- primero borra los datos cubiertos por el backup;
- después reinyecta cada bloque en un orden que minimiza incoherencias entre referencias.

La política v1 es:

- **restaurar reemplazando**;
- **sin merge**.

## Orden práctico de restauración

La importación sigue este orden general:

1. supermercados
2. categorías
3. ofertas
4. pasillos
5. artículos
6. listas
7. productos
8. tickets + líneas
9. históricos y tablas auxiliares

Esto ayuda a restaurar referencias cruzadas con una base razonable.

## Límites actuales

### Binarios externos

El bloque exporta la información necesaria para restaurar el modelo lógico, pero no pretende transportar binarios externos pesados.

### Tickets

El backup no depende del fichero PDF original para reconstruir el bloque restaurado.

### Imágenes / rutas

Las referencias tipo `photoUri` viajan como dato, pero el backup no garantiza por sí mismo mover binarios asociados entre dispositivos.

## Validación realizada al cierre del bloque

### Validación funcional

- Jose validó manualmente el flujo en la app.

### Validación técnica

- `:app:testDebugUnitTest` -> OK
- `:app:compileDebugAndroidTestKotlin` -> OK
- `:app:assembleRelease` -> OK
- prerelease publicada: `v0.8.13-test28`

## Pendiente que sobrevive al cierre

La ejecución real de `UserDataBackupIntegrationTest` en entorno con `adb` queda como tarea futura separada. El bloque queda cerrado funcionalmente sin convertir ese punto en bloqueo de la rama.
