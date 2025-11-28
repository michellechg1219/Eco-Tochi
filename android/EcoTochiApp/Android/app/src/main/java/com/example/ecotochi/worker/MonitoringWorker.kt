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
            val settings = db.settingsDao().getLast()   // o getSettings() si renombraste
            Log.d("MonitoringWorker", "doWork: settings = $settings")

            // Si no hay settings, no hay con qué comparar
            if (settings == null) {
                Log.d("MonitoringWorker", "doWork: NO hay settings, termino en success")
                return Result.success()
            }

            // 2) ESP  (usa la MISMA IP que en HomeScreen)
            val repo = EspRepository(baseUrl = "http://192.168.1.10")
            val reading = repo.fetch()
            Log.d("MonitoringWorker", "doWork: lectura = $reading")

            // 3) Validar pH, temp y humedad → mensajes de alerta
            val check = checkThresholdsList(
                settings = settings,
                ph = reading.ph,
                temp = reading.temperature,
                hum = reading.humidity
            )

            if (check.messages.isNotEmpty()) {
                check.messages.forEach { msg ->
                    Log.d("MonitoringWorker", "doWork: alerta = $msg")
                    notifyAlert(context, msg)
                }
            } else {
                Log.d("MonitoringWorker", "doWork: todo dentro de rango")
            }

            // 4) Lógica de riego automático usando wetMin / wetMax
            if (settings.automaticIrrigation && reading.humidity != null) {
                val hum = reading.humidity
                val wetMin = settings.wetMin
                val wetMax = settings.wetMax

                Log.d(
                    "MonitoringWorker",
                    "doWork: automaticIrrigation=ON hum=$hum wetMin=$wetMin wetMax=$wetMax"
                )

                when {
                    // ENCENDER cuando hum <= wetMin
                    hum <= wetMin -> {
                        Log.d(
                            "MonitoringWorker",
                            "doWork: hum <= wetMin ($hum <= $wetMin) → ENCENDER riego (GET /on)"
                        )
                        repo.turnOn()
                    }

                    // APAGAR cuando hum entre min y max
                    hum > wetMin && hum < wetMax -> {
                        Log.d(
                            "MonitoringWorker",
                            "doWork: hum entre wetMin y wetMax ($hum) → APAGAR riego (GET /off)"
                        )
                        repo.turnOff()
                    }

                    // Seguridad: si supera el máximo, también apagar
                    hum >= wetMax -> {
                        Log.d(
                            "MonitoringWorker",
                            "doWork: hum >= wetMax ($hum >= $wetMax) → APAGAR riego (GET /off)"
                        )
                        repo.turnOff()
                    }
                }
            } else {
                Log.d(
                    "MonitoringWorker",
                    "doWork: riego automático desactivado o humedad=null → no se manda comando"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("MonitoringWorker", "doWork: ERROR ${e.message}", e)
            return Result.retry()
        }
    }
}
