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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Imagen (48.dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_photo_loading),
                        contentDescription = "Sin foto",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 2. Checkbox cuadrado
            Checkbox(
                checked = product.isPurchased,
                onCheckedChange = { onTogglePurchased() },
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Info del producto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Nombre en negrita
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🏷️ ${offer.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Estado de cumplimiento
                        if (offerStatus.needsMore) {
                            Text(
                                text = "⚠️ +${offerStatus.remaining}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "✅",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Cantidad | Precio | Total
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cantidad
                    Text(
                        text = "${product.quantity.toInt()} uds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Precio unitario
                    val unitPrice = product.estimatedPrice
                    if (unitPrice != null) {
                        Text(
                            text = "${String.format("%.2f", unitPrice)} €/ud",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Total
                    val total = product.finalPrice ?: (product.estimatedPrice?.let { it * product.quantity })
                    if (total != null) {
                        Text(
                            text = "${String.format("%.2f", total)} €",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Notas (si hay y contiene info de supermercado)
                if (product.notes.isNotBlank() && supermarketLogoRes == null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Logo de supermercado preferido
            if (supermarketLogoRes != null) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        painter = painterResource(id = supermarketLogoRes),
                        contentDescription = "Supermercado preferido",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(20.dp),
                        tint = androidx.compose.ui.graphics.Color.Unspecified
                    )
                }
            }
        }
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