package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Supermarket

// Extensión para tabIndicatorOffset
@Composable
fun Modifier.tabIndicatorOffset(
    currentTabPosition: TabPosition
): Modifier = this
    .fillMaxWidth()
    .wrapContentSize(align = androidx.compose.ui.Alignment.BottomStart)
    .offset(x = currentTabPosition.left)
    .width(currentTabPosition.width)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketBottomBar(
    supermarkets: List<Supermarket>,
    selectedSupermarketId: Long?,
    onSupermarketSelected: (Long?) -> Unit,
    onHomeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // T4: "Todos" (null) como primer tab
    val showAllSelected = selectedSupermarketId == null
    
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    // Calcular índice seleccionado
    val selectedIndex = when {
        showAllSelected -> 0 // "Todos"
        onHomeClick != null -> 1 // Home
        else -> {
            val idx = supermarkets.indexOfFirst { it.id == selectedSupermarketId }
            if (idx >= 0) idx + (if (onHomeClick != null) 2 else 1) else 0
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surfaceColor,
        tonalElevation = 3.dp
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = surfaceColor,
            contentColor = onSurfaceColor,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        height = 3.dp,
                        shape = MaterialTheme.shapes.small,
                        color = primaryColor
                    )
                }
            },
            divider = {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = outlineVariantColor.copy(alpha = 0.5f)
                )
            }
        ) {
            // T4: Tab "Todos" (null = mostrar todos los productos)
            Tab(
                selected = showAllSelected,
                onClick = { onSupermarketSelected(null) },
                text = {
                    Text(
                        text = "📦 Todos",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (showAllSelected) primaryColor else onSurfaceVariantColor
                    )
                },
                selectedContentColor = primaryColor,
                unselectedContentColor = onSurfaceVariantColor,
                modifier = Modifier.drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = dividerColor,
                        start = Offset(size.width, 12.dp.toPx()),
                        end = Offset(size.width, size.height - 12.dp.toPx()),
                        strokeWidth = strokeWidth
                    )
                }
            )
            
            // Tab de Home (si está habilitado)
            if (onHomeClick != null) {
                Tab(
                    selected = false,
                    onClick = onHomeClick,
                    text = {
                        Text(
                            text = "🏠 Home",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = onSurfaceVariantColor
                        )
                    },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = onSurfaceVariantColor,
                    modifier = Modifier.drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = dividerColor,
                            start = Offset(size.width, 12.dp.toPx()),
                            end = Offset(size.width, size.height - 12.dp.toPx()),
                            strokeWidth = strokeWidth
                        )
                    }
                )
            }
            
            // Tabs de supermercados (sin "Cualquiera")
            supermarkets.filter { it.id > 0 }.forEachIndexed { index, supermarket ->
                val isSelected = supermarket.id == selectedSupermarketId
                
                Tab(
                    selected = isSelected,
                    onClick = { onSupermarketSelected(supermarket.id) },
                    text = {
                        Text(
                            text = "${supermarket.emoji} ${supermarket.name}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) primaryColor else onSurfaceVariantColor
                        )
                    },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = onSurfaceVariantColor,
                    modifier = Modifier.drawBehind {
                        if (index < supermarkets.size - 1) {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = dividerColor,
                                start = Offset(size.width, 12.dp.toPx()),
                                end = Offset(size.width, size.height - 12.dp.toPx()),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                )
            }
        }
    }
}