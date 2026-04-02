package com.jose.listacompra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jose.listacompra.R
import com.jose.listacompra.domain.model.Product
import com.jose.listacompra.domain.model.Offer

@Composable
fun ProductCard(
    product: Product,
    offer: Offer? = null,
    onClick: () -> Unit,
    onTogglePurchased: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (product.isPurchased) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    // Obtener logo de supermercado desde notas
    val supermarketLogoRes = getSupermarketLogo(product.notes)

    // Calcular si se cumple la oferta
    val offerStatus = calculateOfferStatus(product.quantity, offer)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // FILA SUPERIOR: Imagen + Checkbox + Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del producto (64dp)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!product.photoUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = product.photoUri,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder con emoji según categoría
                        Text(
                            text = getCategoryEmoji(product.name),
                            fontSize = 28.sp
                        )
                    }
                }

                // Checkbox (centrado)
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = { onTogglePurchased() },
                    modifier = Modifier.size(36.dp)
                )

                // Logo supermercado (si hay)
                if (supermarketLogoRes != null) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            painter = painterResource(id = supermarketLogoRes),
                            contentDescription = "Supermercado",
                            modifier = Modifier.padding(4.dp),
                            tint = androidx.compose.ui.graphics.Color.Unspecified
                        )
                    }
                } else if (product.notes.isNotBlank()) {
                    // Si no hay logo pero hay notas, mostrar indicador
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📝", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECCIÓN DE TEXTO (abajo)
            // Nombre
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                color = if (product.isPurchased)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurface
            )

            // Oferta (si hay)
            if (offer != null && !product.isPurchased) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge de oferta
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "🏷️ ${offer.name}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Estado de cumplimiento
                    if (offerStatus.needsMore) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "⚠️ Necesitas +${offerStatus.remaining}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else {
                        Text(
                            text = "✅ Oferta aplicada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Precio y cantidad
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cantidad y precio unitario
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${product.quantity.toInt()} uds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    product.estimatedPrice?.let { price ->
                        Text(
                            text = "${String.format("%.2f", price)} €/ud",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total
                val total = product.finalPrice ?: (product.estimatedPrice?.let { it * product.quantity })
                if (total != null) {
                    Text(
                        text = "${String.format("%.2f", total)} €",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Obtiene emoji según el nombre del producto
 */
private fun getCategoryEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("leche") -> "🥛"
        lower.contains("pan") -> "🍞"
        lower.contains("huevo") -> "🥚"
        lower.contains("yogur") -> "🥛"
        lower.contains("queso") -> "🧀"
        lower.contains("tomate") -> "🍅"
        lower.contains("platano", "plátano") -> "🍌"
        lower.contains("manzana") -> "🍎"
        lower.contains("naranja") -> "🍊"
        lower.contains("pollo") -> "🍗"
        lower.contains("carne") -> "🥩"
        lower.contains("pescado") -> "🐟"
        lower.contains("galleta") -> "🍪"
        lower.contains("café") -> "☕"
        lower.contains("aceite") -> "🫒"
        lower.contains("agua") -> "💧"
        lower.contains("cerveza") -> "🍺"
        lower.contains("vino") -> "🍷"
        lower.contains("detergente") -> "🧴"
        lower.contains("papel") -> "🧻"
        lower.contains("jabón") -> "🧼"
        else -> "📦"
    }
}

/**
 * Calcula el estado de cumplimiento de una oferta
 */
private fun calculateOfferStatus(quantity: Float, offer: Offer?): OfferStatus {
    if (offer == null) return OfferStatus(needsMore = false, remaining = 0)
    
    val qty = quantity.toInt()
    val minQty = when (offer.code) {
        "3x2" -> 3
        "2x1" -> 2
        "2nd_50" -> 2
        "2nd_70" -> 2
        "4x3" -> 4
        else -> 1
    }
    
    val needsMore = qty < minQty
    val remaining = if (needsMore) minQty - qty else 0
    
    return OfferStatus(needsMore = needsMore, remaining = remaining)
}

private data class OfferStatus(
    val needsMore: Boolean,
    val remaining: Int
)

/**
 * Obtiene el recurso del logo según el texto en notas
 */
@Composable
private fun getSupermarketLogo(notes: String): Int? {
    val lowerNotes = notes.lowercase()
    return when {
        lowerNotes.contains("mercadona") -> R.drawable.logo_mercadona
        lowerNotes.contains("carrefour") -> R.drawable.logo_carrefour
        lowerNotes.contains("lidl") -> R.drawable.logo_lidl
        lowerNotes.contains("aldi") -> R.drawable.logo_aldi
        lowerNotes.contains("dia") -> R.drawable.logo_dia
        lowerNotes.contains("consum") -> R.drawable.logo_consum
        else -> null
    }
}