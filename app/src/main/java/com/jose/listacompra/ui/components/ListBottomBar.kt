package com.jose.listacompra.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jose.listacompra.R
import com.jose.listacompra.domain.model.Supermarket

/**
 * Bottom bar para Mi Lista
 * Muestra Home + logos de supermercados
 * Solo iconos, sin texto
 * 
 * T4: Añadido "Todos" (null) al principio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListBottomBar(
    supermarkets: List<Supermarket>,
    selectedSupermarketId: Long?,
    onSupermarketSelected: (Long?) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(56.dp),
        tonalElevation = 8.dp
    ) {
        // T4: "Todos" - mostrar todos los productos
        NavigationBarItem(
            selected = selectedSupermarketId == null,
            onClick = { onSupermarketSelected(null) },
            icon = {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = "Todos"
                )
            }
        )
        
        // Home
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        )
        
        // Supermercados (sin "Cualquiera" id=0)
        supermarkets.filter { it.id > 0 }.forEach { supermarket ->
            NavigationBarItem(
                selected = supermarket.id == selectedSupermarketId,
                onClick = { onSupermarketSelected(supermarket.id) },
                icon = {
                    SupermarketLogo(
                        supermarketName = supermarket.name,
                        emoji = supermarket.emoji,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SupermarketLogo(
    supermarketName: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val logoResId = when (supermarketName.lowercase()) {
        "carrefour" -> R.drawable.logo_carrefour
        "meradona", "mercadona" -> R.drawable.logo_mercadona
        "lidl" -> R.drawable.logo_lidl
        "aldi" -> R.drawable.logo_aldi
        "dia" -> R.drawable.logo_dia
        else -> null
    }
    
    if (logoResId != null) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = supermarketName,
            modifier = modifier
        )
    } else {
        // Fallback: usar emoji
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        )
    }
}
