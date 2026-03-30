package com.jose.listacompra.data.local.dataseeder

 val articulosBase = listOf(
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