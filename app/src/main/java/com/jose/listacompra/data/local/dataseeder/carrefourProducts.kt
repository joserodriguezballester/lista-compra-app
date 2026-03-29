package com.jose.listacompra.data.local.dataseeder

/**
 * Productos de la lista "lista-carro-buena.txt" (Carrefour Mislata)
 * Precios estimados basados en el archivo
 */
 val carrefourProducts = listOf(
    // HIGIENE Y BELLEZA (ID: 1)
    SeedProduct("Máquina de afeitar", 1, 1f, 10f),

    // PANADERÍA (mapeado a BOLLERÍA ID: 14)
    SeedProduct("Pan de payés", 14, 1f, null),
    SeedProduct("Pan", 14, 1f, null),

    // FRUTA Y VERDURA (ID: 2)
    SeedProduct("Tomates", 2, 1f, null),
    SeedProduct("Calabacines", 2, 2f, null),
    SeedProduct("Berenjenas", 2, 2f, null),
    SeedProduct("Pimientos rojos", 2, 2f, null),
    SeedProduct("Plátanos", 2, 1f, 1.50f),
    SeedProduct("Manzana Golden", 2, 1f, 1.80f),
    SeedProduct("Manzana roja", 2, 1f, 1.80f),
    SeedProduct("Uva", 2, 1f, 2.50f),
    SeedProduct("Brócoli", 2, 1f, 1.50f),
    SeedProduct("Cebolla", 2, 1f, 1f),

    // CHARCUTERÍA (ID: 3)
    SeedProduct("Taquitos de jamón", 3, 2f, 2.99f),
    SeedProduct("Taquitos de chorizo", 3, 2f, 2.15f),
    SeedProduct("Huevos", 3, 1f, 2.50f),

    // DESPENSA - GALLETAS (ID: 5)
    SeedProduct("Galletas María", 5, 1f, 2.50f),

    // DESPENSA - AZUCAR Y CAFÉ (ID: 7)
    SeedProduct("Sal fina", 7, 1f, 0.70f),
    SeedProduct("Azúcar", 7, 1f, 1.30f),

    // DESPENSA - TOMATE Y LEGUMBRES (ID: 8)
    SeedProduct("Tomate de la abuela", 8, 1f, 1.10f),

    // DESPENSA - ACEITE Y PASTAS (ID: 9)
    SeedProduct("Fideuá", 9, 1f, 1.20f),
    SeedProduct("Starlux", 9, 1f, 1.50f),

    // BEBIDAS (ID: 12)
    SeedProduct("Zumo", 12, 1f, 1.75f),
    SeedProduct("Gaseosas", 12, 1f, 1.50f),
    SeedProduct("Batidos", 12, 1f, 2f),

    // LÁCTEOS (ID: 15)
    SeedProduct("Leche", 15, 6f, 1.15f),

    // PREPARADOS (ID: 16)
    SeedProduct("Capuchinos", 16, 5f, 2.50f),
    SeedProduct("Pizza", 16, 1f, 3.50f),

    // QUESOS (ID: 17)
    SeedProduct("Queso fresco", 17, 2f, 2.30f),
    SeedProduct("Queso rallado", 17, 2f, 2.00f),

    // REGALO (ID: 18)
    SeedProduct("Queso (fidelización)", 18, 1f, 0f)
)