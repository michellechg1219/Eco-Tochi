package com.example.ecotochi.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ecotochi.data.db.DbProvider
import com.example.ecotochi.data.net.EspRepository
import com.example.ecotochi.domain.checkThresholdsList
import com.example.ecotochi.util.notifyAlert

class MonitoringWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d("MonitoringWorker", "doWork: iniciado")

        return try {
            val context = applicationContext

            // 1) DB / settings
            val db = DbProvider.get(context)
            val settings = db.settingsDao().getLast()
            Log.d("MonitoringWorker", "doWork: settings = $settings")

            // Si no hay settings, no hay con qué comparar, pero al menos sabemos que llegó
            if (settings == null) {
                Log.d("MonitoringWorker", "doWork: NO hay settings, termino en success")
                // opcional: notifyAlert(context, "⚠️ No hay configuración de umbrales")
                return Result.success()
            }

            // 2) ESP
            val repo = EspRepository(baseUrl = "http://192.168.1.9")
            val reading = repo.fetch()
            Log.d("MonitoringWorker", "doWork: lectura = $reading")

            // 3) Validar por separado pH, temp y humedad
            val check = checkThresholdsList(
                settings = settings,
                ph = reading.ph,
                temp = reading.temperature,
                hum = reading.humidity
            )

            if (check.messages.isNotEmpty()) {
                // 👉 aquí vienen 1, 2 o 3 mensajes según lo que esté fuera de rango
                check.messages.forEach { msg ->
                    Log.d("MonitoringWorker", "doWork: alerta = $msg")
                    notifyAlert(context, msg)
                }
            } else {
                Log.d("MonitoringWorker", "doWork: todo dentro de rango")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("MonitoringWorker", "doWork: ERROR ${e.message}", e)
            return Result.retry()
        }
    }
}
