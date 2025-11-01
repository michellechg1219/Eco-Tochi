package com.example.ecotochi.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecotochi.data.db.DbProvider
import com.example.ecotochi.ui.components.SimpleAppBar

@Composable
fun HistoryScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }
    val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(db.historicalDao()))

    val rows by vm.rows.collectAsState()
    val snack = remember { SnackbarHostState() }

    Scaffold(
        topBar = { SimpleAppBar(title = "Historial", onBack = onBack) },
        snackbarHost = { SnackbarHost(hostState = snack) }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp)
        ) {
            // Encabezados de la "tabla"
            HeaderRow()

            Divider()

            // Filas
            LazyColumn {
                items(rows, key = { it.id }) { r ->
                    DataRow(
                        temperature = r.temperatureText,
                        wet = r.wetText,
                        ph = r.phText,
                        date = r.dateText
                    )
                    Divider()
                }
            }

            if (rows.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "No hay lecturas todavía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Temperatura
        HeaderCell(
            icon = Icons.Filled.Thermostat,
            text = "Temperatura (°C)",
            weight = 1.0f
        )
        // Humedad
        HeaderCell(
            icon = Icons.Filled.WaterDrop,
            text = "Humedad (%)",
            weight = 1.0f
        )
        // pH
        HeaderCell(
            icon = Icons.Filled.Water,
            text = "pH",
            weight = 0.8f
        )
        // Fecha (sin icono)
        Cell(text = "Fecha", weight = 1.2f, bold = true)
    }
}

@Composable
private fun DataRow(temperature: String, wet: String, ph: String, date: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cell(text = temperature, weight = 1.0f)
        Cell(text = wet,         weight = 1.0f)
        Cell(text = ph,          weight = 0.8f)
        Cell(text = date,        weight = 1.2f)
    }
}

@Composable
private fun RowScope.HeaderCell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    weight: Float
) {
    Row(
        modifier = Modifier
            .weight(weight)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun RowScope.Cell(text: String, weight: Float, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(end = 8.dp),
        style = if (bold)
            MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        else
            MaterialTheme.typography.bodyMedium
    )
}
