package com.example.ecotochi.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecotochi.data.db.DbProvider
import com.example.ecotochi.ui.components.SimpleAppBar
import com.example.ecotochi.ui.theme.GreenPrimary
import com.example.ecotochi.ui.theme.SurfaceLight
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(db.settingsDao()))

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { SimpleAppBar(title = "Configuraciones", onBack = onBack) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(24.dp)
        ) {
            Text("Umbrales del invernadero", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            // ======== TARJETA DE UMBRALES ========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Text(
                    "Umbrales",
                    style = MaterialTheme.typography.titleSmall,
                    color = GreenPrimary
                )

                Spacer(Modifier.height(8.dp))

                // Humedad
                ThresholdRow(
                    icon = Icons.Filled.WaterDrop,
                    label = "Humedad",
                    minValue = vm.wetMin,
                    onMinChange = vm::onWetMin,
                    maxValue = vm.wetMax,
                    onMaxChange = vm::onWetMax,
                    suffix = "%"
                )

                Divider(Modifier.padding(vertical = 6.dp))

                // pH
                ThresholdRow(
                    icon = Icons.Filled.Water,
                    label = "pH",
                    minValue = vm.phMin,
                    onMinChange = vm::onPhMin,
                    maxValue = vm.phMax,
                    onMaxChange = vm::onPhMax,
                    suffix = ""
                )

                Divider(Modifier.padding(vertical = 6.dp))

                // Temperatura
                ThresholdRow(
                    icon = Icons.Filled.Thermostat,
                    label = "Temperatura",
                    minValue = vm.temperatureMin,
                    onMinChange = vm::onTemperatureMin,
                    maxValue = vm.temperatureMax,
                    onMaxChange = vm::onTemperatureMax,
                    suffix = "°C"
                )
            }

            Spacer(Modifier.height(16.dp))

            // ======== TARJETA DE INTERVALOS ========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Text(
                    "Intervalos",
                    style = MaterialTheme.typography.titleSmall,
                    color = GreenPrimary
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lectura periódica",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    SmallNumberField(
                        value = vm.readingTime,
                        onValueChange = vm::onReadingTime,
                        width = 90.dp
                    )

                    Spacer(Modifier.width(6.dp))
                    Text("s")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ======== NUEVA TARJETA: MODO DE RIEGO ========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Text(
                    "Modo de riego",
                    style = MaterialTheme.typography.titleSmall,
                    color = GreenPrimary
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Riego automático",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            if (vm.automaticIrrigation)
                                "El sistema activará el riego según los umbrales"
                            else
                                "El riego se controla de forma manual",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = vm.automaticIrrigation,
                        onCheckedChange = vm::onAutomaticIrrigationChange
                    )
                }
            }

            if (vm.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Error: ${vm.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    vm.save { ok ->
                        scope.launch {
                            snackbar.showSnackbar(
                                if (ok) "Configuración guardada"
                                else "No fue posible guardar"
                            )
                        }
                    }
                },
                enabled = !vm.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (vm.isLoading) "Guardando..." else "Guardar")
            }
        }
    }
}


/* ================== COMPONENTES ================== */

@Composable
private fun ThresholdRow(
    icon: ImageVector,
    label: String,
    minValue: String,
    onMinChange: (String) -> Unit,
    maxValue: String,
    onMaxChange: (String) -> Unit,
    suffix: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GreenPrimary,
            modifier = Modifier.size(26.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(
            label,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.9f)
        ) {
            Text("Mín.", style = MaterialTheme.typography.labelSmall)
            SmallNumberField(
                value = minValue,
                onValueChange = onMinChange,
                width = 70.dp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.9f)
        ) {
            Text("Máx.", style = MaterialTheme.typography.labelSmall)
            SmallNumberField(
                value = maxValue,
                onValueChange = onMaxChange,
                width = 70.dp
            )
        }

        Spacer(Modifier.width(4.dp))
        Text(suffix, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SmallNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp = 80.dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            onValueChange(new.filter { c -> c.isDigit() || c == '.' })
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width(width)
    )
}
