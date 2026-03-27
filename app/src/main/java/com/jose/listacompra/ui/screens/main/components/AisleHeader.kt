package com.jose.listacompra.ui.screens.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle

@Composable
fun AisleHeader(
    aisle: Aisle,
    productCount: Int = 0,
    purchasedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(
            text = aisle.emoji,
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Nombre del pasillo
        Text(
            text = aisle.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Contador
        Text(
            text = "$purchasedCount/$productCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
