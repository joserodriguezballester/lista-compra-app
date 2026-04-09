package com.jose.listacompra.ui.screens.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * VERSIÓN PREVIEW - Diseño moderno M3 de ArticuloCard
 * 
 * Cambios aplicados:
 * - Sin bordes pesados
 * - Relación de aspecto 1:1 en imagen
 * - Bordes redondeados 20.dp
 * - Jerarquía tipográfica clara
 * - Badge de categoría como AssistChip suave
 * - Fondo de imagen con color primario suave
 */
@Composable
fun ArticuloCardPreview(
    name: String = "Aceite de Girasol Carrefour 5L",
    size: Float = 5f,
    unit: String = "L",
    price: Float = 10.75f,
    imageUrl: String? = "https://jcarrefour.vtexassets.com/arquivos/ids/2202611/8410100033833_01.jpg",
    categoryIcon: String = "🫒",
    categoryName: String = "Aceites",
    hasEan: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Área de Imagen - Relación 1:1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Cuadrada perfecta
                    .background(
                        // Color derivado del primario muy suave
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder con icono grande
                    Text(
                        text = categoryIcon,
                        style = MaterialTheme.typography.displayLarge
                    )
                }

                // Badge de EAN en esquina superior derecha
                if (hasEan) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Tiene EAN",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Título - 2 líneas máximo
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    minLines = 2, // Mantiene altura constante
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Cantidad/Peso - secundario
                Text(
                    text = "${size.format(1)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categoría como AssistChip suave
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "$categoryIcon $categoryName",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(24.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Precio - Protagonista
                Text(
                    text = "${String.format("%.2f", price)} €",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Grid de preview para ver varias cards juntas
 */
@Composable
fun ArticuloCardPreviewGrid() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1
            Box(modifier = Modifier.weight(1f)) {
                ArticuloCardPreview(
                    name = "Aceite Girasol 5L",
                    price = 10.75f,
                    categoryIcon = "🫒",
                    categoryName = "Aceites"
                )
            }
            
            // Card 2
            Box(modifier = Modifier.weight(1f)) {
                ArticuloCardPreview(
                    name = "Leche Semidesnatada Carrefour",
                    size = 1f,
                    unit = "L",
                    price = 0.88f,
                    categoryIcon = "🥛",
                    categoryName = "Lácteos"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 3 - Sin imagen
            Box(modifier = Modifier.weight(1f)) {
                ArticuloCardPreview(
                    name = "Banana Granel",
                    size = 1f,
                    unit = "kg",
                    price = 1.54f,
                    imageUrl = null,
                    categoryIcon = "🍌",
                    categoryName = "Frutas"
                )
            }
            
            // Card 4 - Nombre largo
            Box(modifier = Modifier.weight(1f)) {
                ArticuloCardPreview(
                    name = "Pizza Cuatro Quesos Campofrio Especial",
                    size = 365f,
                    unit = "g",
                    price = 3.19f,
                    categoryIcon = "🍕",
                    categoryName = "Platos Prep."
                )
            }
        }
    }
}

// Extension helper
private fun Float.format(decimals: Int): String = String.format("%.${decimals}f", this)
