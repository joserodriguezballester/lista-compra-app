package com.jose.listacompra.ui.navigation

import androidx.navigation.NavHostController

class AppNavigatorImpl(
    private val navController: NavHostController
) : AppNavigator {
    override fun navigateTo(destination: DrawerDestination) {
        when (destination) {
            DrawerDestination.Home -> navController.navigate(NavScreen.Home.route) {
                launchSingleTop = true
                popUpTo(NavScreen.Home.route) { inclusive = false }
            }
            DrawerDestination.ShoppingList -> navController.navigate(NavScreen.ShoppingList.route) {
                launchSingleTop = true
                popUpTo(NavScreen.Home.route) { inclusive = false }
            }
            DrawerDestination.Catalog -> navController.navigate(NavScreen.Catalogo.route) { launchSingleTop = true }
            DrawerDestination.Categories -> navController.navigate(NavScreen.Categories.route) { launchSingleTop = true }
            DrawerDestination.Offers -> navController.navigate(NavScreen.Offers.route) { launchSingleTop = true }
            DrawerDestination.Supermarkets -> navController.navigate(NavScreen.Supermarkets.route) { launchSingleTop = true }
            DrawerDestination.History -> navController.navigate(NavScreen.History.route) { launchSingleTop = true }
            DrawerDestination.TicketImport -> navController.navigate(NavScreen.TicketImport.route) { launchSingleTop = true }
        }
    }
}
