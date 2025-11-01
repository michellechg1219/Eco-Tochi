package com.example.ecotochi.domain

import com.example.ecotochi.data.db.entity.SettingsEntity

/**
 * Versión que regresa una LISTA de mensajes, uno por cada valor fuera de rango.
 */
data class ThresholdResultList(
    val messages: List<String>
)

/**
 * Revisa pH, temperatura y humedad por separado.
 */
fun checkThresholdsList(
    settings: SettingsEntity,
    ph: Double?,
    temp: Double?,
    hum: Double?
): ThresholdResultList {
    val msgs = mutableListOf<String>()

    // pH
    if (ph != null) {
        if (ph < settings.phMin || ph > settings.phMax) {
            msgs.add("pH fuera de rango ($ph / ${settings.phMin}-${settings.phMax})")
        }
    }

    // temperatura
    if (temp != null) {
        if (temp < settings.temperatureMin || temp > settings.temperatureMax) {
            msgs.add(
                "Temperatura fuera de rango (${String.format("%.1f", temp)} / " +
                        "${settings.temperatureMin}-${settings.temperatureMax})"
            )
        }
    }

    // humedad
    if (hum != null) {
        if (hum < settings.wetMin || hum > settings.wetMax) {
            msgs.add(
                "Humedad fuera de rango (${String.format("%.1f", hum)} / " +
                        "${settings.wetMin}-${settings.wetMax})"
            )
        }
    }

    return ThresholdResultList(messages = msgs)
}
