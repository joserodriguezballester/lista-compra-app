package com.jose.listacompra.ui.screens.catalogo

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
 * Vista de comparación de 4 variantes del diseño actual de ArticuloCard
 * Todas mantienen el estilo actual con diferentes sombras, márgenes y bordes
 */
@Composable
fun ArticuloCardDesignComparison() {
    val productName = "Yogur Natural Danone"
    val productSize = "125"
    val productUnit = "g"
    val productPrice = 0.75f
    val productCategory = "Lácteos"
    val productCategoryIcon = "🥛"
    val productImage = "https://images.openfoodfacts.org/images/products/841/037/604/0685/front_es.3.400.jpg"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Variantes del Diseño Actual",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Variante 1: Sombra suave
            Box(modifier = Modifier.weight(1f)) {
                Variant1SoftShadow(
                    name = productName,
                    size = productSize,
                    unit = productUnit,
                    price = productPrice,
                    categoryIcon = productCategoryIcon,
                    categoryName = productCategory,
                    imageUrl = productImage
                )
            }
            
            // Variante 2: Sombra pronunciada
            Box(modifier = Modifier.weight(1f)) {
                Variant2StrongShadow(
                    name = productName,
                    size = productSize,
                    unit = productUnit,
                    price = productPrice,
                    categoryIcon = productCategoryIcon,
                    categoryName = productCategory,
                    imageUrl = productImage
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Variante 3: Compacto
            Box(modifier = Modifier.weight(1f)) {
                Variant3Compact(
                    name = productName,
                    size = productSize,
                    unit = productUnit,
                    price = productPrice,
                    categoryIcon = productCategoryIcon,
                    categoryName = productCategory,
                    imageUrl = productImage
                )
            }
            
            // Variante 4: Con borde
            Box(modifier = Modifier.weight(1f)) {
                Variant4WithBorder(
                    name = productName,
                    size = productSize,
                    unit = productUnit,
                    price = productPrice,
                    categoryIcon = productCategoryIcon,
                    categoryName = productCategory,
                    imageUrl = productImage
                )
            }
        }
        
        Text(
            text = "1=Suave | 2=Pronunciada | 3=Compacto | 4=Borde",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ===== VARIANTE 1: SOMBRA SUAVE =====
// Elevación baja (2dp), padding generoso (12dp), esquinas redondeadas
@Composable
fun Variant1SoftShadow(
    name: String,
    size: String,
    unit: String,
    price: Float,
    categoryIcon: String,
    categoryName: String,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$size $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$categoryIcon $categoryName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "${String.format("%.2f", price)} €",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ===== VARIANTE 2: SOMBRA PRONUNCIADA =====
// Elevación alta (6dp), padding medio (10dp), esquinas estándar
@Composable
fun Variant2StrongShadow(
    name: String,
    size: String,
    unit: String,
    price: Float,
    categoryIcon: String,
    categoryName: String,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "$size $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "$categoryIcon $categoryName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${String.format("%.2f", price)} €",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ===== VARIANTE 3: COMPACTO =====
// Sin elevación (0dp), padding mínimo (8dp), esquinas pequeñas
@Composable
fun Variant3Compact(
    name: String,
    size: String,
    unit: String,
    price: Float,
    categoryIcon: String,
    categoryName: String,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "$size $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$categoryIcon $categoryName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${String.format("%.2f", price)} €",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ===== VARIANTE 4: CON BORDE =====
// Elevación media (4dp), padding normal (10dp), borde visible
@Composable
fun Variant4WithBorder(
    name: String,
    size: String,
    unit: String,
    price: Float,
    categoryIcon: String,
    categoryName: String,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "$size $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "$categoryIcon $categoryName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${String.format("%.2f", price)} €",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
