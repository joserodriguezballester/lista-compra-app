package com.jose.listacompra.data.local.dataseeder

import com.jose.listacompra.data.local.entities.CategoryEntity

/**
 * Categorías alineadas con Open Food Facts
 * ID 1 = "Sin categoría" (fallback obligatorio)
 */
val categories = listOf(
    CategoryEntity(1, "Sin categoría", "📦"),
    CategoryEntity(2, "Alimentos infantiles", "👶"),
    CategoryEntity(3, "Bebidas", "🥤"),
    CategoryEntity(4, "Galletas y pasteles", "🍪"),
    CategoryEntity(5, "Panes", "🍞"),
    CategoryEntity(6, "Cereales desayuno", "🥣"),
    CategoryEntity(7, "Conservas", "🥫"),
    CategoryEntity(8, "Quesos", "🧀"),
    CategoryEntity(9, "Chocolates", "🍫"),
    CategoryEntity(10, "Condimentos", "🧂"),
    CategoryEntity(11, "Lácteos", "🥛"),
    CategoryEntity(12, "Postres", "🍮"),
    CategoryEntity(13, "Productos secos", "🫘"),
    CategoryEntity(14, "Huevos", "🥚"),
    CategoryEntity(15, "Grasas", "🧈"),
    CategoryEntity(16, "Pescado y marisco", "🐟"),
    CategoryEntity(17, "Congelados", "🧊"),
    CategoryEntity(18, "Frutas", "🍎"),
    CategoryEntity(19, "Verduras", "🥬"),
    CategoryEntity(20, "Carnes", "🥩"),
    CategoryEntity(21, "Comidas preparadas", "🍱"),
    CategoryEntity(22, "Frutos secos", "🥜"),
    CategoryEntity(23, "Aceites", "🫒"),
    CategoryEntity(24, "Pasta", "🍝"),
    CategoryEntity(25, "Encurtidos", "🥒"),
    CategoryEntity(26, "Pizza", "🍕"),
    CategoryEntity(27, "Alimentos vegetales", "🌱"),
    CategoryEntity(28, "Salsas", "🫙"),
    CategoryEntity(29, "Aperitivos", "🥨"),
    CategoryEntity(30, "Sopas", "🍲"),
    CategoryEntity(31, "Untables", "🍯"),
    CategoryEntity(32, "Azúcares", "🍬"),
    CategoryEntity(33, "Tés e infusiones", "🍵"),
    CategoryEntity(34, "Bebidas alcohólicas", "🍺"),
    CategoryEntity(35, "Higiene y belleza", "🧴"),
    CategoryEntity(36, "Limpieza", "🧼")
)
