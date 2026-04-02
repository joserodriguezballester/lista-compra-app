package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Supermarket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketBottomBar(
    supermarkets: List<Supermarket>,
    selectedSupermarketId: Long,
    onSupermarketSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = supermarkets.indexOfFirst { it.id == selectedSupermarketId }.coerceAtLeast(0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        height = 3.dp,
                        shape = MaterialTheme.shapes.small
                    )
                }
            },
            divider = {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        ) {
            supermarkets.forEachIndexed { index, supermarket ->
                Tab(
                    selected = supermarket.id == selectedSupermarketId,
                    onClick = { onSupermarketSelected(supermarket.id) },
                    text = {
                        Text(
                            text = "${supermarket.emoji} ${supermarket.name}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.drawBehind {
                        // Línea divisoria vertical entre tabs
                        if (index < supermarkets.size - 1) {
                            val strokeWidth = 1.dp.toPx()
                            val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            drawLine(
                                color = color,
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