package com.jose.listacompra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jose.listacompra.data.local.dao.PriceStats
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tarjeta con estadísticas de precios de un producto
 */
@Composable
fun PriceStatsCard(
    stats: PriceStats?,
    currentPrice: Float?,
    modifier: Modifier = Modifier
) {
    if (stats == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "Sin estadísticas disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "📊 Estadísticas de precio",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Mínimo", "${String.format("%.2f", stats.minPrice)} €")
                StatItem("Máximo", "${String.format("%.2f", stats.maxPrice)} €")
                StatItem("Media", "${String.format("%.2f", stats.avgPrice)} €")
            }
            
            if (currentPrice != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                val variation = if (stats.avgPrice > 0) {
                    ((currentPrice - stats.avgPrice) / stats.avgPrice) * 100
                } else 0f
                
                val variationColor = when {
                    variation < -5 -> MaterialTheme.colorScheme.primary // Más barato
                    variation > 5 -> MaterialTheme.colorScheme.error // Más caro
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                val variationText = when {
                    variation < -5 -> "👇 ${String.format("%.1f", kotlin.math.abs(variation))}% más barato que la media"
                    variation > 5 -> "👆 ${String.format("%.1f", variation)}% más caro que la media"
                    else -> "≈ Precio similar a la media"
                }
                
                Text(
                    variationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = variationColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                "Basado en ${stats.totalPurchases} compras",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}