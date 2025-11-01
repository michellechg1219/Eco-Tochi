package com.example.ecotochi.data.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LiveReadings(
    val ph: Double?,
    val temperature: Double?,
    val humidity: Double?,
    val timestamp: String,
)

class EspRepository(
    private val baseUrl: String // ej. "http://192.168.1.80"
) {

    // --- LECTURAS ---
    suspend fun fetch(): LiveReadings = withContext(Dispatchers.IO) {
        val t = fetchValue("$baseUrl/temperature")
        val h = fetchValue("$baseUrl/humidity")
        val p = fetchValue("$baseUrl/ph") // si no existe en tu ESP quedará null

        LiveReadings(
            ph = p,
            temperature = t,
            humidity = h,
            timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
    }

    // --- CONTROL ON/OFF ---
    suspend fun turnOn() = simpleGet("$baseUrl/on")

    suspend fun turnOff() = simpleGet("$baseUrl/off")

    // puedes usar este si luego quieres /status o algo así
    private suspend fun simpleGet(url: String): String = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
            }
            conn.inputStream.use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            // lo regresamos como string para que el VM pueda mostrar el error si quiere
            "{\"ok\":false,\"error\":\"${e.message ?: "request error"}\"}"
        }
    }

    // --- util para leer /temperature, /humidity, /ph ---
    private fun fetchValue(url: String): Double? {
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
            }
            conn.inputStream.use { ins ->
                val raw = ins.bufferedReader().readText()
                val root = JSONObject(raw)
                val data = root.optJSONObject("data")
                val value = data?.opt("value")
                when (value) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull()
                    else -> null
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
