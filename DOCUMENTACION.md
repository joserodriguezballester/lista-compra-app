# DOCUMENTACION.md

Documentación técnica viva de `lista-compra-app`.

## Puntos de entrada

- **Operativo / estado actual del trabajo:** `OPERATIVO.md`
- **Uso del import/export de datos de usuario:** `docs/import-export-datos-uso.md`
- **Técnica del import/export de datos de usuario:** `docs/import-export-datos-tecnico.md`
- **Modelo de base de datos / entidad-relación (texto):** `docs/modelo-bd-entidad-relacion.md`
- **Modelo de base de datos / entidad-relación (diagrama):** `docs/modelo-bd-diagrama-er.md`
- **Navegación actual / drawer / rutas principales:** `docs/navegacion.md`
- **Centralización de fotos de artículos (comportamiento actual):** `docs/fotos-articulos-centralizacion.md`

## Notas rápidas

- La base de datos Room actual es **`shopping_list_db`** en versión **14**.
- El documento de BD distingue entre:
  - relaciones **forzadas por Room/FK**
  - relaciones **lógicas** que existen en código pero **no están protegidas por FK**
- Esto es importante para futuras migraciones, para el bloque de import/export y para no diseñar “de memoria”.
