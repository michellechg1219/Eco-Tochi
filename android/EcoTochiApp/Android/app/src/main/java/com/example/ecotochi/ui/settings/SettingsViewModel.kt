package com.example.ecotochi.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecotochi.data.db.dao.SettingsDao
import com.example.ecotochi.data.db.entity.SettingsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dao: SettingsDao
) : ViewModel() {

    // Campos numéricos como String
    var temperatureMax by mutableStateOf("")
        private set
    var temperatureMin by mutableStateOf("")
        private set
    var wetMax by mutableStateOf("")
        private set
    var wetMin by mutableStateOf("")
        private set
    var readingTime by mutableStateOf("")   // segundos
        private set

    // pH
    var phMax by mutableStateOf("")
        private set
    var phMin by mutableStateOf("")
        private set

    // NUEVO: bandera de riego automático
    var automaticIrrigation by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        // Carga inicial (GET)
        viewModelScope.launch {
            isLoading = true
            try {
                val current = dao.observeSettings().first()
                if (current != null) {
                    temperatureMax = current.temperatureMax.toString()
                    temperatureMin = current.temperatureMin.toString()
                    wetMax = current.wetMax.toString()
                    wetMin = current.wetMin.toString()
                    readingTime = current.readingTime.toString()
                    phMax = current.phMax.toString()
                    phMin = current.phMin.toString()
                    automaticIrrigation = current.automaticIrrigation
                } else {
                    // valores por defecto
                    temperatureMax = "30.0"
                    temperatureMin = "18.0"
                    wetMax = "80.0"
                    wetMin = "40.0"
                    readingTime = "60"
                    phMax = "8.0"
                    phMin = "6.0"
                    automaticIrrigation = false  // por defecto manual
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun onTemperatureMax(v: String) { temperatureMax = v }
    fun onTemperatureMin(v: String) { temperatureMin = v }
    fun onWetMax(v: String) { wetMax = v }
    fun onWetMin(v: String) { wetMin = v }
    fun onReadingTime(v: String) { readingTime = v }
    fun onPhMax(v: String) { phMax = v }
    fun onPhMin(v: String) { phMin = v }

    // NUEVO: cambio de modo de riego
    fun onAutomaticIrrigationChange(enabled: Boolean) {
        automaticIrrigation = enabled
    }

    // Guardar (UPSERT)
    fun save(onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val entity = SettingsEntity(
                    id = 1, // único registro
                    temperatureMax = temperatureMax.toDoubleOrNull() ?: 0.0,
                    temperatureMin = temperatureMin.toDoubleOrNull() ?: 0.0,
                    wetMax = wetMax.toDoubleOrNull() ?: 0.0,
                    wetMin = wetMin.toDoubleOrNull() ?: 0.0,
                    readingTime = readingTime.toLongOrNull() ?: 60L,
                    phMax = phMax.toDoubleOrNull() ?: 8.0,
                    phMin = phMin.toDoubleOrNull() ?: 6.0,
                    automaticIrrigation = automaticIrrigation
                )
                dao.upsert(entity)
                onDone(true)
            } catch (e: Exception) {
                error = e.message
                onDone(false)
            } finally {
                isLoading = false
            }
        }
    }

    companion object {
        fun factory(dao: SettingsDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(dao) as T
                }
            }
    }
}
