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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScaffold(
    title: String,
    onNavigateToHome: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToOffers: () -> Unit = {},
    onNavigateToSupermarkets: () -> Unit = {},
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTicketImport: () -> Unit = {},
    isCurrentHome: Boolean = false,
    isCurrentList: Boolean = false,
    isCurrentOffers: Boolean = false,
    isCurrentSupermarkets: Boolean = false,
    isCurrentCatalogo: Boolean = false,
    isCurrentCategories: Boolean = false,
    isCurrentHistory: Boolean = false,
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

    fun closeThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                onNavigateToHome = {
                    if (isCurrentHome) scope.launch { drawerState.close() } else closeThen(onNavigateToHome)
                },
                onNavigateToList = {
                    if (isCurrentList) scope.launch { drawerState.close() } else closeThen(onNavigateToList)
                },
                onNavigateToOffers = {
                    if (isCurrentOffers) scope.launch { drawerState.close() } else closeThen(onNavigateToOffers)
                },
                onNavigateToSupermarkets = {
                    if (isCurrentSupermarkets) scope.launch { drawerState.close() } else closeThen(onNavigateToSupermarkets)
                },
                onNavigateToCatalogo = {
                    if (isCurrentCatalogo) scope.launch { drawerState.close() } else closeThen(onNavigateToCatalogo)
                },
                onNavigateToCategories = {
                    if (isCurrentCategories) scope.launch { drawerState.close() } else closeThen(onNavigateToCategories)
                },
                onNavigateToHistory = {
                    if (isCurrentHistory) scope.launch { drawerState.close() } else closeThen(onNavigateToHistory)
                },
                onNavigateToTicketImport = { closeThen(onNavigateToTicketImport) }
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
