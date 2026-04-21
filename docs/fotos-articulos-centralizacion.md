# Centralización de fotos de artículos

## Qué quedó implementado

La app ya centraliza **las fotos nuevas o modificadas de artículos** al guardar cambios, usando una ubicación canónica y visible del dispositivo.

El alcance cerrado en esta ronda es deliberadamente limitado:

- **sí** cubre fotos de artículos al crear o editar;
- **no** migra fotos antiguas;
- **no** mete binarios dentro del backup JSON;
- **no** extiende todavía la centralización a otros flujos fuera del bloque de artículos.

## Comportamiento actual

### Al guardar un artículo nuevo

`SaveArticuloUseCase` centraliza `photoUri` antes de persistir si la foto viene informada y no está vacía.

### Al editar un artículo existente

`UpdateArticuloUseCase` aplica estas reglas:

- si `photoUri` pasa a `null` o vacío -> la foto se elimina en persistencia;
- si `photoUri` no cambia respecto al artículo ya guardado -> se conserva tal cual, sin duplicar;
- si `photoUri` cambia -> se centraliza antes de actualizar.

## Piezas implicadas

### Contrato de almacenamiento
- `app/src/main/java/com/jose/listacompra/domain/storage/ArticuloPhotoStorage.kt`

Expone una operación única:
- `centralizeIfNeeded(photoUri, articuloName)`

### Implementación actual
- `app/src/main/java/com/jose/listacompra/data/storage/MediaStoreArticuloPhotoStorage.kt`

Responsabilidades principales:
- detectar si la imagen ya está centralizada;
- copiar imágenes locales (`content://`, `file://` o ruta cruda);
- descargar imágenes remotas (`http://`, `https://`);
- escribir el resultado en la ubicación canónica final.

### Inyección
- `app/src/main/java/com/jose/listacompra/di/StorageModule.kt`

Hilt enlaza `ArticuloPhotoStorage` con `MediaStoreArticuloPhotoStorage` como implementación singleton.

## Ubicación canónica

### Android Q o superior
Se usa `MediaStore` con:
- `RELATIVE_PATH = Pictures/ListaCompra/Articulos/`

### Android anterior a Q
Se usa almacenamiento público en:
- `Pictures/ListaCompra/Articulos/`

y luego se lanza `MediaScannerConnection` para visibilidad en el dispositivo.

## Nombres de fichero

Los nombres ya no son opacos tipo UUID puro.

Formato actual:
- `<slug-del-articulo>-<shortId>.<ext>`

Ejemplos esperables:
- `tomate-frito-a1b2c3d4.jpg`
- `leche-entera-9f2e1abc.png`

Detalles:
- el slug se genera a partir del nombre del artículo;
- se eliminan tildes y caracteres raros;
- se limita la longitud;
- la extensión final depende del MIME detectado (`jpg`, `png`, `webp`, `gif`).

## Entradas soportadas

La centralización actual contempla:

- imagen local vía `content://...`;
- imagen local vía `file://...`;
- ruta local cruda;
- URL remota `http/https`;
- imagen ya centralizada, que se devuelve sin duplicar.

## Límites conocidos que quedan fuera del cierre

- migración de fotos antiguas ya existentes en base de datos;
- borrado automático de archivos viejos al sustituir una foto;
- portabilidad binaria de imágenes en export/import JSON;
- centralización explícita de fotos fuera del bloque de artículos.

## Validación automatizada

Cobertura unitaria relevante:
- `app/src/test/java/com/jose/listacompra/domain/usecase/articulo/ArticuloPhotoCentralizationUseCaseTest.kt`

Casos cubiertos:
- alta con foto nueva;
- alta con foto vacía;
- edición sin cambio de foto;
- edición con foto nueva;
- edición eliminando foto;
- fallo al actualizar un artículo inexistente.

## Nota operativa

Si en el futuro se vuelve a tocar este bloque, la validación manual útil sigue siendo:

1. crear artículo con foto desde galería;
2. crear artículo con foto desde cámara;
3. crear artículo desde escáner/OpenFoodFacts con imagen remota;
4. editar sin cambiar foto y verificar que no duplica;
5. editar cambiando foto y verificar que queda centralizada;
6. revisar la carpeta visible del dispositivo.
