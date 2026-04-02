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

    // Calcular total con oferta si aplica
    val total = calculateTotal(product, offer, offerStatus)

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
            // FILA SUPERIOR: Imagen + Checkbox + (Logo u Oferta)
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
                        Text(
                            text = getCategoryEmoji(product.name),
                            fontSize = 28.sp
                        )
                    }
                }

                // Checkbox (siempre visible y centrado)
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = { onTogglePurchased() },
                    modifier = Modifier.size(36.dp)
                )

                // Columna derecha: Logo (si hay) + Oferta (si hay)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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

                    // Oferta (si hay)
                    if (offer != null && !product.isPurchased) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (offerStatus.needsMore) 
                                MaterialTheme.colorScheme.errorContainer
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = offer.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (offerStatus.needsMore)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                if (offerStatus.needsMore) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "⚠️+${offerStatus.remaining}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
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

                // Total (con descuento si oferta cumplida)
                if (total != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Si hay oferta cumplida, mostrar precio tachado
                        if (offer != null && !offerStatus.needsMore && product.estimatedPrice != null) {
                            val originalTotal = product.estimatedPrice * product.quantity
                            if (originalTotal != total) {
                                Text(
                                    text = "${String.format("%.2f", originalTotal)} €",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                        
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
}

/**
 * Calcula el total aplicando la oferta si corresponde
 */
private fun calculateTotal(product: Product, offer: Offer?, offerStatus: OfferStatus): Float? {
    val unitPrice = product.estimatedPrice ?: return null
    val qty = product.quantity.toInt()
    
    // Si ya tiene precio final guardado, usarlo
    product.finalPrice?.let { return it }
    
    // Si no hay oferta o no se cumple, precio normal
    if (offer == null || offerStatus.needsMore) {
        return unitPrice * qty
    }
    
    // Aplicar oferta
    return when (offer.code) {
        "3x2" -> unitPrice * 2 * (qty / 3) + unitPrice * (qty % 3)  // 3x2: paga 2 de cada 3
        "2x1" -> unitPrice * (qty / 2) + unitPrice * (qty % 2)      // 2x1: paga 1 de cada 2
        "2nd_50" -> unitPrice + (unitPrice * 0.5f * (qty - 1))       // 2ª al 50%
        "2nd_70" -> unitPrice + (unitPrice * 0.3f * (qty - 1))       // 2ª al 30%
        "4x3" -> unitPrice * 3 * (qty / 4) + unitPrice * (qty % 4)  // 4x3: paga 3 de cada 4
        else -> unitPrice * qty
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