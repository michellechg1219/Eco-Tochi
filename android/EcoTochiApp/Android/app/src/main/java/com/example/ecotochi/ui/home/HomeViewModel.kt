package com.example.ecotochi.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecotochi.data.db.dao.HistoricalDao
import com.example.ecotochi.data.db.dao.SettingsDao
import com.example.ecotochi.data.db.entity.HistoricalReading
import com.example.ecotochi.data.net.EspRepository
import com.example.ecotochi.data.net.LiveReadings
import com.example.ecotochi.domain.checkThresholdsList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val phText: String = "–",
    val tempText: String = "–",
    val humText: String = "–",
    val lastTs: String = "-",
    val error: String? = null,
    val deviceOn: Boolean = false,
    val isToggling: Boolean = false,
    val alerts: List<String> = emptyList(),
    val automaticIrrigation: Boolean = false, // 👈 modo automático ON/OFF
)

class HomeViewModel(
    private val settingsDao: SettingsDao,
    private val historicalDao: HistoricalDao,
    private val repo: EspRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui = _ui.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    private var loopJob: Job? = null

    init {
        viewModelScope.launch {
            settingsDao.observeSettings().collect { s ->
                val periodSec = (s?.readingTime ?: 60L).coerceAtLeast(5L)

                _ui.value = _ui.value.copy(
                    automaticIrrigation = s?.automaticIrrigation ?: false
                )

                Log.d(
                    "HomeViewModel",
                    "observeSettings: periodSec=$periodSec auto=${s?.automaticIrrigation}"
                )

                startLoop(periodSec)
            }
        }
    }

    private fun startLoop(periodSec: Long) {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (true) {
                try {
                    // 1) settings
                    val currentSettings = settingsDao.getLast()
                    Log.d("HomeViewModel", "loop: settings=$currentSettings")

                    // 2) lectura ESP
                    val r: LiveReadings = repo.fetch()
                    Log.d("HomeViewModel", "loop: reading=$r")

                    // 3) alertas
                    val alertList: List<String> = if (currentSettings != null) {
                        val check = checkThresholdsList(
                            settings = currentSettings,
                            ph = r.ph,
                            temp = r.temperature,
                            hum = r.humidity
                        )
                        check.messages
                    } else {
                        emptyList()
                    }

                    // 4) lógica de riego automático
                    if (currentSettings != null &&
                        currentSettings.automaticIrrigation &&
                        r.humidity != null
                    ) {
                        val hum = r.humidity
                        val wetMin = currentSettings.wetMin
                        val wetMax = currentSettings.wetMax
                        val currentlyOn = _ui.value.deviceOn

                        Log.d(
                            "HomeViewModel",
                            "autoIrrigation: hum=$hum wetMin=$wetMin wetMax=$wetMax deviceOn=$currentlyOn"
                        )

                        when {
                            // ENCENDER cuando hum <= wetMin
                            !currentlyOn && hum <= wetMin -> {
                                Log.d("HomeViewModel", "autoIrrigation: hum <= wetMin -> turnOn()")
                                repo.turnOn()
                                _ui.value = _ui.value.copy(deviceOn = true)
                            }

                            // APAGAR cuando hum entre (wetMin, wetMax)
                            currentlyOn && hum > wetMin && hum < wetMax -> {
                                Log.d(
                                    "HomeViewModel",
                                    "autoIrrigation: hum entre wetMin y wetMax -> turnOff()"
                                )
                                repo.turnOff()
                                _ui.value = _ui.value.copy(deviceOn = false)
                            }

                            // Seguridad: hum >= wetMax también apagamos
                            currentlyOn && hum >= wetMax -> {
                                Log.d(
                                    "HomeViewModel",
                                    "autoIrrigation: hum >= wetMax -> turnOff()"
                                )
                                repo.turnOff()
                                _ui.value = _ui.value.copy(deviceOn = false)
                            }
                        }
                    } else {
                        Log.d(
                            "HomeViewModel",
                            "autoIrrigation: SKIP (settings=null, auto=false o humidity=null)"
                        )
                    }

                    // 5) UI
                    _ui.value = _ui.value.copy(
                        phText = r.ph?.let { String.format("%.2f", it) } ?: "–",
                        tempText = r.temperature?.let { String.format("%.1f °C", it) } ?: "–",
                        humText = r.humidity?.let { String.format("%.1f %%", it) } ?: "–",
                        lastTs = r.timestamp,
                        error = null,
                        alerts = alertList
                    )

                    // 6) histórico
                    if (r.temperature != null && r.humidity != null && r.ph != null) {
                        historicalDao.insert(
                            HistoricalReading(
                                temperature = r.temperature,
                                wet = r.humidity,
                                date = r.timestamp,
                                ph = r.ph
                            )
                        )
                    }

                } catch (e: Exception) {
                    Log.e("HomeViewModel", "loop ERROR: ${e.message}", e)
                    _ui.value = _ui.value.copy(error = e.message ?: "Error de red")
                }

                delay(periodSec * 1000)
            }
        }
    }

    fun toggleDevice() {
        // Si el riego es automático, ignoramos el toque
        if (_ui.value.automaticIrrigation) {
            Log.d("HomeViewModel", "toggleDevice: ignorado, modo automático activo")
            return
        }

        if (_ui.value.isToggling) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isToggling = true)
            try {
                if (_ui.value.deviceOn) {
                    Log.d("HomeViewModel", "toggleDevice: turnOff() manual")
                    repo.turnOff()
                    _ui.value = _ui.value.copy(deviceOn = false)
                } else {
                    Log.d("HomeViewModel", "toggleDevice: turnOn() manual")
                    repo.turnOn()
                    _ui.value = _ui.value.copy(deviceOn = true)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "toggleDevice ERROR: ${e.message}", e)
                _ui.value = _ui.value.copy(error = e.message)
            } finally {
                _ui.value = _ui.value.copy(isToggling = false)
            }
        }
    }

    companion object {
        fun factory(
            settingsDao: SettingsDao,
            historicalDao: HistoricalDao,
            repo: EspRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(settingsDao, historicalDao, repo) as T
                }
            }
    }
}
