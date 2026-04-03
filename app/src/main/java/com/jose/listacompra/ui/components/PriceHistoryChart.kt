package com.jose.listacompra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PriceHistoryChart(
    priceHistory: List<ProductPriceHistoryEntity>,
    modifier: Modifier = Modifier
) {
    if (priceHistory.isEmpty()) {
        Text(
            "Sin datos históricos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    
    if (priceHistory.size < 2) {
        Text(
            "Se necesitan al menos 2 registros",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    
    val prices = priceHistory.map { it.price }
    val dates = remember(priceHistory) {
        priceHistory.map { 
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.fecha))
        }
    }
    
    val minPrice = (prices.minOrNull() ?: 0f)
    val maxPrice = (prices.maxOrNull() ?: 0f)
    val priceRange = maxPrice - minPrice
    val padding = if (priceRange == 0f) 0.1f else priceRange * 0.1f
    
    val chartMinPrice = minPrice - padding
    val chartMaxPrice = maxPrice + padding
    val chartRange = chartMaxPrice - chartMinPrice
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    Column(modifier = modifier) {
        Text(
            "📈 Evolución del precio",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val paddingPx = 40.dp.toPx()
            
            val chartWidth = canvasWidth - paddingPx * 2
            val chartHeight = canvasHeight - paddingPx
            
            // Dibujar ejes
            drawLine(
                color = surfaceVariantColor,
                start = Offset(paddingPx, 0f),
                end = Offset(paddingPx, canvasHeight - paddingPx),
                strokeWidth = 2.dp.toPx()
            )
            
            drawLine(
                color = surfaceVariantColor,
                start = Offset(paddingPx, canvasHeight - paddingPx),
                end = Offset(canvasWidth, canvasHeight - paddingPx),
                strokeWidth = 2.dp.toPx()
            )
            
            // Calcular puntos
            val points = prices.mapIndexed { index, price ->
                val x = paddingPx + (index.toFloat() / (prices.size - 1)) * chartWidth
                val y = canvasHeight - paddingPx - ((price - chartMinPrice) / chartRange) * chartHeight
                Offset(x, y)
            }
            
            // Dibujar línea de precios
            if (points.size >= 2) {
                val path = Path()
                path.moveTo(points.first().x, points.first().y)
                
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }
                
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Dibujar puntos
            points.forEachIndexed { index, point ->
                val isMin = prices[index] == minPrice
                val isMax = prices[index] == maxPrice
                
                val pointColor = when {
                    isMin -> Color(0xFF4CAF50)
                    isMax -> errorColor
                    else -> primaryColor
                }
                
                drawCircle(
                    color = pointColor,
                    radius = if (isMin || isMax) 8.dp.toPx() else 5.dp.toPx(),
                    center = point
                )
                
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Etiquetas de precio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                String.format("%.2f€", minPrice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                String.format("%.2f€", maxPrice),
                style = MaterialTheme.typography.labelSmall,
                color = errorColor
            )
        }
    }
}