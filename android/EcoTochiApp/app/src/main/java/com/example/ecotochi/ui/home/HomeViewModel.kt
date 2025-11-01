package com.example.ecotochi.ui.home

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
    val alerts: List<String> = emptyList(),  // 👈 ahora es lista
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
        // Observa la configuración (cada cuánto leer) y arranca el loop
        viewModelScope.launch {
            settingsDao.observeSettings().collect { s ->
                val periodSec = (s?.readingTime ?: 60L).coerceAtLeast(5L)
                startLoop(periodSec)
            }
        }
    }

    private fun startLoop(periodSec: Long) {
        // si ya había un loop corriendo, lo cancelamos
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (true) {
                try {
                    // 1) leer settings actuales (puede ser null si nunca han configurado)
                    val currentSettings = settingsDao.getLast()

                    // 2) leer del ESP
                    val r: LiveReadings = repo.fetch()

                    // 3) checar umbrales si hay settings → LISTA
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

                    // 4) Actualiza la UI con lo que haya
                    _ui.value = _ui.value.copy(
                        phText = r.ph?.let { String.format("%.2f", it) } ?: "–",
                        tempText = r.temperature?.let { String.format("%.1f °C", it) } ?: "–",
                        humText = r.humidity?.let { String.format("%.1f %%", it) } ?: "–",
                        lastTs = r.timestamp,
                        error = null,
                        alerts = alertList
                    )

                    // 5) Solo guarda si LLEGARON LAS TRES: temp, hum y pH
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
                    _ui.value = _ui.value.copy(error = e.message ?: "Error de red")
                }

                // espera el periodo configurado
                delay(periodSec * 1000)
            }
        }
    }

    fun toggleDevice() {
        if (_ui.value.isToggling) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isToggling = true)
            try {
                if (_ui.value.deviceOn) {
                    repo.turnOff()
                    _ui.value = _ui.value.copy(deviceOn = false)
                } else {
                    repo.turnOn()
                    _ui.value = _ui.value.copy(deviceOn = true)
                }
            } catch (e: Exception) {
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
