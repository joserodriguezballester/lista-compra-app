package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
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
    selectedSupermarketId: Long,
    onSupermarketSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = supermarkets.indexOfFirst { it.id == selectedSupermarketId }.coerceAtLeast(0)
    
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

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
            supermarkets.forEachIndexed { index, supermarket ->
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