package com.jose.listacompra.data.local.dataseeder

 val articulosBase = listOf(
    // === ARTÍCULOS DEL TICKET (2026-04-09) ===
    
    // Aceites y Grasas
    SeedArticulo("Aceite de Girasol 5L", 23, 10.75f, 5f, "L", "8410100033833",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100033833_01.jpg"),

    // Limpieza e Higiene
    SeedArticulo("Fairy Poder 900ML", 36, 5.39f, 900f, "ml", "8006540833162",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8006540833162_01.jpg"),
    SeedArticulo("Papel Aluminio 50M", 36, 7.99f, 50f, "m", "8410100021052",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100021052_01.jpg"),
    SeedArticulo("Papel Cocina Foxy", 36, 2.75f, 3f, "ud", "8004260492143",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8004260492143_01.jpg"),
    SeedArticulo("Recambios Maquinilla", 35, 3.45f, 1f, "ud", null, null),

    // Bebidas y Café
    SeedArticulo("Cafe Cortado Nescafé", 3, 8.35f, 16f, "ud", "7613032431442",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/7613032431442_01.jpg"),
    SeedArticulo("Cafe Capuccino 250ML", 3, 0.79f, 250f, "ml", "8410100055262",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100055262_01.jpg"),

    // Aperitivos y Snacks
    SeedArticulo("Cheetos Pelotazos", 29, 1.89f, 130f, "g", "8410199003311",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410199003311_01.jpg"),
    SeedArticulo("Drakis Pandilla 75G", 29, 1.89f, 75f, "g", "8410199005391",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410199005391_01.jpg"),
    SeedArticulo("Patatas Onduladas 170", 29, 0.89f, 170f, "g", "8410100010015",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100010015_01.jpg"),

    // Lácteos, Quesos y Postres
    SeedArticulo("Leche Semidesnatada", 11, 0.88f, 1f, "L", "8410100010015",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100010015_01.jpg"),
    SeedArticulo("Mozzarella Carrefour", 8, 0.89f, 125f, "g", "3245412151676",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/3245412151676_01.jpg"),
    SeedArticulo("Queso Tierno Lonchas", 8, 1.95f, 1f, "ud", "8410103529364",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410103529364_01.jpg"),
    SeedArticulo("Flan de Café 4X100", 12, 2.69f, 400f, "g", "8410100052308",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100052308_01.jpg"),
    SeedArticulo("Natillas Vainilla X4", 12, 0.99f, 400f, "g", "8410100021656",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100021656_01.jpg"),

    // Platos Preparados y Pizza
    SeedArticulo("Pizza Campofrio 365G", 26, 3.19f, 365f, "g", "8410510340352",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410510340352_01.jpg"),
    SeedArticulo("Tortilla Palacios", 21, 5.95f, 650f, "g", "8410920002161",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410920002161_01.jpg"),
    SeedArticulo("Salsa Alioli Chovi", 28, 3.15f, 250f, "ml", "8410558000300",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410558000300_01.jpg"),

    // Despensa (Pasta, Conservas, Legumbres)
    SeedArticulo("Fideua Carrefour 500", 24, 0.80f, 500f, "g", "8410100015501",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100015501_01.jpg"),

    // === ARTÍCULOS BASE ORIGINALES ===

    SeedArticulo("Leche Entera Carrefour", 15, 1.15f, 1f, "L", "8410100010015",
        "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100010015_01.jpg"),

    // CORREGIDO: Huevos → Lácteos y Huevos (15), no Carnicería (3)
    SeedArticulo("Huevos Docena L", 15, 2.50f, 12f, "ud", null,
        "https://images.pexels.com/photos/162712/egg-white-food-simple-162712.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Pan de Molde Blanco", 14, 1.80f, 1f, "paquete", null,
        "https://images.pexels.com/photos/1586942/pexels-photo-1586942.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Arroz Redondo 1kg", 9, 1.30f, 1f, "kg", "8410231234567",
        "https://images.pexels.com/photos/4187621/pexels-photo-4187621.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Pasta Fideuá n2", 9, 1.20f, 500f, "g", null,
        "https://images.pexels.com/photos/5692131/pexels-photo-5692131.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Tomate Frito Receta Artesana", 8, 2.10f, 1f, "ud", null,
        "https://jcarrefour.vtexassets.com/arquivos/ids/135932/8410022200235_01.jpg"),

    SeedArticulo("Aceite de Oliva Virgen Extra", 9, 9.50f, 1f, "L", "8423456789012",
        "https://images.pexels.com/photos/33783/olive-oil-salad-dressing-cooking-oil.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    // CORREGIDO: Detergente → Droguería y Limpieza (11), no Higiene (1)
    SeedArticulo("Detergente Gel Activo", 11, 6.99f, 3f, "L", null,
        "https://via.placeholder.com/300/4F46E5/FFFFFF?text=Detergente"),

    // CORREGIDO: Papel Higiénico → Papel (10), no Higiene (1)
    SeedArticulo("Papel Higiénico 12 rollos", 10, 4.50f, 1f, "paquete", null,
        "https://via.placeholder.com/300/F9FAFB/333333?text=Papel+Higienico"),

    SeedArticulo("Pechuga de Pollo Bandeja", 3, 5.50f, 1f, "kg", null,
        "https://images.pexels.com/photos/618775/pexels-photo-618775.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Manzanas Golden", 2, 1.95f, 1f, "kg", null,
        "https://images.pexels.com/photos/102104/pexels-photo-102104.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Plátano de Canarias", 2, 2.10f, 1f, "kg", null,
        "https://images.pexels.com/photos/2872767/pexels-photo-2872767.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),

    SeedArticulo("Queso Rallado Emmental", 17, 1.85f, 200f, "g", null,
        "https://via.placeholder.com/300/FEF3C7/92400E?text=Queso+Rallado"),

    SeedArticulo("Yogur Natural Pack 8", 15, 1.40f, 1f, "paquete", null,
        "https://via.placeholder.com/300/FFFFFF/4F46E5?text=Yogur+Natural"),

    SeedArticulo("Cerveza Premium Pack 6", 12, 3.90f, 6f, "ud", "8412345678901",
        "https://images.pexels.com/photos/1552630/pexels-photo-1552630.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1")
)