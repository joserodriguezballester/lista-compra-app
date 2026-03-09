package com.jose.listacompra.ui.screens.main.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jose.listacompra.domain.model.Aisle

@Composable
fun AisleHeader(
    aisle: Aisle,
    productCount: Int,
    purchasedCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                text = "${aisle.emoji} ${aisle.name}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.Companion.weight(1f)
            )
            Text(
                text = "$purchasedCount/$productCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}