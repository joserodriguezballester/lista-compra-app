# Uso del import/export de datos de usuario

## Dónde está en la app

El flujo vive en **Mi lista** dentro del **overflow** de la pantalla.

Ruta práctica:

- `Mi lista` -> overflow -> `Datos` -> `Exportar datos`
- `Mi lista` -> overflow -> `Datos` -> `Importar datos`

## Exportar datos

### Flujo de uso

1. Abrir `Mi lista`.
2. Abrir el menú overflow.
3. Entrar en `Datos`.
4. Pulsar `Exportar datos`.
5. Elegir dónde guardar el fichero JSON.

La app genera un **backup lógico en JSON** usando el selector de documentos de Android.

### Qué esperar

- el fichero se propone por defecto como `lista-compra-backup.json`;
- al terminar, la app muestra un mensaje de confirmación;
- el backup no es una copia bruta del SQLite, sino un JSON pensado para restaurar datos útiles del usuario.

## Importar datos

### Flujo de uso

1. Abrir `Mi lista`.
2. Abrir el menú overflow.
3. Entrar en `Datos`.
4. Pulsar `Importar datos`.
5. Elegir el fichero JSON.
6. Confirmar el diálogo de importación.

Antes de ejecutar la restauración, la app muestra una confirmación porque la importación **reemplaza** los datos cubiertos por el backup seleccionado.

## Regla funcional actual

La importación v1 funciona en modo:

- **restaurar reemplazando**
- **sin merge**

Es decir, no mezcla parcialmente datos antiguos y nuevos dentro de los bloques cubiertos.

## Qué datos cubre este bloque

El backup lógico cubre bloques completos y coherentes de datos de usuario, incluyendo:

- supermercados;
- categorías;
- ofertas;
- pasillos;
- artículos;
- listas;
- productos;
- tickets y líneas de ticket;
- histórico de compras y precios;
- frecuencias e histórico de producto;
- defaults artículo-supermercado;
- orden de categorías por supermercado.

## Qué no debe asumirse

### No es una copia bruta de la base de datos

El fichero exportado:

- **no** es el SQLite en bruto;
- **sí** es un JSON lógico con los bloques que la app sabe exportar e importar.

### No guarda binarios asociados

El backup actual no está pensado para transportar binarios pesados asociados a rutas externas.

En particular:

- no se apoya en el PDF original del ticket para restaurar el bloque;
- no resuelve todavía la portabilidad completa de imágenes/binarios entre dispositivos.

## Estado funcional al cierre del bloque

Este bloque se da por funcionalmente válido por:

- validación manual/visual hecha por Jose en la app;
- compilación correcta del bloque de tests Android (`:app:compileDebugAndroidTestKotlin`).

La **ejecución real** del `androidTest` queda como tarea futura separada cuando haya entorno práctico de `adb` / dispositivo / emulador.
