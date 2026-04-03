package com.jose.listacompra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gráfico de evolución de precios de un producto
 */
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
    val dates = priceHistory.map { 
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.fecha))
    }
    
    val minPrice = prices.minOrNull() ?: 0f
    val maxPrice = prices.maxOrNull() ?: 0f
    val priceRange = maxPrice - minPrice
    val padding = if (priceRange == 0f) 0.1f else priceRange * 0.1f
    
    val chartMinPrice = minPrice - padding
    val chartMaxPrice = maxPrice + padding
    val chartRange = chartMaxPrice - chartMinPrice
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    Column(modifier = modifier) {
        // Título
        Text(
            "📈 Evolución del precio",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Gráfico
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val padding = 40.dp.toPx()
            
            val chartWidth = canvasWidth - padding * 2
            val chartHeight = canvasHeight - padding
            
            // Dibujar ejes
            drawLine(
                color = surfaceVariantColor,
                start = Offset(padding, 0f),
                end = Offset(padding, canvasHeight - padding),
                strokeWidth = 2.dp.toPx()
            )
            
            drawLine(
                color = surfaceVariantColor,
                start = Offset(padding, canvasHeight - padding),
                end = Offset(canvasWidth, canvasHeight - padding),
                strokeWidth = 2.dp.toPx()
            )
            
            // Dibujar líneas de referencia (precio mínimo y máximo)
            val minPriceY = canvasHeight - padding
            val maxPriceY = padding / 2
            
            drawLine(
                color = surfaceVariantColor.copy(alpha = 0.3f),
                start = Offset(padding, maxPriceY),
                end = Offset(canvasWidth - padding, maxPriceY),
                strokeWidth = 1.dp.toPx()
            )
            
            drawLine(
                color = surfaceVariantColor.copy(alpha = 0.3f),
                start = Offset(padding, minPriceY),
                end = Offset(canvasWidth - padding, minPriceY),
                strokeWidth = 1.dp.toPx()
            )
            
            // Dibujar etiquetas de precio
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    String.format("%.2f€", maxPrice),
                    5f,
                    maxPriceY + 10,
                    android.graphics.Paint().apply {
                        textSize = 10.sp.toPx()
                        color = android.graphics.Color.GRAY
                    }
                )
                drawText(
                    String.format("%.2f€", minPrice),
                    5f,
                    minPriceY - 5,
                    android.graphics.Paint().apply {
                        textSize = 10.sp.toPx()
                        color = android.graphics.Color.GRAY
                    }
                )
            }
            
            // Calcular puntos
            val points = prices.mapIndexed { index, price ->
                val x = padding + (index.toFloat() / (prices.size - 1)) * chartWidth
                val y = canvasHeight - padding - ((price - chartMinPrice) / chartRange) * chartHeight
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
                val isLast = index == points.lastIndex
                val isFirst = index == 0
                val isMin = prices[index] == minPrice
                val isMax = prices[index] == maxPrice
                
                val pointColor = when {
                    isMin -> Color(0xFF4CAF50) // Verde para mínimo
                    isMax -> errorColor // Rojo para máximo
                    isLast -> primaryColor // Último punto
                    else -> primaryColor.copy(alpha = 0.7f)
                }
                
                drawCircle(
                    color = pointColor,
                    radius = if (isMin || isMax) 8.dp.toPx() else 5.dp.toPx(),
                    center = point
                )
                
                // Círculo interior blanco
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
            
            // Dibujar fechas en eje X (solo algunas)
            val dateIndices = listOf(0, prices.size / 2, prices.size - 1)
            dateIndices.forEach { index ->
                if (index < dates.size) {
                    val x = padding + (index.toFloat() / (prices.size - 1)) * chartWidth
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            dates[index],
                            x - 20,
                            canvasHeight - padding + 20,
                            android.graphics.Paint().apply {
                                textSize = 9.sp.toPx()
                                color = android.graphics.Color.GRAY
                            }
                        )
                    }
                }
            }
        }
    }
}