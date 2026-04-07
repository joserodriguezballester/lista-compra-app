package com.jose.listacompra.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jose.listacompra.R
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

@Composable
fun ProductCard(
    product: Product,
    offer: Offer? = null,
    onClick: () -> Unit,
    onTogglePurchased: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (product.offerId != null && product.offerId > 0) {
        Log.d("ProductCard", "📦 ${product.name} tiene offerId=${product.offerId}, offer=${offer?.name}")
    }
    
    val backgroundColor = if (product.isPurchased) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val supermarketLogoRes = getSupermarketLogo(product.notes)
    val offerStatus = calculateOfferStatus(product.quantity, offer)
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
                        Text(
                            text = getCategoryEmoji(product.name),
                            fontSize = 28.sp
                        )
                    }
                }

                // Checkbox
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = { onTogglePurchased() },
                    modifier = Modifier.size(36.dp)
                )

                // Logo supermercado
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.isPurchased) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        MaterialTheme.colorScheme.surface
                ) {
                    when {
                        supermarketLogoRes != null -> {
                            Icon(
                                painter = painterResource(id = supermarketLogoRes),
                                contentDescription = "Supermercado",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                tint = androidx.compose.ui.graphics.Color.Unspecified
                            )
                        }
                        product.notes.isNotBlank() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📝", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            // Oferta debajo del nombre (si hay)
            if (offer != null && !product.isPurchased) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (offerStatus.needsMore) 
                        MaterialTheme.colorScheme.errorContainer
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = offer.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (offerStatus.needsMore)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (offerStatus.needsMore) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${offerStatus.remaining}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
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

                if (total != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

private fun calculateTotal(product: Product, offer: Offer?, offerStatus: OfferStatus): Float? {
    val unitPrice = product.estimatedPrice ?: return null
    val qty = product.quantity.toInt()
    
    product.finalPrice?.let { return it }
    
    if (offer == null || offerStatus.needsMore) {
        return unitPrice * qty
    }
    
    return when (offer.code) {
        "3x2" -> unitPrice * 2 * (qty / 3) + unitPrice * (qty % 3)
        "2x1" -> unitPrice * (qty / 2) + unitPrice * (qty % 2)
        "2nd_50" -> unitPrice + (unitPrice * 0.5f * (qty - 1))
        "2nd_70" -> unitPrice + (unitPrice * 0.3f * (qty - 1))
        "4x3" -> unitPrice * 3 * (qty / 4) + unitPrice * (qty % 4)
        else -> unitPrice * qty
    }
}

private fun getCategoryEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("leche") -> "🥛"
        lower.contains("pan") -> "🍞"
        lower.contains("huevo") -> "🥚"
        lower.contains("yogur") -> "🥛"
        lower.contains("queso") -> "🧀"
        lower.contains("tomate") -> "🍅"
        lower.contains("plátano") || lower.contains("platano") -> "🍌"
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