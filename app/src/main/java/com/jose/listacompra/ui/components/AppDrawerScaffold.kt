package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.jose.listacompra.ui.navigation.AppNavigator
import com.jose.listacompra.ui.navigation.DrawerDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScaffold(
    title: String,
    navigator: AppNavigator? = null,
    currentDestination: DrawerDestination? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTicketImport: () -> Unit = {},
    onMicrophoneClick: ((android.content.Context, kotlinx.coroutines.CoroutineScope) -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onChangeColor: () -> Unit = {},
    onToggleDarkMode: (() -> Unit)? = null,
    isDarkMode: Boolean = false,
    showVersionInOverflow: Boolean = true,
    overflowActions: @Composable (Expanded: Boolean, onDismiss: () -> Unit) -> Unit = { _, _ -> },
    topBar: (@Composable ((openDrawer: () -> Unit, context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope) -> Unit))? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun handleDestination(destination: DrawerDestination) {
        if (navigator != null && currentDestination != null) {
            if (destination != currentDestination) navigator.navigateTo(destination)
            return
        }
        when (destination) {
            DrawerDestination.Home -> onNavigateToHome()
            DrawerDestination.ShoppingList -> onNavigateToList()
            DrawerDestination.Catalog -> onNavigateToCatalogo()
            DrawerDestination.Categories -> onNavigateToCategories()
            DrawerDestination.Offers -> onNavigateToOffers()
            DrawerDestination.Supermarkets -> onNavigateToSupermarkets()
            DrawerDestination.History -> onNavigateToHistory()
            DrawerDestination.TicketImport -> onNavigateToTicketImport()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    scope.launch {
                        drawerState.close()
                        handleDestination(destination)
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                topBar?.invoke({ scope.launch { drawerState.open() } }, context, scope) ?: CommonTopBar(
                    title = title,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onMicrophoneClick = onMicrophoneClick?.let { { it(context, scope) } },
                    onAddClick = onAddClick,
                    onChangeColor = onChangeColor,
                    onToggleDarkMode = onToggleDarkMode,
                    isDarkMode = isDarkMode,
                    showVersionInOverflow = showVersionInOverflow,
                    overflowActions = overflowActions
                )
            },
            bottomBar = bottomBar
        ) { padding ->
            content(padding)
        }
    }
}
