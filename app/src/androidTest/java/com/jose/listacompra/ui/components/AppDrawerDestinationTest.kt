package com.jose.listacompra.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jose.listacompra.ui.navigation.DrawerDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppDrawerDestinationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clicking_importar_ticket_emits_ticket_destination() {
        var selected: DrawerDestination? = null

        composeTestRule.setContent {
            MaterialTheme {
                AppDrawer(
                    currentDestination = DrawerDestination.Home,
                    onDestinationSelected = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Importar Ticket").performClick()
        assertEquals(DrawerDestination.TicketImport, selected)
    }

    @Test
    fun current_destination_is_rendered_as_selected() {
        composeTestRule.setContent {
            MaterialTheme {
                AppDrawer(
                    currentDestination = DrawerDestination.History,
                    onDestinationSelected = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Historial").assertIsSelected()
    }

    @Test
    fun clicking_home_and_list_emit_expected_destinations() {
        var lastSelected: DrawerDestination? = null

        composeTestRule.setContent {
            MaterialTheme {
                AppDrawer(
                    currentDestination = null,
                    onDestinationSelected = { lastSelected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Home").performClick()
        assertEquals(DrawerDestination.Home, lastSelected)

        composeTestRule.onNodeWithText("Mi Lista").performClick()
        assertEquals(DrawerDestination.ShoppingList, lastSelected)
    }
}
