package com.jose.listacompra.ui.screens.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Offer
import com.jose.listacompra.domain.model.Product

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: Product,
    offer: Offer?,
    onTogglePurchased: () -> Unit,
    onEdit: () -> Unit
) {
    // Card "fantasma" si está comprado (40% opacidad)
    val alpha = if (product.isPurchased) 0.4f else 1f
    val backgroundColor = if (product.isPurchased) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .alpha(alpha)
            .combinedClickable(
                onClick = onTogglePurchased,
                onLongClick = onEdit
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (product.isPurchased) 0.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Checkbox + Nombre
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Checkbox(
                    checked = product.isPurchased,
                    onCheckedChange = { onTogglePurchased() },
                    modifier = Modifier.Companion.size(24.dp)
                )

                Spacer(modifier = Modifier.Companion.width(4.dp))

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!product.isPurchased) FontWeight.Companion.SemiBold else FontWeight.Companion.Normal,
                    textDecoration = if (product.isPurchased) TextDecoration.Companion.LineThrough else null,
                    color = if (product.isPurchased)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Companion.Ellipsis,
                    modifier = Modifier.Companion.weight(1f)
                )
            }

            // Producto comprado: mostrar solo "(comprado)" sin datos de precio
            if (product.isPurchased) {
                Text(
                    text = "(comprado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.Companion.padding(start = 28.dp, top = 4.dp)
                )
            } else {
                // Producto no comprado: mostrar oferta, cantidad, precio, total

                // Indicador de oferta (si existe) - con validación de mínimo
                if (offer != null) {
                    // Verificar si cumple el mínimo para la oferta
                    val minRequired = when (offer.code) {
                        "3x2" -> 3
                        "2x1", "2nd_50", "2nd_70" -> 2
                        "4x3" -> 4
                        else -> 1
                    }
                    val meetsMinimum = product.quantity >= minRequired

                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        modifier = Modifier.Companion.padding(start = 28.dp, top = 2.dp)
                    ) {
                        Text(
                            text = if (meetsMinimum) "🏷️ " else "⚠️ ",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (meetsMinimum) offer.name else "${offer.name} (¡faltan ${minRequired - product.quantity.toInt()}!)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (meetsMinimum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Companion.Medium
                        )
                    }
                }

                // 3 columnas: Cantidad | Precio | Total
                if (product.estimatedPrice != null) {
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(start = 28.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cantidad
                        Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                            Text(
                                text = "Cant",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${product.quantity.toInt()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Precio unitario
                        Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                            Text(
                                text = "Precio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "%.2f€".format(product.estimatedPrice),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Total (con oferta aplicada si existe y cumple mínimo)
                        val meetsMinimum = if (offer != null) {
                            val minRequired = when (offer.code) {
                                "3x2" -> 3
                                "2x1", "2nd_50", "2nd_70" -> 2
                                "4x3" -> 4
                                else -> 1
                            }
                            product.quantity >= minRequired
                        } else true

                        Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "%.2f€".format(product.finalPriceToPay()),
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    offer != null && !meetsMinimum -> MaterialTheme.colorScheme.error
                                    product.hasOffer() -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (product.hasOffer() && meetsMinimum) FontWeight.Companion.Bold else FontWeight.Companion.Normal
                            )
                        }
                    }
                } else {
                    // Solo cantidad si no hay precio
                    Text(
                        text = "${product.quantity.toInt()} uds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.Companion.padding(start = 28.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}