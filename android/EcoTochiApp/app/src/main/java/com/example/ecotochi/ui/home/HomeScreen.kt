package com.example.ecotochi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecotochi.data.db.DbProvider
import com.example.ecotochi.data.net.EspRepository
import com.example.ecotochi.ui.components.SimpleAppBar

@Composable
fun HomeScreen(
    onGoSettings: () -> Unit,
    onGoHistory: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val repo = remember { EspRepository(baseUrl = "http://192.168.1.9") }

    val vm: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            settingsDao = db.settingsDao(),
            historicalDao = db.historicalDao(),
            repo = repo
        )
    )
    val ui by vm.ui.collectAsState()

    Scaffold(topBar = { SimpleAppBar(title = "EcoTochi") }) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(24.dp)
        ) {
            Text("Bienvenido 👋", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))

            // fila de botones arriba
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onGoSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Configuración")
                }
                Button(
                    onClick = onGoHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Historial",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Historial")
                }
            }

            Spacer(Modifier.height(20.dp))

            // título de la tabla
            Text("Parámetros del invernadero", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // ===== Tabla vertical estilo tarjeta =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ReadingRow(
                    icon = Icons.Default.WaterDrop,
                    label = "Humedad",
                    value = ui.humText
                )
                DividerRow()
                ReadingRow(
                    icon = Icons.Default.Water,
                    label = "pH",
                    value = ui.phText
                )
                DividerRow()
                ReadingRow(
                    icon = Icons.Default.Thermostat,
                    label = "Temperatura",
                    value = ui.tempText
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Última actualización: ${ui.lastTs}",
                style = MaterialTheme.typography.bodySmall
            )

            // ===== Sección de alertas =====
            if (ui.alerts.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Alertas", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                ui.alerts.forEach { msg ->
                    Text(
                        text = "• $msg",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ===== Control de bomba =====
            Text("Control de bomba de agua", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { vm.toggleDevice() },
                enabled = !ui.isToggling,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = if (ui.deviceOn) "Apagar" else "Encender",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(if (ui.deviceOn) "Apagar" else "Encender")
            }

            // errores (los dejo hasta abajo)
            if (ui.error != null) {
                Spacer(Modifier.height(12.dp))
                Text("Error: ${ui.error}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/* ---------- Helpers UI ---------- */

@Composable
private fun ReadingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DividerRow() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    )
}
