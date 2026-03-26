package com.jose.listacompra.ui.navigation

// ui/navigation/Routes.kt
sealed class Route(val route: String) {

    object ShoppingList : Route("shopping_list")
    object Catalogo : Route("catalogo")
    object SplashScreen : Route("splashScreen")

    // Pantallas completas (antes eran "dialogs")
    object ImportTicket : Route("import_ticket")
    object ColorSettings : Route("color_settings")
    object ProductHistory : Route("product_history")
    object BarcodeScanner : Route("barcode_scanner")
}
