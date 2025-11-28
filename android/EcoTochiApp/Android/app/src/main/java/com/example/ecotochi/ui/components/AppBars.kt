package com.example.ecotochi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SimpleAppBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()   // 👈 deja espacio para notch / status bar
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    Text(
                        "←",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { onBack() },
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,                       // 👈 no más de una línea
                    overflow = TextOverflow.Ellipsis,   // 👈 si se pasa, pone "EcoToch…"
                    modifier = Modifier.weight(1f)
                )
            }
            Divider()
        }
    }
}
