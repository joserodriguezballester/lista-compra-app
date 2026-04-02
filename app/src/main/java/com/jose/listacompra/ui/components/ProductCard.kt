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
import com.jose.listacompra.ui.utils.getSupermarketLogoRes
import com.jose.listacompra.ui.utils.calculateOfferStatus
import com.jose.listacompra.ui.utils.getCategoryEmoji

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

    val supermarketLogoRes = getSupermarketLogoRes(product.notes)
    val offerStatus = calculateOfferStatus(product.quantity, offer)
    val emoji = getCategoryEmoji(product.name)

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
            // FILA SUPERIOR: Imagen + Checkbox + (Logo/Oferta)
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
                        Text(text = emoji, fontSize = 28.sp)
                    }
                }

                // Checkbox (siempre visible)
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = { onTogglePurchased() },
                    modifier = Modifier.size(36.dp)
                )

                // Columna derecha: Logo + Oferta
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

                    // Oferta (si hay) - SIEMPRE mostrar si offer != null
                    if (offer != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (offerStatus.needsMore && !product.isPurchased)
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
                                    color = if (offerStatus.needsMore && !product.isPurchased)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                if (offerStatus.needsMore && !product.isPurchased) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "⚠️+${offerStatus.remaining}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                } else if (!product.isPurchased) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "✅",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
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

            // Precio y cantidad
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cantidad y precio unitario
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                // Total - usar finalPrice del producto (ya calculado en ViewModel)
                val total = product.finalPrice
                if (total != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Si hay oferta cumplida, mostrar precio original tachado
                        if (offer != null && !offerStatus.needsMore && product.estimatedPrice != null && !product.isPurchased) {
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