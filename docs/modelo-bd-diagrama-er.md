# Diagrama entidad-relación de `lista-compra-app`

Diagrama basado en las `Entity` reales de Room (`Database.kt` + `entities/*.kt`).

## 1. Relaciones con FK reales en Room

```mermaid
erDiagram
    SUPERMARKETS {
        long id PK
        string name
        string emoji
        boolean isDefault
    }

    AISLES {
        long id PK
        string name
        int orderIndex
        long supermarketId FK
        boolean isDefault
    }

    CATEGORIES {
        long id PK
        string name
        string icon
    }

    ARTICULOS {
        long id PK
        string name
        float basePrice
        string ean
        long categoryId
    }

    SHOPPING_LISTS {
        long id PK
        string name
        long supermarketId
        string estado
    }

    OFFERS {
        long id PK
        string code
        string name
        boolean isDefault
    }

    PRODUCTS {
        long id PK
        string name
        long shoppingListId FK
        long articuloId FK
        long supermarketId FK
        long aisleId
        long offerId
    }

    ARTICULO_SUPERMARKET_DEFAULTS {
        long id PK
        long articuloId FK
        long supermarketId FK
        long aisleId FK
    }

    CATEGORY_SUPERMARKET_ORDERS {
        long id PK
        long categoryId FK
        long supermarketId FK
        int orderIndex
    }

    TICKETS {
        long id PK
        long fecha
        long supermarketId FK
        float total
        boolean importado
    }

    TICKET_LINES {
        long id PK
        long ticketId FK
        long articuloId FK
        long categoriaId
        string nombreNormalizado
        float precioTotal
    }

    PURCHASE_HISTORY {
        long id PK
        long fecha
        float total
        string tienda
    }

    PRODUCT_PRICE_HISTORY {
        long id PK
        long purchaseId FK
        string productName
        float price
    }

    SUPERMARKETS ||--o{ AISLES : "FK supermarketId"
    SHOPPING_LISTS ||--o{ PRODUCTS : "FK shoppingListId"
    ARTICULOS o|--o{ PRODUCTS : "FK articuloId"
    SUPERMARKETS ||--o{ PRODUCTS : "FK supermarketId"

    ARTICULOS ||--o{ ARTICULO_SUPERMARKET_DEFAULTS : "FK articuloId"
    SUPERMARKETS ||--o{ ARTICULO_SUPERMARKET_DEFAULTS : "FK supermarketId"
    AISLES ||--o{ ARTICULO_SUPERMARKET_DEFAULTS : "FK aisleId"

    CATEGORIES ||--o{ CATEGORY_SUPERMARKET_ORDERS : "FK categoryId"
    SUPERMARKETS ||--o{ CATEGORY_SUPERMARKET_ORDERS : "FK supermarketId"

    SUPERMARKETS o|--o{ TICKETS : "FK supermarketId"
    TICKETS ||--o{ TICKET_LINES : "FK ticketId"
    ARTICULOS o|--o{ TICKET_LINES : "FK articuloId"

    PURCHASE_HISTORY ||--o{ PRODUCT_PRICE_HISTORY : "FK purchaseId"
```

## 2. Relaciones lógicas relevantes (sin FK real en Room)

```mermaid
erDiagram
    SUPERMARKETS {
        long id PK
        string name
    }

    CATEGORIES {
        long id PK
        string name
    }

    AISLES {
        long id PK
        string name
        long supermarketId
    }

    ARTICULOS {
        long id PK
        string name
        long categoryId
    }

    SHOPPING_LISTS {
        long id PK
        string name
        long supermarketId
    }

    OFFERS {
        long id PK
        string code
        string name
    }

    PRODUCTS {
        long id PK
        string name
        long aisleId
        long offerId
    }

    TICKET_LINES {
        long id PK
        long categoriaId
        string nombreNormalizado
    }

    PRODUCT_HISTORY {
        long id PK
        string name
        long aisleId
    }

    PRODUCT_FREQUENCY {
        long id PK
        string productName
        long lastSupermarketId
        long lastAisleId
        long preferredAisleId
    }

    CATEGORIES ||--o{ ARTICULOS : "categoryId (sin FK)"
    SUPERMARKETS o|--o{ SHOPPING_LISTS : "supermarketId (sin FK)"
    AISLES o|--o{ PRODUCTS : "aisleId (sin FK)"
    OFFERS o|--o{ PRODUCTS : "offerId (sin FK)"
    CATEGORIES o|--o{ TICKET_LINES : "categoriaId (sin FK)"
    AISLES o|--o{ PRODUCT_HISTORY : "aisleId (sin FK)"
    SUPERMARKETS o|--o{ PRODUCT_FREQUENCY : "lastSupermarketId (sin FK)"
    AISLES o|--o{ PRODUCT_FREQUENCY : "lastAisleId / preferredAisleId (sin FK)"
```

## 3. Nota rápida

- El primer diagrama recoge solo relaciones **forzadas por Room/FK**.
- El segundo recoge relaciones **lógicas usadas por la app**, pero que Room **no protege** con FK.
- Hay tablas históricas (`product_history`, `product_frequency` y parte de `product_price_history`) que además se apoyan bastante en nombres normalizados, no solo en IDs.
