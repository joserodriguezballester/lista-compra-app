package com.jose.listacompra.ui.navigation

sealed class DrawerDestination(
    val route: String,
    val label: String,
    val emoji: String
) {
    data object Home : DrawerDestination(NavScreen.Home.route, "Home", "🏠")
    data object ShoppingList : DrawerDestination(NavScreen.ShoppingList.route, "Mi Lista", "📝")
    data object Catalog : DrawerDestination(NavScreen.Catalogo.route, "Catálogo", "📦")
    data object Categories : DrawerDestination(NavScreen.Categories.route, "Categorías", "📂")
    data object Offers : DrawerDestination(NavScreen.Offers.route, "Ofertas", "🏷️")
    data object Supermarkets : DrawerDestination(NavScreen.Supermarkets.route, "Supermercados", "🏪")
    data object History : DrawerDestination(NavScreen.History.route, "Historial", "📊")
    data object TicketImport : DrawerDestination(NavScreen.TicketImport.route, "Importar Ticket", "🧾")

    companion object {
        val all = listOf(
            Home,
            ShoppingList,
            Catalog,
            Categories,
            Offers,
            Supermarkets,
            History,
            TicketImport
        )
    }
}
