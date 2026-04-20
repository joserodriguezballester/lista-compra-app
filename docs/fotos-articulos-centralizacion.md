# Centralización de fotos nuevas de artículos

## Resumen

Plan para centralizar **las fotos nuevas de artículos** en una carpeta visible y canónica del dispositivo, sin migrar por ahora las fotos antiguas ni abrir una migración de Room innecesaria.

## Decisión vigente

- **Alcance de esta ronda:** solo fotos nuevas de artículos.
- **Visibilidad:** la carpeta debe ser **visible** desde Archivos / Galería.
- **Migración de fotos antiguas:** **no** en esta fase.
- **Referencia en BD:** se mantiene `photoUri: String?`.

## Problema actual

Hoy la app acepta fotos de artículo desde varias fuentes distintas:

- galería (`content://...`);
- cámara (en algunos flujos con fichero temporal en caché o almacenamiento app-específico);
- escáner / OpenFoodFacts (URL remota `https://...`).

Eso deja el sistema con referencias heterogéneas y con riesgo real de fotos temporales o poco estables, especialmente cuando la cámara usa caché.

## Objetivo funcional

Toda foto nueva de artículo debe acabar guardada en una **ubicación canónica única** del dispositivo.

Carpeta objetivo:

- **`Pictures/ListaCompra/Articulos/`**

La app seguirá guardando en BD solo la referencia (`photoUri`), pero esa referencia deberá corresponder a la copia **centralizada y estable**.

## Regla operativa

La normalización de la imagen se hará **al guardar el artículo**, no en el momento de selección de preview.

### Casos de entrada

- **Galería** -> copiar imagen a la carpeta canónica.
- **Cámara** -> copiar o mover la imagen temporal a la carpeta canónica.
- **Escáner / OpenFoodFacts** -> descargar la imagen remota y guardarla en la carpeta canónica al guardar el artículo.
- **Imagen ya centralizada** -> no duplicarla.

## Diseño propuesto

### 1. Pieza de almacenamiento dedicada

Crear una pieza tipo:

- `ArticuloPhotoStorage`
  o, si se prefiere más genérico,
- `PhotoStorage`

Responsabilidades:

- detectar si una imagen ya está centralizada;
- importar una imagen desde `Uri` local;
- descargar una imagen desde URL remota;
- generar la referencia final canónica que se persistirá en BD.

### 2. Mantener la UI ligera

La UI actual puede seguir usando la imagen temporal/remota para preview.

La lógica de almacenamiento **no** debería vivir en la UI, sino ejecutarse al confirmar `Guardar`.

### 3. Punto de integración principal

Integrar la centralización en:

- `SaveArticuloUseCase`
- `UpdateArticuloUseCase`

Comportamiento esperado:

- **alta** -> si la foto no es canónica, centralizar antes de persistir;
- **edición sin cambio de foto** -> no duplicar;
- **edición con foto nueva** -> centralizar la nueva referencia antes de guardar;
- **eliminar foto** -> persistir `null`.

## Impacto en datos

### Room / esquema

No debería hacer falta migración de BD en esta fase porque:

- `articulos.photoUri` ya existe;
- el tipo almacenado sigue siendo `String?`.

### Nombres de fichero

Usar nombres robustos y desacoplados del nombre del artículo, por ejemplo:

- `articulo-<uuid>.jpg`

## Visibilidad en Android

Como la carpeta debe ser visible, la implementación debe apoyarse en almacenamiento público de imágenes (p. ej. `MediaStore` en `Pictures/ListaCompra/Articulos`) y no en caché efímera.

## Fuera de alcance en esta fase

- migrar fotos antiguas;
- borrar automáticamente fotos antiguas sustituidas;
- meter los binarios de imagen dentro del backup JSON;
- centralizar todavía todas las fotos de `products` salvo el rebote natural desde `articulos`.

## Nota importante sobre backup

El backup lógico JSON actual exporta la **URI** de la imagen, pero **no** el fichero binario asociado.

Por tanto, este plan mejora el almacenamiento local en el dispositivo, pero **no resuelve todavía** la portabilidad completa de imágenes entre dispositivos vía export/import.

## Validación manual mínima

1. Crear artículo con foto desde galería.
2. Crear artículo con foto desde cámara.
3. Crear artículo desde escáner con imagen remota.
4. Editar artículo sin cambiar la foto y comprobar que no se duplica.
5. Editar artículo cambiando la foto y comprobar que la nueva queda centralizada.
6. Reiniciar la app y verificar que la imagen sigue cargando.
7. Verificar en Archivos / Galería que la carpeta visible existe y recibe imágenes.

## Orden sugerido de implementación

1. Crear `ArticuloPhotoStorage`.
2. Inyectarlo con Hilt.
3. Integrarlo en `SaveArticuloUseCase`.
4. Integrarlo en `UpdateArticuloUseCase`.
5. Compilar.
6. Probar alta y edición manual.
7. Documentar el resultado final si el comportamiento queda bueno.
