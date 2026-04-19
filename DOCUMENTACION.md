# DOCUMENTACION.md

Documentación técnica viva de `lista-compra-app`.

## Puntos de entrada

- **Operativo / estado actual del trabajo:** `OPERATIVO.md`
- **Modelo de base de datos / entidad-relación:** `docs/modelo-bd-entidad-relacion.md`

## Notas rápidas

- La base de datos Room actual es **`shopping_list_db`** en versión **14**.
- El documento de BD distingue entre:
  - relaciones **forzadas por Room/FK**
  - relaciones **lógicas** que existen en código pero **no están protegidas por FK**
- Esto es importante para futuras migraciones, para el bloque de import/export y para no diseñar “de memoria”.
